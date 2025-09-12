/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_geo_extensometer;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.function.Function;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final ExtensoAttributeManager mAttributeManager = ExtensoAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mLayer;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        mLayer = layer;
        mCheckModel = checkModel;
    }

    public void plot(BGeoExtensometer extenso, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicRendererItem.INDICATORS)) {
            plotIndicators(extenso, position);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.LABEL_ALARM_LEVELS)) {
            plotLabelsAlarm(extenso.ext().getReferencePoint());
            plotLabelsAlarm(extenso);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.LABEL_DELTA_Z)) {
            plotLabelsDeltaZ(extenso.ext().getReferencePoint());
            plotLabelsDeltaZ(extenso);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.LABEL_DEPTH)) {
            plotLabelsDepth(extenso.ext().getReferencePoint());
            plotLabelsDepth(extenso);
        }
    }

    private void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            mLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    private void plotIndicators(BGeoExtensometer extenso, Position position) {
        var scale = 2d;
        var ground = 1 * scale + Math.abs(ExtensoManager.getInstance().getMinimumDepth() * scale);
        if (Double.isInfinite(ground)) {
            return;
        }
        var shapeSize = 2.0;

        var p0 = WWHelper.positionFromPosition(position, 0.0);
        var p1 = WWHelper.positionFromPosition(position, ground);
        var path = new Path(p0, p1);
        path.setAttributes(mAttributeManager.getGroundConnectorAttributes());
        addRenderable(path, true);

        var groundCylinder = new Cylinder(p1, 0.2, shapeSize);
        groundCylinder.setAttributes(mAttributeManager.getComponentZeroAttributes());

        addRenderable(groundCylinder, true);

        var indicatorStep = 10.0 * scale;
        var indicatorAltitude = ground - indicatorStep;
        var indicatorAttributes = mAttributeManager.getAlarmInteriorAttributes(-1);
        indicatorAttributes.setInteriorOpacity(0.10);

        while (indicatorAltitude > 0) {
            var indicatorPos = WWHelper.positionFromPosition(position, indicatorAltitude);
            var indicateCylinder = new Cylinder(indicatorPos, 0.05, shapeSize * .75);
            indicateCylinder.setAttributes(indicatorAttributes);
            addRenderable(indicateCylinder, true);
            indicatorAltitude -= indicatorStep;
        }

        if (extenso.ext().getReferencePoint() != null) {
            var p = extenso.ext().getReferencePoint();
            var pos = WWHelper.positionFromPosition(position, ground + 1);
            p.setValue(Position.class, pos);

            var pyramid = new Pyramid(WWHelper.positionFromPosition(position, ground + 2), shapeSize * 1.0, shapeSize);
            var attrs = mAttributeManager.getAlarmInteriorAttributes(TopoHelper.getAlarmLevelHeight(p));
//            var attrs = TopoAttributeManager.getInstance().getComponentmAlarmLevelTrace1dAttributes();
//            var attrs = mAttributeManager.getComponentAlarmAttributes(p.ext().getAlarmLevel());

            pyramid.setAttributes(attrs);
//            point.setValue(Position.class, p);
            var lastObservation = p.ext().getObservationFilteredLast();
            if (lastObservation != null && lastObservation.ext().getDelta() < 0) {
                pyramid.setRoll(Angle.POS180);
            }

            addRenderable(pyramid, true);

        }

        for (var point : extenso.getPoints()) {
            if (point.ext().getObservationsTimeFiltered().isEmpty()) {
                continue;
            }

            var lastObservation = point.ext().getObservationFilteredLast();
            var depth = ground + point.getDepth() * scale;
            var p = WWHelper.positionFromPosition(position, depth);
            var attrs = mAttributeManager.getComponentAlarmAttributes(point.ext().getAlarmLevel());

            if (extenso.ext().getMeasurementUntilNext(ChronoUnit.DAYS) < 0) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.2);
            }

            var pyramid = new Pyramid(p, shapeSize * 1.0, shapeSize);
            pyramid.setAttributes(attrs);
            point.setValue(Position.class, p);
            if (lastObservation.ext().getDelta() < 0) {
                pyramid.setRoll(Angle.POS180);
            }

            addRenderable(pyramid, true);
        }
    }

    private void plotLabel(BGeoExtensometer extenso, Function<BGeoExtensometerPoint, String> function, double offset) {
        extenso.getPoints().forEach(p -> {
            var position = p.<Position>getValue(Position.class);
            var placemark = new PointPlacemark(WWHelper.positionFromPosition(position, position.elevation + offset));
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText("•    " + function.apply(p));
            placemark.setAlwaysOnTop(true);
            addRenderable(placemark, true);
        });
    }

    private void plotLabel(BTopoControlPoint p, Function<BTopoControlPoint, String> function, double offset) {
        if (p == null) {
            return;
        }
        var position = p.<Position>getValue(Position.class);
        var placemark = new PointPlacemark(WWHelper.positionFromPosition(position, position.elevation + offset));
        placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
        placemark.setAltitudeMode(WorldWind.ABSOLUTE);
        placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
        placemark.setLabelText("•    " + function.apply(p));
        placemark.setAlwaysOnTop(true);
        addRenderable(placemark, true);
    }

    private void plotLabelsAlarm(BGeoExtensometer extenso) {
        var function = (Function<BGeoExtensometerPoint, String>) p -> "%.1f  %.1f  %.1f".formatted(
                p.getLimit1() * 1000,
                p.getLimit2() * 1000,
                p.getLimit3() * 1000
        );
        plotLabel(extenso, function, .75);
    }

    private void plotLabelsAlarm(BTopoControlPoint point) {
        var function = (Function<BTopoControlPoint, String>) p -> AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
        plotLabel(point, function, .75);
    }

    private void plotLabelsDeltaZ(BGeoExtensometer extenso) {
        var function = (Function<BGeoExtensometerPoint, String>) p -> "%.2f".formatted(p.ext().getDelta());
        plotLabel(extenso, function, 0);
    }

    private void plotLabelsDeltaZ(BTopoControlPoint point) {
        var function = (Function<BTopoControlPoint, String>) p -> p.ext().deltaZero().getDelta1(3);
        plotLabel(point, function, 0);
    }

    private void plotLabelsDepth(BGeoExtensometer extenso) {
        var function = (Function<BGeoExtensometerPoint, String>) p -> "%.1f".formatted(p.getDepth());
        plotLabel(extenso, function, -.75);
    }

    private void plotLabelsDepth(BTopoControlPoint point) {
        var function = (Function<BTopoControlPoint, String>) p -> "%.1f".formatted(p.getZeroZ());
        plotLabel(point, function, -.75);
    }

    private void plotSlice(BGeoExtensometer extenso, Position position) {
        int numOfSlices = extenso.getPoints().size();

        for (int i = 0; i < extenso.getPoints().size(); i++) {
            var point = extenso.getPoints().get(i);
            var angle = 360.0 / numOfSlices;

            var reversedList = point.ext().getObservationsTimeFiltered().reversed();
            var prevDate = LocalDateTime.now();
            var altitude = 0.0;
            var prevHeight = 0.0;

            for (int j = 0; j < reversedList.size(); j++) {
                var o = reversedList.get(j);

                var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
                var height = timeSpan / 24000.0;
                altitude = altitude + height * 0.5 + prevHeight * 0.5;
                prevDate = o.getDate();
                prevHeight = height;

                var delta = o.ext().getDelta();
                if (delta == null) {
                    continue;
                }

                var maxRadius = 50.0;
                var radius = Math.min(maxRadius, Math.abs(delta) / 20 + 0.05);
                var maximus = radius == maxRadius;

                var cappedCylinder = new PartialCappedCylinder(position, radius,
                        Angle.fromDegrees(i * angle),
                        Angle.fromDegrees(angle * (i + 1))
                );
                var halfHeight = height / 2.0;
                cappedCylinder.setAltitudes(altitude - halfHeight, altitude + halfHeight);
                var alarmLevel = point.ext().getAlarmLevel(o);
                var rise = Math.signum(delta) > 0;
                var attrs = mAttributeManager.getComponentTraceAttributes(alarmLevel, rise, maximus);

                if (j == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                    attrs = new BasicShapeAttributes(attrs);
                    attrs.setInteriorOpacity(0.25);
                    attrs.setOutlineOpacity(0.20);
                }

                cappedCylinder.setAttributes(attrs);
                addRenderable(cappedCylinder, true);
            }
        }
    }

    private void plotTrace(BGeoExtensometer extenso) {
        int numOfSlices = extenso.getPoints().size();

        for (int i = 0; i < extenso.getPoints().size(); i++) {
            var point = extenso.getPoints().get(i);
            var angle = 360.0 / numOfSlices;

            var reversedList = point.ext().getObservationsTimeFiltered().reversed();
            var prevDate = LocalDateTime.now();
            var altitude = 0.0;
            var prevHeight = 0.0;

            var latLon = new MLatLon(extenso.getLat(), extenso.getLon());

//            if (mCheckModel.isChecked(GraphicRendererItem.TRACE_LABEL)) {
//                var labelPosition = WWHelper.positionFromLatLon(latLon.getDestinationPoint(angle * i, 8));
//                var placemark = new PointPlacemark(labelPosition);
//                placemark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
//                placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
//                placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
//                var label = StringUtils.remove(point.getName(), extenso.getName());
//                label = StringUtils.removeStart(label, "-");
//                placemark.setLabelText(label);
//                addRenderable(placemark, true);
//            }
            for (int j = 0; j < reversedList.size(); j++) {
                var o = reversedList.get(j);

                var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
                var height = timeSpan / 24000.0;
                altitude = altitude + height * 0.5 + prevHeight * 0.5;
                prevDate = o.getDate();
                prevHeight = height;

                var delta = o.ext().getDelta();
                if (delta == null) {
                    continue;
                }

                var maxRadius = 50.0;
                var radius = Math.min(maxRadius, Math.abs(delta) / 10 + 0.05);
                var maximus = radius == maxRadius;

                var pos = WWHelper.positionFromLatLon(latLon.getDestinationPoint(angle * i, 6), altitude);
                var cylinder = new Cylinder(pos, height, radius);
                var alarmLevel = point.ext().getAlarmLevel(o);
                var rise = Math.signum(delta) > 0;
                var attrs = mAttributeManager.getComponentTraceAttributes(alarmLevel, rise, maximus);

                if (j == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                    attrs = new BasicShapeAttributes(attrs);
                    attrs.setInteriorOpacity(0.25);
                    attrs.setOutlineOpacity(0.20);
                }

                cylinder.setAttributes(attrs);
                addRenderable(cylinder, true);
            }
        }
    }
}
