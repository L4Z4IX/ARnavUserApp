package com.google.ar.core.examples.java.common.entityModel;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private List<Venue> venues=new ArrayList<>();
    private List<Level> levels=new ArrayList<>();
    private List<Point> points=new ArrayList<>();
    private List<Connection> connections=new ArrayList<>();

    public static final Storage INSTANCE=new Storage();

    private Storage(){

    }
    public void setVenues(List<Venue> venues){
        this.venues=venues;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }

    public void setPoints(List<Point> points) {
        this.points = points;
    }

    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public List<Level> getLevels() {
        return levels;
    }

    public List<Point> getPoints() {
        return points;
    }

    public List<Connection> getConnections() {
        return connections;
    }
    public void clearInstance(){
        venues.clear();
        levels.clear();
        points.clear();
        connections.clear();
    }
}
