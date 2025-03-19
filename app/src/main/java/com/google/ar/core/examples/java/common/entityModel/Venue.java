package com.google.ar.core.examples.java.common.entityModel;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private Integer id;
    private String name;
    public static final TypeToken<List<Venue>> LIST_TYPE_TOKEN=new TypeToken<List<Venue>>(){};

    public Venue(Integer id,String name,ArrayList<Level>levelSet){
        this.id=id;
        this.name=name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
