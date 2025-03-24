package com.google.ar.core.examples.java.common.entityModel;


import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Level {
    private final Integer id;

    private final String name;

    private final ArrayList<Point> pointSet;
    public static final TypeToken<List<Level>> LIST_TYPE_TOKEN = new TypeToken<List<Level>>() {
    };


    public Level(Integer id, String name, ArrayList<Point> pointSet) {
        this.id = id;
        this.name = name;
        this.pointSet = pointSet;
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
