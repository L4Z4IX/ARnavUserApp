package hu.pte.mik.l4z4ix.src.Components.dto;

public class VenueDTOs {
    public record addVenueDTO(String venueName) {
    }

    public record delVenueDTO(Long venueId) {
    }

    public record setVenueNameDTO(Long venueId, String venueName) {
    }
}
