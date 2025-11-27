package hu.pte.mik.l4z4ix.src.Components.httpConnection;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import hu.pte.mik.l4z4ix.src.Components.dto.ConnectionDTOs;
import hu.pte.mik.l4z4ix.src.Components.dto.LevelDTOs;
import hu.pte.mik.l4z4ix.src.Components.dto.Login;
import hu.pte.mik.l4z4ix.src.Components.dto.PointDTOs;
import hu.pte.mik.l4z4ix.src.Components.dto.VenueDTOs;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Connection;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Storage;
import hu.pte.mik.l4z4ix.src.Components.entityModel.Venue;
import okhttp3.Response;

public class DataManager {
    private final Logger logger = Logger.getLogger("Datamanager");
    private final Storage storage = Storage.INSTANCE;
    private final HttpConnectionHandler httpConnectionHandler = HttpConnectionHandler.getInstance();
    private static final String PROTOCOL = "http://";
    private String url = null;
    private static final DataManager INSTANCE = new DataManager();

    private DataManager() {
    }

    public static DataManager getManager() {
        return INSTANCE;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Response doHello(String Url) throws IOException {
        return httpConnectionHandler.doRequest(PROTOCOL + Url + "/data/hello");
    }

    public void requestVenues() throws IOException {
        Response r = httpConnectionHandler.doRequest(PROTOCOL + url + "/data/venues");
        if (!r.isSuccessful()) {
            throw new IOException("Something went wrong. Try again later.");
        }
        try {
            var venues = httpConnectionHandler.getResponseFromJson(r, Venue.LIST_TYPE_TOKEN);
            storage.clearInstance();
            storage.setVenues(venues);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while reading venue response JSON!");
            e.printStackTrace();
            throw new IOException("Something went wrong. Try again later.");
        }
    }

    public void requestVenueData(Integer venueIndex) throws IOException {
        requestVenueData(storage.getVenues().get(venueIndex).getId());
    }

    public void requestVenueData(Long venueId) throws IOException {
        try {
            Response r = httpConnectionHandler.doRequest(PROTOCOL + url + "/data/venuedata/" + venueId);
            if (!r.isSuccessful()) {
                throw new IOException();
            }

            Response r2 = httpConnectionHandler.doRequest(PROTOCOL + url + "/data/connectionsByVenue?venueId=" + venueId);
            if (!r2.isSuccessful()) {
                throw new IOException();
            }
            storage.setLevels(httpConnectionHandler.getResponseFromJson(r, hu.pte.mik.l4z4ix.src.Components.entityModel.Level.LIST_TYPE_TOKEN));
            storage.setConnections(httpConnectionHandler.getResponseFromJson(r2, Connection.LIST_TYPE_TOKEN));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while getting venue data!");
            e.printStackTrace();
            throw new IOException("Something went wrong. Try again later.");
        }
    }

    public void requestConnectionsByVenue(Venue venue) throws IOException {
        try {
            Response r = httpConnectionHandler.doRequest(PROTOCOL + url + "/data/connectionsByVenue?venueId=" + venue.getId());
            if (!r.isSuccessful()) {
                throw new IOException();
            }
            storage.setConnections(httpConnectionHandler.getResponseFromJson(r, Connection.LIST_TYPE_TOKEN));
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while fetching connection data!");
            e.printStackTrace();
            throw new IOException("Error while fetching connection data!");
        }
    }


    public String doLogin(Login credentials) throws IOException {
        try {
            Response r1 = httpConnectionHandler.doRequest(PROTOCOL + url + "/data/hello");
            if (!r1.isSuccessful()) {
                throw new IOException("Invalid url");
            }
            String[] data = httpConnectionHandler.getResponseString(r1).split(";");
            Response response;
            response = httpConnectionHandler.doRequest(PROTOCOL + url + "/login", credentials, RequestType.POST);
            if (!response.isSuccessful()) {
                throw new IOException("Invalid credentials");
            }
            return data[0];
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Error while logging in!");
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    public String addVenue(VenueDTOs.addVenueDTO venue) throws IOException {
        return doHttpCall("/admin/venue", venue, "Error while adding venue!", RequestType.POST);
    }

    public String updateVenue(VenueDTOs.setVenueNameDTO venue) throws IOException {
        return doHttpCall("/admin/venue", venue, "Error while updating venue!", RequestType.PATCH);
    }

    public String deleteVenue(VenueDTOs.delVenueDTO venue) throws IOException {
        return doHttpCall("/admin/venue", venue, "Error while removing venue!", RequestType.DELETE);
    }

    public String addLevel(LevelDTOs.addLevelDTO level) throws IOException {
        return doHttpCall("/admin/level", level, "Error while adding level!", RequestType.POST);
    }

    public String updateLevel(LevelDTOs.setLevelNameDTO level) throws IOException {
        return doHttpCall("/admin/level", level, "Error while updating level!", RequestType.PATCH);
    }

    public String deleteLevel(LevelDTOs.delLevelDTO level) throws IOException {
        return doHttpCall("/admin/level", level, "Error while deleting level!", RequestType.DELETE);
    }

    public String addPoint(PointDTOs.addPointDTO point) throws IOException {
        return doHttpCall("/admin/point", point, "Error while adding point!", RequestType.POST);
    }

    public String updatePoint(PointDTOs.editPointDTO point) throws IOException {
        return doHttpCall("/admin/point", point, "Error while updating point!", RequestType.PATCH);
    }

    public String deletePoint(PointDTOs.deletePointDTO point) throws IOException {
        return doHttpCall("/admin/point", point, "Error while deleting point!", RequestType.DELETE);
    }

    public void handleConnection(ConnectionDTOs.connectionDTO connection) throws IOException {
        doHttpCall("/admin/connection", connection, "Error while changing connection!", RequestType.POST);
    }


    private String doHttpCall(String subUrl, Object DTO, String errorMessage, RequestType requestType) throws IOException {
        try {
            Response resp;
            resp = httpConnectionHandler.doRequest(PROTOCOL + url + subUrl, DTO, requestType);
            if (!resp.isSuccessful()) {
                throw new IOException("Response was not successful");
            }
            return httpConnectionHandler.getResponseString(resp);
        } catch (IOException e) {
            logger.log(Level.SEVERE, e.getMessage() + ", " + errorMessage);
            e.printStackTrace();
            throw new IOException(errorMessage);
        }
    }
}
