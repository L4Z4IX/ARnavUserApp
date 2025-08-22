package com.google.ar.core.examples.java.common.dto;

public class ConnectionDTOs {
    public record addConnectionDTO(Long pointId1,
                                   Long pointId2) {
    }

    public record delConnectionDTO(Long pointId1, Long pointId2) {
    }
}
