package com.google.ar.core.examples.java.common.entityModel;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Connection implements Comparable<Connection> {
    private final Integer id;

    private final Integer fromId;

    private final Integer toId;
    private final double distance;
    public static final TypeToken<List<Connection>> LIST_TYPE_TOKEN = new TypeToken<List<Connection>>() {
    };

    public Connection(Integer id, Integer fromId, Integer toId, double distance) {
        this.id = id;
        this.fromId = fromId;
        this.toId = toId;
        this.distance = distance;
    }

    public Integer getId() {
        return id;
    }

    public Integer getFromId() {
        return fromId;
    }

    public Integer getToId() {
        return toId;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Connection o) {
        return (int) (this.distance - o.distance);
    }
     
}
