package hu.pte.mik.l4z4ix.src.common.listHelpers;

import hu.pte.mik.l4z4ix.src.common.entityModel.Point;

public interface PointFormHandler extends FormHandler<Point> {
    void onManageConnectionClick(Point item);
}
