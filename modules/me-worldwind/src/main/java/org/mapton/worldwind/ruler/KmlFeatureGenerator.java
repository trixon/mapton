/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.worldwind.ruler;

import de.micromata.opengis.kml.v_2_2_0.AltitudeMode;
import de.micromata.opengis.kml.v_2_2_0.ColorMode;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.measure.MeasureTool;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.mapton.api.MKmlCreator;

/**
 *
 * @author Patrik Karlström
 */
class KmlFeatureGenerator extends MKmlCreator {

    private final MeasureTool mMeasureTool;
    private final String mTitle;

    KmlFeatureGenerator(String title, MeasureTool measureTool) {
        mTitle = title;
        mMeasureTool = measureTool;
    }

    Feature generate() {
        switch (mMeasureTool.getMeasureShapeType()) {
            case MeasureTool.SHAPE_LINE:
            case MeasureTool.SHAPE_PATH:
                return lineFeature();

            case MeasureTool.SHAPE_POLYGON:
            case MeasureTool.SHAPE_SQUARE:
            case MeasureTool.SHAPE_QUAD:
                return polygonFeature();

            case MeasureTool.SHAPE_CIRCLE:
                return circleFeature();

            case MeasureTool.SHAPE_ELLIPSE:
                return ellipseFeature();

            default:
                return null;
        }

    }

    private Feature circleFeature() {
        Position center = mMeasureTool.getCenterPosition();
        double height = mMeasureTool.getHeight();

        return createCircle(mTitle, createCircle(center.getLatitude().getDegrees(), center.getLongitude().getDegrees(), height, 100), "#FF0000FF");
    }

    private Feature ellipseFeature() {
        throw new UnsupportedOperationException("Ellipse not supported yet.");
    }

    private Feature lineFeature() {
        return createLine(mTitle, getCoordinates(), 0, "#FF00FF00", AltitudeMode.CLAMP_TO_GROUND);
    }

    private Feature polygonFeature() {
        return createPolygon(mTitle, getCoordinates(), 0, "#FF00FF00", ColorMode.NORMAL, AltitudeMode.CLAMP_TO_GROUND);
    }

    private ArrayList<Point3D> getCoordinates() {
        ArrayList<Point3D> coordinates = new ArrayList<>();
        for (Position p : mMeasureTool.getPositions()) {
            Point3D c = new Point3D(p.getLongitude().getDegrees(), p.getLatitude().getDegrees(), 0);

            coordinates.add(c);
        }
        return coordinates;
    }
}
