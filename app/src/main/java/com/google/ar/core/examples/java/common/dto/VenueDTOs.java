package com.google.ar.core.examples.java.common.dto;

public class VenueDTOs {
    public record addVenueDTO(String venueName) {
    }

    public record delVenueDTO(Long venueId) {
    }

    public record setVenueNameDTO(Long venueId, String venueName) {
    }
}
