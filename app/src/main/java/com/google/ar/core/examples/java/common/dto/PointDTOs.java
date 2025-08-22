package com.google.ar.core.examples.java.common.dto;

import com.google.ar.core.examples.java.common.entityModel.Point;

public class PointDTOs {
    public record addPointDTO(double x, double y, double z, String name, Long levelId) {
    }

    public record editPointDTO(Point point, Long levelId) {
    }

    public record deletePointDTO(Long pointId) {
    }
}
