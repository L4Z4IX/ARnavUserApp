package hu.pte.mik.l4z4ix.src.common.dto;

public class ConnectionDTOs {
    public record addConnectionDTO(Long pointId1,
                                   Long pointId2) {
    }

    public record delConnectionDTO(Long pointId1, Long pointId2) {
    }
}
