package com.google.ar.core.examples.java.common.dto;

import com.google.ar.core.examples.java.common.entityModel.Point;

public class PointDTO {
    public final Point point;
    public final Long levelId;

    public PointDTO(Point point, Long levelId) {
        this.point = point;
        this.levelId = levelId;
    }
}
