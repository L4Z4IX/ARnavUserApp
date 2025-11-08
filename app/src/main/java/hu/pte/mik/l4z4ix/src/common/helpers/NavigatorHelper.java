package hu.pte.mik.l4z4ix.src.common.helpers;

import android.location.Location;

import com.google.ar.sceneform.math.Vector3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import hu.pte.mik.l4z4ix.src.common.entityModel.Connection;
import hu.pte.mik.l4z4ix.src.common.entityModel.Point;
import hu.pte.mik.l4z4ix.src.common.navigation.Dijkstra;

public class NavigatorHelper {
    private final double REACHING_TRESHOLD = 2.0;
    private final List<Connection> connections;
    private final List<Point> points;
    private final Point target;
    private List<Point> path = null;
    private int currentStepIndex = 0;

    public NavigatorHelper(List<Connection> connections, List<Point> points, Point target) {
        this.connections = connections;
        this.points = points;
        this.target = target;
    }

    public void startNavigation(Location CurrentLocation) {
        if (path == null || path.isEmpty()) {
            Point start = findClosestReachableNode(CurrentLocation);
            path = Dijkstra.solve(connections, points, start, target);
            currentStepIndex = 0;
        }
    }

    public Point getCurrentPlacementLocation(Vector3 objectPose, Vector3 cameraPose) {

        if (Vector3.subtract(objectPose, cameraPose).length() < REACHING_TRESHOLD) {
            if (currentStepIndex + 1 >= path.size()) {
                return null;
            }
            currentStepIndex++;
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
        sorted.sort(Comparator.comparingDouble(p -> current.distanceTo(pointToLocation(p))));
        for (Point candidate : sorted) {
            List<Point> path = Dijkstra.solve(connections, points, candidate, target);
            if (path != null && !path.isEmpty()) {
                return candidate;
            }
        }
        return null;
    }
}
