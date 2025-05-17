package com.google.ar.core.examples.java.common.navigation;

import android.location.Location;

import com.google.ar.core.examples.java.common.entityModel.Point;
import com.google.ar.core.examples.java.common.entityModel.Storage;

import java.util.Comparator;
import java.util.List;

public class PointManager {
    private Point next;
    private final Point destination;

    public PointManager(Point destination) {
        this.destination = destination;
    }

    public void pointManagerCallback(Location loc) {
        //TESTING DATA REMOVE LATER
        final Location location = new Location("ASD");
        location.setLatitude(0f);
        location.setLongitude(0f);
        location.setAltitude(0f);
        Point closest = Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).min(Comparator.comparingDouble(x ->
                Math.sqrt(Math.pow(x.getLatitude() - location.getLatitude(), 2) + Math.pow(x.getLongitude() - location.getLongitude(), 2) + Math.pow(x.getAltitude() - location.getAltitude(), 2))
        )).get();
        List<Point> points = Dijkstra.solve(Storage.INSTANCE.getConnections(),
                Storage.INSTANCE.getLevels().stream().flatMap(x -> x.getPoints().stream()).toList(),
                closest,
                destination);
        System.out.print("ORDER: ");
        points.forEach(x -> System.out.print(x.getName() + " "));
        System.out.println();
        next = points.get(points.size() - 1);
    }

    public Point getNext() {
        return next;
    }

}