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
package org.mapton.butterfly_geo.inclinometer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.RigidShape;
import gov.nasa.worldwind.render.Wedge;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.butterfly_core.api.sos.ScalePlot3dPSosi;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final InclinoAttributeManager mAttributeManager = InclinoAttributeManager.getInstance();
    private final double mHeightMargin = 2.0;
    private final double mHeightOffset = 25.0;
    private int mScale3dP;
    private double mScaleIndicatorSize;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BGeoInclinometerPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (p.ext().getObservationsTimeFiltered().isEmpty()) {
            return;
        }
        sMapObjects = mapObjects;
        mScale3dP = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dPSosi.class, ScalePlot3dPSosi.DEFAULT_VALUE);
        mScaleIndicatorSize = 0.010 * mScale3dP;

        if (sCheckModel.isChecked(GraphicRendererItem.AXIS)) {
            plotAxis(p, position);
        }
        if (sCheckModel.isChecked(GraphicRendererItem.WEDGE)) {
            plotWedge(p, position);
        }
        if (sCheckModel.isChecked(GraphicRendererItem.CIRCLE)) {
            plotCircle(p, position);
        }
        if (sCheckModel.isChecked(GraphicRendererItem.PATH)) {
            plotPath(p, position);
        }
        if (sCheckModel.isChecked(GraphicRendererItem.VALUE)) {
            plotValue(p, position);
        }
    }

    private ArrayList<Position> createPoints(BGeoInclinometerPoint p, Position position) {
        var positions = new ArrayList<Position>();
        positions.add(WWHelper.positionFromPosition(position, mHeightOffset));
        for (var item : p.ext().getObservationFilteredLast().getObservationItems()) {
            var distance = mScale3dP * item.getDistance();
            var azimuth = item.getAzimuth();
            if (p.getAzimuth() != null) {
                azimuth = Angle.normalizedDegrees(azimuth + p.getAzimuth());
            }

            var position2 = position;
            if (distance > 0) {
                position2 = WWHelper.movePolar(position, azimuth, distance);
            }

            positions.add(WWHelper.positionFromPosition(position2, mHeightOffset + item.getDown()));
        }

        return positions;
    }

    private void plotAxis(BGeoInclinometerPoint p, Position position) {
        Double azimuth = p.getAzimuth();
        if (azimuth != null) {
            azimuth -= 90;
        } else {
            azimuth = 0.0;
        }

        plotAxis(p, position, mScaleIndicatorSize, azimuth);
        plotAxis(p, position, mScaleIndicatorSize, azimuth, mHeightOffset + mHeightMargin);

        double topZ = mHeightOffset + mHeightMargin;
        var path = new Path(WWHelper.positionFromPosition(position, 0), WWHelper.positionFromPosition(position, topZ));
        addRenderable(path, false, null, null);
    }

    private void plotCircle(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.CIRCLE, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var positions = createPoints(p, position);
        plotRod(position, positions);

        for (var node : positions) {
            var h = 2;
            var r = 4;
            var cylinder = new Cylinder(node, h, r);
            cylinder.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(cylinder, true, GraphicRendererItem.CIRCLE.getPlotLimit(), sMapObjects);
        }
    }

    private void plotPath(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.PATH, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var positions = createPoints(p, position);
        plotRod(position, positions);

        for (var node : positions) {
            var r = 0.25;
            var ellipsoid = new Ellipsoid(node, r, r, r);
            ellipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(ellipsoid, true, null, null);
        }

        var path = new Path(positions);
        path.setAttributes(mAttributeManager.getInclinoAttribute());
        addRenderable(path, true, GraphicRendererItem.PATH.getPlotLimit(), sMapObjects);
    }

    private void plotRod(Position position, ArrayList<Position> positions) {
        var topZ = mHeightOffset + mHeightMargin * 0.5;

        var attrs = new BasicShapeAttributes();
        attrs.setDrawOutline(false);
        attrs.setInteriorMaterial(Material.BLACK);
        attrs.setInteriorOpacity(0.2);
        var cylinder = new Cylinder(WWHelper.positionFromPosition(position, topZ / 2), topZ, mScaleIndicatorSize);
        cylinder.setAttributes(attrs);
        addRenderable(cylinder, false, null, null);
    }

    private void plotValue(BGeoInclinometerPoint p, Position position) {
        for (var item : p.ext().getObservationFilteredLast().getObservationItems()) {
            var distance = 1.5 + mScale3dP * item.getDistance();
            var azimuth = item.getAzimuth();
            if (p.getAzimuth() != null) {
                azimuth = Angle.normalizedDegrees(azimuth + p.getAzimuth());
            }

            var position2 = position;
            if (distance > 0) {
                position2 = WWHelper.movePolar(position, azimuth, distance);
            }

            var position3 = WWHelper.positionFromPosition(position2, mHeightOffset + item.getDown());
            var placemark = new PointPlacemark(position3);
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText("%.1f".formatted(item.getDistance() * 1000));
            addRenderable(placemark, false, null, null);
        }
    }

    private void plotWedge(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.WEDGE, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var positions = createPoints(p, position);
        plotRod(position, positions);

        for (var observationItem : p.ext().getObservationFilteredLast().getObservationItems()) {
            var wedgeHeight = 2.0;
            var angle = 45.0;
            var wedgeRadius = mScale3dP * observationItem.getDistance();
            var position2 = WWHelper.positionFromPosition(position, observationItem.getDown() + mHeightOffset);

            RigidShape shape;
            if (wedgeRadius > 0) {
                var bearing = observationItem.getAzimuth();
                if (p.getAzimuth() != null) {
                    bearing = Angle.normalizedDegrees(bearing + p.getAzimuth());
                }
                shape = new Wedge(position2, Angle.fromDegrees(angle), wedgeHeight, wedgeRadius);
                var az = Angle.normalizedDegrees(bearing - angle / 2);
                shape.setHeading(Angle.fromDegrees(az));
            } else {
                var size = 0.25;
                shape = new Box(position2, size, size, size);
            }

            shape.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(shape, true, GraphicRendererItem.WEDGE.getPlotLimit(), sMapObjects);
        }
    }

}
