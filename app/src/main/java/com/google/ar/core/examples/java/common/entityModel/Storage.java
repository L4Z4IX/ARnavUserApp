package com.google.ar.core.examples.java.common.entityModel;

import java.util.ArrayList;

public class Storage {
    private ArrayList<Venue> venues=new ArrayList<>();
    private ArrayList<Level> levels=new ArrayList<>();
    private ArrayList<Point> points=new ArrayList<>();
    private ArrayList<Connection> connections=new ArrayList<>();

    public static final Storage INSTANCE=new Storage();

    private Storage(){

    }
    public void setVenues(ArrayList<Venue> venues){
        this.venues=venues;
    }

    public void setLevels(ArrayList<Level> levels) {
        this.levels = levels;
    }

    public void setPoints(ArrayList<Point> points) {
        this.points = points;
    }

    public void setConnections(ArrayList<Connection> connections) {
        this.connections = connections;
    }

    public ArrayList<Venue> getVenues() {
        return venues;
    }

    public ArrayList<Level> getLevels() {
        return levels;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    public ArrayList<Connection> getConnections() {
        return connections;
    }
    public void clearInstance(){
        venues.clear();
        levels.clear();
        points.clear();
        connections.clear();
    }
}
