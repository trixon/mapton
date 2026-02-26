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
package org.mapton.butterfly_geo.inclinometer.graphics;

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
import gov.nasa.worldwind.render.SurfaceCircle;
import java.util.ArrayList;
import java.util.List;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.butterfly_core.api.sos.ScalePlot3dPSosi;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.butterfly_geo.inclinometer.InclinoAttributeManager;
import org.mapton.butterfly_geo.inclinometer.InclinoHelper;
import org.mapton.worldwind.api.CylinderWithOffset;
import org.mapton.worldwind.api.WWHelper;
import org.mapton.worldwind.api.WedgeWithOffset;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final InclinoAttributeManager mAttributeManager = InclinoAttributeManager.getInstance();
    private final double mHeightMargin = 2.0;
    private double mHeightOffset = 25.0;
    private int mScale3dP;
    private double mScaleIndicatorSize;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    @Override
    public void plot(BGeoInclinometerPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        mHeightOffset = Math.abs(mOffsetManager.getMinZ());
        sMapObjects = mapObjects;
        mScale3dP = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dPSosi.class, ScalePlot3dPSosi.DEFAULT_VALUE);
        mScaleIndicatorSize = 0.010 * mScale3dP;

        if (sCheckModel.isChecked(GraphicItem.AXIS)) {
            plotAxis(p, position);
        }

        if (p.ext().getObservationsTimeFiltered().isEmpty()) {
//            return;
        }

        if (sCheckModel.isChecked(GraphicItem.WEDGE)) {
            plotWedge(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.AREA)) {
            plotArea(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.WEDGE_AB)) {
            plotWedgeAB(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.CIRCLES)) {
            plotCircle(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.PATH)) {
            plotPath(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.VALUE)) {
            plotValue(p, position);
        }
    }

    private void plotArea(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.AREA, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var r = p.ext().getObservationFilteredLast().getObservationItems().stream()
                .mapToDouble(item -> Math.abs(item.getDown()))
                .max()
                .orElse(1.0);

        var circle = new SurfaceCircle(position, r);
        var attrs = mAttributeManager.getGroundSurfaceAttributes();
        circle.setAttributes(attrs);

        addRenderable(circle, false, GraphicItem.AREA, null);
    }

    private void plotAxis(BGeoInclinometerPoint p, Position position) {
        Double azimuth = p.getAzimuth();
        if (azimuth != null) {
            azimuth += 90;
        } else {
            azimuth = 0.0;
        }

        plotAxis(p, position, mScaleIndicatorSize, azimuth);
        plotAxis(p, position, mScaleIndicatorSize, azimuth, mHeightOffset + mHeightMargin);

        double topZ = mHeightOffset + mHeightMargin;
        var path = new Path(WWHelper.positionFromPosition(position, 0), WWHelper.positionFromPosition(position, topZ));
        addRenderable(path, false, null, null);

        plotRod(position);
    }

    private void plotCircle(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.CIRCLES, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var observationItems = p.ext().getObservationFilteredLast().getObservationItems().reversed();
        if (observationItems.size() < 2) {
            return;
        }

        for (int i = 0; i < observationItems.size(); i++) {
            var item = observationItems.get(i);
            Double pHeight = null;
            Double nHeight = null;
            var down = item.getDown();

            if (i > 0) {
                var pItem = observationItems.get(i - 1);
                var pDown = pItem.getDown();
                pHeight = (down - pDown) * .5;
            }

            if (i < observationItems.size() - 1) {
                var nItem = observationItems.get(i + 1);
                var nDown = nItem.getDown();
                nHeight = Math.abs((nDown - down) * .5);
            }

            if (pHeight == null) {
                pHeight = nHeight;
            } else if (nHeight == null) {
                nHeight = pHeight;
            }

            pHeight = pHeight / 2.0;
            nHeight = nHeight / 2.0;

            var distance = mScale3dP * item.getDistance();
            var azimuth = item.getAzimuth();
            if (p.getAzimuth() != null) {
                azimuth = Angle.normalizedDegrees(azimuth + p.getAzimuth());
            }

            var position2 = position;
            if (distance > 0) {
                position2 = WWHelper.movePolar(position, azimuth, distance);
            }

            var visualPosition = WWHelper.positionFromPosition(position2, mHeightOffset + down);

            try {
                var cylinder = new CylinderWithOffset(visualPosition, nHeight, pHeight, mScaleIndicatorSize);
                var alarmLevel = p.ext().getAlarmLevel(BComponent.HEIGHT, distance);

                cylinder.setAttributes(mAttributeManager.getSurfaceAttributes(alarmLevel));
                addRenderable(cylinder, true, GraphicItem.CIRCLES.getPlotLimit(), sMapObjects);
            } catch (Exception e) {
            }
        }
    }

    private void plotPath(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.PATH, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

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

            var visualPosition = WWHelper.positionFromPosition(position2, mHeightOffset + item.getDown());
            positions.add(visualPosition);

            var r = 0.25;
            var ellipsoid = new Ellipsoid(visualPosition, r, r, r);
            var alarmLevel = p.ext().getAlarmLevel(BComponent.HEIGHT, distance);
            ellipsoid.setAttributes(mAttributeManager.getSurfaceAttributes(alarmLevel));

            addRenderable(ellipsoid, true, null, null);
        }

        var path = new Path(positions);
        path.setAttributes(mAttributeManager.getComponentVector3dAttributes(InclinoHelper.getAlarmLevel(p)));
        addRenderable(path, true, GraphicItem.PATH.getPlotLimit(), sMapObjects);
    }

    private void plotRod(Position position) {
        var topZ = mHeightOffset + mHeightMargin * 0.5;
        var attrs = new BasicShapeAttributes();
        attrs.setDrawOutline(false);
        attrs.setInteriorMaterial(Material.BLACK);
        attrs.setInteriorOpacity(0.1);
        var cylinder = new Cylinder(WWHelper.positionFromPosition(position, topZ / 2), topZ, mScaleIndicatorSize);
        cylinder.setAttributes(attrs);
        addRenderable(cylinder, false, null, null);
    }

    private void plotValue(BGeoInclinometerPoint p, Position position) {
        if (p.ext().getObservationFilteredLast() == null) {
            return;
        }

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
            placemark.setLabelText("%.0f @ %.1f".formatted(item.getDistance() * 1000, item.getDown()));
            addRenderable(placemark, false, null, null);
        }
    }

    private void plotWedge(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.WEDGE, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var observationItems = p.ext().getObservationFilteredLast().getObservationItems().reversed();
        if (observationItems.size() < 2) {
            return;
        }

        for (int i = 0; i < observationItems.size(); i++) {
            var item = observationItems.get(i);
            Double pHeight = null;
            Double nHeight = null;
            var down = item.getDown();

            if (i > 0) {
                var pItem = observationItems.get(i - 1);
                var pDown = pItem.getDown();
                pHeight = (down - pDown) * .5;
            }

            if (i < observationItems.size() - 1) {
                var nItem = observationItems.get(i + 1);
                var nDown = nItem.getDown();
                nHeight = Math.abs((nDown - down) * .5);
            }

            if (pHeight == null) {
                pHeight = nHeight;
            } else if (nHeight == null) {
                nHeight = pHeight;
            }

            var distance = mScale3dP * item.getDistance();
            var azimuth = item.getAzimuth();
            if (p.getAzimuth() != null) {
                azimuth = Angle.normalizedDegrees(azimuth + p.getAzimuth());
            }

            var visualPosition = WWHelper.positionFromPosition(position, mHeightOffset + down);

            try {
                var angle = 5.0;
                RigidShape shape;
                if (distance > 0) {
                    shape = new WedgeWithOffset(Angle.fromDegrees(angle), visualPosition, nHeight, pHeight, distance);
                    var az = Angle.normalizedDegrees(azimuth - angle / 2);
                    shape.setHeading(Angle.fromDegrees(az));
                } else {
                    var size = 0.25;
                    shape = new Box(visualPosition, size, size, size);
                }

                shape.setAttributes(mAttributeManager.getSurfaceAttributes(InclinoHelper.getAlarmLevel(p)));
                addRenderable(shape, true, GraphicItem.WEDGE.getPlotLimit(), sMapObjects);
            } catch (Exception e) {
            }
        }
    }

    private void plotWedgeAB(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.WEDGE_AB, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var observationItems = p.ext().getObservationFilteredLast().getObservationItems().reversed();
        if (observationItems.size() < 2) {
            return;
        }

        for (int i = 0; i < observationItems.size(); i++) {
            var item = observationItems.get(i);
            Double pHeight = null;
            Double nHeight = null;
            var down = item.getDown();

            if (i > 0) {
                var pItem = observationItems.get(i - 1);
                var pDown = pItem.getDown();
                pHeight = (down - pDown) * .5;
            }

            if (i < observationItems.size() - 1) {
                var nItem = observationItems.get(i + 1);
                var nDown = nItem.getDown();
                nHeight = Math.abs((nDown - down) * .5);
            }

            if (pHeight == null) {
                pHeight = nHeight;
            } else if (nHeight == null) {
                nHeight = pHeight;
            }

            for (var axis : List.of("A", "B")) {
                Double distance;
                Double azimuth = p.getAzimuth() == null ? 0 : p.getAzimuth();
                Double value;

                if (axis.equalsIgnoreCase("A")) {
                    value = item.getA();
                } else {
                    value = item.getB();
                    azimuth += 90;
                }

                distance = mScale3dP * value;
                if (distance < 0) {
                    azimuth += 180;
                }

                azimuth = Angle.normalizedDegrees(azimuth);
                if (azimuth < 0) {
                    azimuth += 360;
                }

                var visualPosition = WWHelper.positionFromPosition(position, mHeightOffset + down);
                try {
                    var angle = 1.0;
                    RigidShape shape;
                    distance = Math.abs(distance);
                    if (distance > 0) {
                        shape = new WedgeWithOffset(Angle.fromDegrees(angle), visualPosition, nHeight, pHeight, distance);
                        var az = Angle.normalizedDegrees(azimuth - angle / 2);
                        shape.setHeading(Angle.fromDegrees(az));
                    } else {
                        var size = 0.25;
                        shape = new Box(visualPosition, size, size, size);
                    }

                    var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes(InclinoHelper.getAlarmLevel(p)));
                    attrs.setInteriorOpacity(0.5);
                    shape.setAttributes(attrs);
                    addRenderable(shape, true, GraphicItem.WEDGE.getPlotLimit(), sMapObjects);
                } catch (Exception e) {
                }

                if (sCheckModel.isChecked(GraphicItem.VALUE_AB)) {
                    var position2 = WWHelper.movePolar(visualPosition, azimuth, distance + 2, visualPosition.getAltitude());
                    var placemark = new PointPlacemark(position2);
                    placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
                    placemark.setAltitudeMode(WorldWind.ABSOLUTE);
                    placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
                    placemark.setLabelText("%.0f @ %.1f".formatted(value * 1000, item.getDown()));
                    addRenderable(placemark, false, null, null);
                }
            }
        }
    }

}
