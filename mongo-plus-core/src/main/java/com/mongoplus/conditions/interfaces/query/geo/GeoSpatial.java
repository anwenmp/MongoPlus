package com.mongoplus.conditions.interfaces.query.geo;

import com.mongoplus.conditions.interfaces.query.geo.operations.*;

/**
 * 地理空间
 *
 * @author anwen
 */
public interface GeoSpatial<T, Children> extends
        GeoIntersects<T, Children>,
        GeoWithin<T, Children>,
        GeoWithinBox<T, Children>,
        GeoWithinCenter<T, Children>,
        GeoWithinCenterSphere<T, Children>,
        GeoWithinPolygon<T, Children>,
        Near<T, Children>,
        NearSphere<T, Children> {
}
