package hu.pte.mik.l4z4ix.src.Components.navigation;

import android.location.Location;

import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

import hu.pte.mik.l4z4ix.src.Components.entityModel.Connection;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;

public class Navigator {
    private final Logger logger = Logger.getLogger("navigator");
    private final double REACHING_TRESHOLD = 2.0;
    private final List<Connection> connections;
    private final List<Point> points;
    private final Point target;
    private List<Point> path = null;
    private int currentStepIndex = 0;

    public Navigator(List<Connection> connections, List<Point> points, Point target) {
        this.connections = connections;
        this.points = points;
        this.target = target;
    }

    public void startNavigation(Location CurrentLocation) {
        if (path == null || path.isEmpty()) {
            logger.info("Starting navigation at " + CurrentLocation + " location");
            Point start = findClosestReachableNode(CurrentLocation);
            logger.info("Found closest node named " + start);
            path = Dijkstra.solve(connections, points, start, target);
            logger.info("Set path to " + path);
            currentStepIndex = 0;
        }
    }

    Vector3 previousObjectPose = new Vector3();

    public Point getCurrentPlacementLocation(Vector3 objectPose, Vector3 cameraPose) {
        if (currentStepIndex >= path.size())
            return null;
        if (!previousObjectPose.equals(objectPose) && Vector3.subtract(objectPose, cameraPose).length() < REACHING_TRESHOLD) {
            previousObjectPose = objectPose;
            if (++currentStepIndex >= path.size()) {
                return null;
            }
        }
        return path.get(currentStepIndex);

    }


    private Location pointToLocation(Point point) {
        Location pointLoc = new Location("");
        pointLoc.setAltitude(point.getAltitude());
        pointLoc.setLatitude(point.getLatitude());
        pointLoc.setLongitude(point.getLongitude());
        return pointLoc;
    }

    private Point findClosestReachableNode(Location current) {
        List<Point> sorted = new ArrayList<>(points);
        sorted.sort(Comparator.comparingDouble(p -> {
                    double dist = current.distanceTo(pointToLocation(p));
                    logger.info(p.getName() + "point is " + dist + " distance away");
                    return dist;
                }
        ));
        logger.info("Finding closest point to destination, sorted list is: " + sorted);
        for (Point candidate : sorted) {
            logger.info("Trying candidate " + candidate.getName());
            List<Point> path = Dijkstra.solve(connections, points, candidate, target);
            logger.info("Candidate gave path of " + path);
            if (!path.isEmpty()) {
                logger.info(candidate.getName() + " candidate gave valid path");
                return candidate;
            }
        }
        return null;
    }
}
