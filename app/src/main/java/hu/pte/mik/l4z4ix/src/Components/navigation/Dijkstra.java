package hu.pte.mik.l4z4ix.src.Components.navigation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.PriorityQueue;

import hu.pte.mik.l4z4ix.src.Components.entityModel.Connection;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Point;

public class Dijkstra {
    public static ArrayList<Point> solve(List<Connection> connections, List<Point> points, Point start, Point end) {
        HashMap<Long, Point> idToPoint = new HashMap<>();
        ArrayList<Point> pointsInOrder = new ArrayList<>();
        HashMap<Point, PriorityQueue<Connection>> connectionsWithPriority = new HashMap<>();
        HashMap<Point, Double> distances = new HashMap<>();
        HashMap<Point, Point> previous = new HashMap<>();
        PriorityQueue<Point> queue = new PriorityQueue<>(Comparator.comparingDouble(distances::get));

        points.forEach(x -> idToPoint.put(x.getId(), x));


        points.forEach(x -> connectionsWithPriority.put(x, new PriorityQueue<>(
                connections.stream().filter(y -> Objects.equals(y.getPoint1Id(), x.getId()) || Objects.equals(y.getPoint2Id(), x.getId())).toList()
        )));


        for (Point p : points) {
            distances.put(p, Double.MAX_VALUE);
        }
        distances.put(start, 0.0);
        queue.add(start);


        while (!queue.isEmpty()) {
            Point current = queue.poll();

            if (current.equals(end)) break;

            for (Connection conn : connectionsWithPriority.get(current)) {
                Point neighbor = conn.getPoint1Id().equals(current.getId()) ? idToPoint.get(conn.getPoint2Id()) : idToPoint.get(conn.getPoint1Id());

                if (neighbor != null) {
                    Double newDist = distances.get(current) + conn.getDistance();
                    if (newDist < distances.get(neighbor)) {
                        distances.put(neighbor, newDist);
                        previous.put(neighbor, current);
                        queue.add(neighbor);
                    }
                }
            }
        }


        for (Point at = end; at != null; at = previous.get(at)) {
            pointsInOrder.add(at);
        }
        Collections.reverse(pointsInOrder);
        return pointsInOrder.isEmpty() || !pointsInOrder.get(0).equals(start) ? new ArrayList<>() : pointsInOrder;
    }

}
