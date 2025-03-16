package com.google.ar.core.examples.java.common.entityModel;


import java.util.ArrayList;

public class Level {
    private Integer id;

    private String name;

    private ArrayList<Point> pointSet;

    public Level(Integer id, String name, ArrayList<Point> pointSet){
        this.id=id;
        this.name=name;
        this.pointSet=pointSet;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Point> getPointSet() {
        return pointSet;
    }
}
