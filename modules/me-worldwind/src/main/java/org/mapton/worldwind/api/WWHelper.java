/*
 * Copyright 2023 Patrik Karlström.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mapton.worldwind.api;

import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PointPlacemarkAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import org.apache.commons.math3.util.FastMath;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.MPoiStyle.ImageLocation;

/**
 *
 * @author Patrik Karlström
 */
public class WWHelper {

    public static final String KEY_FAST_OPEN = "mapton.layer.fast_open";
    public static final String KEY_HOOVER_TEXT = "mapton.hoover.text";
    public static final String KEY_LAYER_CATEGORY = "mapton.layer.category";
    public static final String KEY_LAYER_HIDE_FROM_MANAGER = "mapton.layer.hide_from_manager";
    public static final String KEY_RUNNABLE_HOOVER = "mapton.runnable.hoover";
    public static final String KEY_RUNNABLE_LEFT_CLICK = "mapton.runnable.left_click";
    public static final String KEY_RUNNABLE_LEFT_DOUBLE_CLICK = "mapton.runnable.left_double_click";

    public static PointPlacemarkAttributes createHighlightAttributes(PointPlacemarkAttributes attrs, double scale) {
        PointPlacemarkAttributes highlightAttrs = new PointPlacemarkAttributes(attrs);
        highlightAttrs.setScale(attrs.getScale() * scale);
        highlightAttrs.setLabelScale(attrs.getLabelScale() * scale);

        return highlightAttrs;
    }

    public static MLatLon latLonFromPosition(Position position) {
        return new MLatLon(
                position.getLatitude().getDegrees(),
                position.getLongitude().getDegrees()
        );
    }

    public static Position movePolar(Position position, double bearing, double distance) {
        var latLon = latLonFromPosition(position).getDestinationPoint(bearing, distance);

        return Position.fromDegrees(latLon.getLatitude(), latLon.getLongitude());
    }

    public static Offset offsetFromImageLocation(ImageLocation imageLocation) {
        switch (imageLocation) {
            case BOTTOM_LEFT:
                return Offset.fromFraction(0.0, 0.0);
            case BOTTOM_CENTER:
                return Offset.fromFraction(0.5, 0.0);
            case BOTTOM_RIGHT:
                return Offset.fromFraction(1.0, 0.0);
            case MIDDLE_LEFT:
                return Offset.fromFraction(0.0, 0.5);
            case MIDDLE_CENTER:
                return Offset.fromFraction(0.5, 0.5);
            case MIDDLE_RIGHT:
                return Offset.fromFraction(1.0, 0.5);
            case TOP_LEFT:
                return Offset.fromFraction(0.0, 1.0);
            case TOP_CENTER:
                return Offset.fromFraction(0.5, 1.0);
            case TOP_RIGHT:
                return Offset.fromFraction(1.0, 1.0);
            default:
                throw new AssertionError();
        }
    }

    public static Position positionFromLatLon(MLatLon latLon) {
        return Position.fromDegrees(latLon.getLatitude(), latLon.getLongitude());
    }

    public static Position positionFromLatLon(MLatLon latLon, double elevation) {
        return Position.fromDegrees(latLon.getLatitude(), latLon.getLongitude(), elevation);
    }

    public static ArrayList<Position> positionsFromGeometry(Geometry geometry, double elevation) {
        var positions = new ArrayList<Position>();

        for (var coordinate : geometry.getCoordinates()) {
            positions.add(Position.fromDegrees(coordinate.y, coordinate.x, elevation));
        }

        return positions;
    }

    public static Sector sectorFromCoordinates(ArrayList<Coordinate> coordinates) {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (var coordinate : coordinates) {
            minX = FastMath.min(minX, coordinate.x);
            minY = FastMath.min(minY, coordinate.y);
            maxX = FastMath.max(maxX, coordinate.x);
            maxY = FastMath.max(maxY, coordinate.y);
        }

        var sector = new Sector(
                Angle.fromDegreesLatitude(minY),
                Angle.fromDegreesLatitude(maxY),
                Angle.fromDegreesLongitude(minX),
                Angle.fromDegreesLongitude(maxX)
        );

        return sector;
    }

    public static Sector sectorFromGeometry(Geometry geometry) {
        return sectorFromCoordinates(new ArrayList<Coordinate>(Arrays.asList(geometry.getCoordinates())));
    }

    public static Sector sectorFromLatLonBox(MLatLonBox latLonBox) {
        return new Sector(
                Angle.fromDegreesLatitude(latLonBox.getSouthWest().getLatitude()),
                Angle.fromDegreesLatitude(latLonBox.getNorthEast().getLatitude()),
                Angle.fromDegreesLongitude(latLonBox.getSouthWest().getLongitude()),
                Angle.fromDegreesLongitude(latLonBox.getNorthEast().getLongitude())
        );
    }

}
