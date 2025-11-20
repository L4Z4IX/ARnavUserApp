package hu.pte.mik.l4z4ix.src.Components.entityModel;

import androidx.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Venue {
    private final Long id;
    private final String name;
    public static final TypeToken<List<Venue>> LIST_TYPE_TOKEN = new TypeToken<List<Venue>>() {
    };

    public Venue(Long id, String name, ArrayList<Level> levelSet) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
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
