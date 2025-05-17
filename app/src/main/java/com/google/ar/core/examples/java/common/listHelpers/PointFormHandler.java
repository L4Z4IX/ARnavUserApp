package com.google.ar.core.examples.java.common.listHelpers;

import com.google.ar.core.examples.java.common.entityModel.Point;

public interface PointFormHandler extends FormHandler<Point> {
    void onManageConnectionClick(Point item);
}
