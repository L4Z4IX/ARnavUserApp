package com.google.ar.core.examples.java.common.entityModel;

public class Point {
    private final Integer id;
    private final String name;
    private final double x;
    private final double y;
    private final double z;

    public Point(Integer id, String name, double x, double y, double z) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || this.getClass() != o.getClass())
            return false;

        return ((Point) (o)).id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return name;
    }
}
