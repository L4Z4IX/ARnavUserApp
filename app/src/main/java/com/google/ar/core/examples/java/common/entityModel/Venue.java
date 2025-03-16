package com.google.ar.core.examples.java.common.entityModel;

import java.util.ArrayList;

public class Venue {
    private Integer id;
    private String name;
    private ArrayList<Level> levelSet;

    public Venue(Integer id,String name,ArrayList<Level>levelSet){
        this.id=id;
        this.name=name;
        this.levelSet=levelSet;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
