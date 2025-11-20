package hu.pte.mik.l4z4ix.src.Components.dto;

public class LevelDTOs {
    public record addLevelDTO(String levelName,
                              Long venueId
    ) {
    }

    public record delLevelDTO(Long id) {
    }

    public record setLevelNameDTO(String levelName,
                                  Long id
    ) {
    }
}
