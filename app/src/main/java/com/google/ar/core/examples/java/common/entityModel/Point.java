package com.google.ar.core.examples.java.common.entityModel;

public class Point {
    private Integer id;
    private String name;
    private double x, y, z;

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
    public boolean equals(Object o){
        if(this==o)
            return true;
        if(o==null||this.getClass()!=o.getClass())
            return false;

        return ((Point)(o)).id.equals(this.id);
    }
    @Override
    public int hashCode(){
        return id;
    }
}
