package hu.pte.mik.l4z4ix.src.common.dto;

import hu.pte.mik.l4z4ix.src.common.entityModel.Point;

public class PointDTOs {
    public record addPointDTO(double x, double y, double z, String name, Long levelId) {
    }

    public record editPointDTO(Point point, Long levelId) {
    }

    public record deletePointDTO(Long pointId) {
    }
}
