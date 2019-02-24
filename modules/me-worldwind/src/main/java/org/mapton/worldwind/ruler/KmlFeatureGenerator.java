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
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.util.measure.MeasureTool;
import java.util.ArrayList;
import javafx.geometry.Point3D;
import org.mapton.api.MKmlCreator;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class KmlFeatureGenerator extends MKmlCreator {

    private final String mDescription;
    private final MeasureTool mMeasureTool;
    private final String mTitle;

    KmlFeatureGenerator(String title, String description, MeasureTool measureTool) {
        mTitle = title;
        mDescription = description;
        mMeasureTool = measureTool;
    }

    Feature generate() {
        switch (mMeasureTool.getMeasureShapeType()) {
            case MeasureTool.SHAPE_LINE:
            case MeasureTool.SHAPE_PATH:
                return generateLine();

            case MeasureTool.SHAPE_POLYGON:
            case MeasureTool.SHAPE_SQUARE:
            case MeasureTool.SHAPE_QUAD:
                return generatePolygon();

            case MeasureTool.SHAPE_CIRCLE:
                return generateCircle();

            case MeasureTool.SHAPE_ELLIPSE:
                return generateEllipse();

            default:
                return null;
        }
    }

    private Feature generateCircle() {
        Position center = mMeasureTool.getCenterPosition();
        double height = mMeasureTool.getHeight();
        Placemark placemark = createCircle(mTitle, createCircle(center.getLatitude().getDegrees(), center.getLongitude().getDegrees(), height, 100), "#FF0000FF");
        placemark.setDescription(mDescription);

        return placemark;
    }

    private Feature generateEllipse() {
        throw new UnsupportedOperationException("Ellipse export not supported yet.");
    }

    private Feature generateLine() {
        Placemark placemark = createLine(mTitle, getCoordinates(), mMeasureTool.getLineWidth(), getColorLine(), AltitudeMode.CLAMP_TO_GROUND);
        placemark.setDescription(mDescription);

        return placemark;
    }

    private Feature generatePolygon() {
        Placemark placemark = createPolygon(mTitle, getCoordinates(), mMeasureTool.getLineWidth(), getColorLine(), getColorFill(), ColorMode.NORMAL, AltitudeMode.CLAMP_TO_GROUND);
        placemark.setDescription(mDescription);

        return placemark;
    }

    private String getColorFill() {
        return FxHelper.colorToHexABGR(mMeasureTool.getFillColor());
    }

    private String getColorLine() {
        return FxHelper.colorToHexABGR(mMeasureTool.getLineColor());
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
