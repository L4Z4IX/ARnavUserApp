package com.google.ar.core.examples.java.common.entityModel;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private final Integer id;
    private final String name;
    public static final TypeToken<List<Venue>> LIST_TYPE_TOKEN = new TypeToken<List<Venue>>() {
    };

    public Venue(Integer id, String name, ArrayList<Level> levelSet) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
