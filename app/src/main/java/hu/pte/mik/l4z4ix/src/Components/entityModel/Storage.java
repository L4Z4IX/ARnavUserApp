package hu.pte.mik.l4z4ix.src.Components.entityModel;

import java.util.ArrayList;
import java.util.List;

public class Storage {
    private List<Venue> venues = new ArrayList<>();
    private List<Level> levels = new ArrayList<>();
    private List<Connection> connections = new ArrayList<>();

    public static final Storage INSTANCE = new Storage();

    private Storage() {

    }

    public void setVenues(List<Venue> venues) {
        this.venues = venues;
    }

    public void setLevels(List<Level> levels) {
        this.levels = levels;
    }


    public void setConnections(List<Connection> connections) {
        this.connections = connections;
    }

    public List<Venue> getVenues() {
        return venues;
    }

    public List<Level> getLevels() {
        return levels;
    }


    public List<Connection> getConnections() {
        return connections;
    }

    public void clearInstance() {
        venues.clear();
        levels.clear();
        connections.clear();
    }
}
