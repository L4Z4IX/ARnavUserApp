package hu.pte.mik.l4z4ix.src.Components.entityModel;


import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Level {
    private final Long id;

    private final String name;

    private final ArrayList<Point> points;
    public static final TypeToken<List<Level>> LIST_TYPE_TOKEN = new TypeToken<List<Level>>() {
    };


    public Level(Long id, String name, ArrayList<Point> points) {
        this.id = id;
        this.name = name;
        this.points = points;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<Point> getPoints() {
        return points;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Level level = (Level) o;
        return Objects.equals(id, level.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
