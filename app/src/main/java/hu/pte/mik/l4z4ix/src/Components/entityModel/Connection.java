package hu.pte.mik.l4z4ix.src.Components.entityModel;

import com.google.gson.reflect.TypeToken;

import java.util.List;

public class Connection implements Comparable<Connection> {
    private final Long id;

    private final Long point1Id;

    private final Long point2Id;
    private final double distance;
    public static final TypeToken<List<Connection>> LIST_TYPE_TOKEN = new TypeToken<List<Connection>>() {
    };

    public Connection(Long id, long point1Id, long point2Id, double distance) {
        this.id = id;
        this.point1Id = point1Id;
        this.point2Id = point2Id;
        this.distance = distance;
    }

    public Long getId() {
        return id;
    }

    public Long getPoint1Id() {
        return point1Id;
    }

    public Long getPoint2Id() {
        return point2Id;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int compareTo(Connection o) {
        return (int) (this.distance - o.distance);
    }

}
