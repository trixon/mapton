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
package org.mapton.butterfly_rock_extensometer.graphics;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.PointPlacemark;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.RigidShape;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.function.Function;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.rock.BRockExtensometer;
import org.mapton.butterfly_format.types.rock.BRockExtensometerPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_rock_extensometer.ExtensoAttributeManager;
import org.mapton.butterfly_rock_extensometer.ExtensoManager;
import org.mapton.butterfly_topo.TopoHelper;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final ExtensoAttributeManager mAttributeManager = ExtensoAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicItem> mCheckModel;
    private ArrayList<AVListImpl> mMapObjects;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        mCheckModel = checkModel;
    }

    @Override
    public void plot(BRockExtensometer extenso, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicItem.INDICATORS)) {
            plotIndicators(extenso, position);

            if (mCheckModel.isChecked(GraphicItem.LABEL_ALARM_LEVELS)) {
                plotLabelsAlarm(extenso.ext().getReferencePoint());
                plotLabelsAlarm(extenso);
            }

            if (mCheckModel.isChecked(GraphicItem.LABEL_DELTA_Z)) {
                plotLabelsDeltaZ(extenso.ext().getReferencePoint());
                plotLabelsDeltaZ(extenso);
            }

            if (mCheckModel.isChecked(GraphicItem.LABEL_DEPTH)) {
                plotLabelsDepth(extenso.ext().getReferencePoint());
                plotLabelsDepth(extenso);
            }
        }
    }

    private void plotIndicators(BRockExtensometer extenso, Position position) {
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
        addRenderable(path, true, null, mMapObjects);

        var groundCylinder = new Cylinder(p1, 0.2, shapeSize);
        groundCylinder.setAttributes(mAttributeManager.getComponentZeroAttributes());

        addRenderable(groundCylinder, true, null, mMapObjects);

        var indicatorStep = 10.0 * scale;
        var indicatorAltitude = ground - indicatorStep;
        var indicatorAttributes = mAttributeManager.getAlarmInteriorAttributes(-1);
        indicatorAttributes.setInteriorOpacity(0.10);

        while (indicatorAltitude > 0) {
            var indicatorPos = WWHelper.positionFromPosition(position, indicatorAltitude);
            var indicateCylinder = new Cylinder(indicatorPos, 0.05, shapeSize * .75);
            indicateCylinder.setAttributes(indicatorAttributes);
            addRenderable(indicateCylinder, true, null, mMapObjects);
            indicatorAltitude -= indicatorStep;
        }

        if (extenso.ext().getReferencePoint() != null) {
            var p = extenso.ext().getReferencePoint();
            var pos = WWHelper.positionFromPosition(position, ground + 1);
            p.setValue(Position.class, pos);

            var lastObservation = p.ext().getObservationFilteredLast();
            if (lastObservation != null) {
                var deltaH = lastObservation.ext().getDelta();
                RigidShape shape;
                if (deltaH != 0) {
                    shape = new Pyramid(WWHelper.positionFromPosition(position, ground + 2), shapeSize * 1.0, shapeSize);
                    if (deltaH < 0) {
                        shape.setRoll(Angle.POS180);
                    }
                } else {
                    var r = shapeSize * .5;
                    shape = new Ellipsoid(WWHelper.positionFromPosition(position, ground + 2), r, r, r);
                }
                var attrs = mAttributeManager.getAlarmInteriorAttributes(TopoHelper.getAlarmLevelHeight(p));
                shape.setAttributes(attrs);
//            point.setValue(Position.class, p);

                addRenderable(shape, true, null, mMapObjects);
            }
        }

        for (var point : extenso.getPoints()) {
            if (point.ext().getObservationsTimeFiltered().isEmpty()) {
                continue;
            }

            var lastObservation = point.ext().getObservationFilteredLast();
            var depth = ground + point.getDepth() * scale;
            var p = WWHelper.positionFromPosition(position, depth);
            var attrs = mAttributeManager.getAlarmInteriorAttributes(point.ext().getAlarmLevel());
            if (extenso.ext().getMeasurementUntilNext(ChronoUnit.DAYS) < 0) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.2);
            }

            var pyramid = new Pyramid(p, shapeSize * 1.0, shapeSize);
            pyramid.setAttributes(attrs);
            point.setValue(Position.class, p);
            var delta = lastObservation.ext().getDelta();
            if (delta != null && delta < 0) {
                pyramid.setRoll(Angle.POS180);
            }

            addRenderable(pyramid, true, null, mMapObjects);
        }
    }

    private void plotLabel(BRockExtensometer extenso, Function<BRockExtensometerPoint, String> function, double offset) {
        extenso.getPoints().forEach(p -> {
            var position = p.<Position>getValue(Position.class);
            var placemark = new PointPlacemark(WWHelper.positionFromPosition(position, position.elevation + offset));
            placemark.setAttributes(mAttributeManager.getLabelPlacemarkAttributes());
            placemark.setAltitudeMode(WorldWind.ABSOLUTE);
            placemark.setHighlightAttributes(WWHelper.createHighlightAttributes(mAttributeManager.getLabelPlacemarkAttributes(), 1.5));
            placemark.setLabelText("•    " + function.apply(p));
            placemark.setAlwaysOnTop(true);
            addRenderable(placemark, true, null, mMapObjects);
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
        addRenderable(placemark, true, null, mMapObjects);
    }

    private void plotLabelsAlarm(BRockExtensometer extenso) {
        var function = (Function<BRockExtensometerPoint, String>) p -> "%.1f  %.1f  %.1f".formatted(
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

    private void plotLabelsDeltaZ(BRockExtensometer extenso) {
        var function = (Function<BRockExtensometerPoint, String>) p -> "%.2f".formatted(p.ext().getDelta());
        plotLabel(extenso, function, 0);
    }

    private void plotLabelsDeltaZ(BTopoControlPoint point) {
        var function = (Function<BTopoControlPoint, String>) p -> p.ext().deltaZero().getDelta1(3);
        plotLabel(point, function, 0);
    }

    private void plotLabelsDepth(BRockExtensometer extenso) {
        var function = (Function<BRockExtensometerPoint, String>) p -> "%.1f".formatted(p.getDepth());
        plotLabel(extenso, function, -.75);
    }

    private void plotLabelsDepth(BTopoControlPoint point) {
        var function = (Function<BTopoControlPoint, String>) p -> "%.1f".formatted(p.getZeroZ());
        plotLabel(point, function, -.75);
    }

}
