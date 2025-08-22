package com.google.ar.core.examples.java.common.dto;

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
