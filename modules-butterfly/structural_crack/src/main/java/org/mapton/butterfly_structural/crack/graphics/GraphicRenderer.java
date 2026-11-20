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
package org.mapton.butterfly_structural.crack.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cone;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.RigidShape;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.BKey;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.DELTA_1D_ALTITUDE;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.DELTA_1D_NEG_HEIGHT;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.DELTA_1D_POS_HEIGHT;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.structural.BStructuralCrackPoint;
import org.mapton.butterfly_structural.crack.CrackAttributeManager;
import org.mapton.butterfly_structural.crack.CrackHelper;
import org.mapton.butterfly_structural.crack.CrackPointBy;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.Exceptions;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    double SHAPE_SIZE = 0.5;
    private final CrackAttributeManager mAttributeManager = CrackAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    @Override
    public void plot(BStructuralCrackPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;
        if (sCheckModel.isChecked(GraphicItem.DELTA)) {
            plotDelta(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.TRACE)) {
            plotTrace(p, position);
        }

        if (mLayerOptions.getPointBy() == CrackPointBy.PIN_3D) {
            plot3dPin2(p, position);
        }

    }

    public Position plot3dPin(BXyzPoint p, Position position) {
//        return s3dPinPointToPositionMap.computeIfAbsent(p, k -> {
//        });
        var zeroZ = Math.abs(MathHelper.convertDoubleToDouble(p.getZeroZ()));
        var startPosition = WWHelper.positionFromPosition(position, zeroZ);
        RigidShape startShape;
        if (p.getDimension() == BDimension._1d) {
            startShape = new Ellipsoid(startPosition, SHAPE_SIZE, SHAPE_SIZE, SHAPE_SIZE);
        } else {
            startShape = new Pyramid(startPosition, SHAPE_SIZE, SHAPE_SIZE);
        }
        var attrs = mAttributeManager.getAlarmInteriorAttributes(CrackHelper.getAlarmLevel((BStructuralCrackPoint) p));
        startShape.setAttributes(attrs);
        return plot3dPin(p, position, startShape);
    }

    public Position plot3dPin(BXyzPoint p, Position position, RigidShape startShape) {

        addRenderable(startShape, true, null, sMapObjects);

        var groundPath = new Path(position, startShape.getCenterPosition());
        if (p.getZeroZ() < 0) {
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathStippleAttributes());
        } else {
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        }
        addRenderable(groundPath, true, null, sMapObjects);

        return startShape.getCenterPosition();

    }

    private void plot3dPin2(BXyzPoint p, Position position) {
        if (Butterfly.anyNaN(p.getZeroX(), p.getZeroY(), p.getZeroZ())) {
            return;
        }

        var position2 = plot3dPin(p, position);
//        plotLabel(p, position2);
        Thread.ofVirtual().start(() -> {
            try {
                Thread.sleep(10);
                String label = p.getValue(BKey.PIN_NAME);
                if (StringUtils.isNotBlank(label)) {
                    plotLabel3d(position2, label);
                }
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    private void plotAlarmConsumption(BStructuralCrackPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.ALARM_CONSUMPTION, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        var o = p.ext().getObservationFilteredLast();

        Integer percentH = p.ext().getAlarmPercent(BComponent.HEIGHT);
        if (percentH == null) {
            percentH = 0;
        }

        int alarmLevel = p.ext().getAlarmLevelHeight(o);
        var dZ = o.ext().getDeltaZ();
        var rise = false;
        if (!Double.isNaN(dZ)) {
            rise = Math.signum(o.ext().getDeltaZ()) > 0;
        }
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
        var box = new Box(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE, PERCENTAGE_SIZE);
        box.setAttributes(attrs);
        addRenderable(box, true, GraphicItem.ALARM_CONSUMPTION, sMapObjects);

        var alarm = p.ext().getAlarm(BComponent.HEIGHT);
        var alarmShape = new Box(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
        plotPercentageAlarmIndicator(position, alarm, alarmShape, false);

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }

    private void plotDelta(BStructuralCrackPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.DELTA, position)) {
            return;
        }

        var heightOffset = 0.1 - Math.abs(p.extOrNull().deltaZero().getDelta1() * 2);
        var pos = WWHelper.positionFromPosition(position, DELTA_1D_ALTITUDE + MathHelper.convertDoubleToDouble(heightOffset) * 2);
        var maxRadius = 10.0;
        var o = p.ext().getObservationFilteredLast();

        var dZ = o.ext().getDeltaZ();
        if (Double.isNaN(dZ)) {
            return;
        }
//        var radius = Math.min(maxRadius, Math.abs(dZ) * 250 + 0.05);
//        var maximus = radius == maxRadius;
        var mScale1dH = 1;
//            var dZ = o.ext().getDeltaZ();
        var radius = Math.min(maxRadius, Math.abs(dZ * 1000) * mScale1dH + 0.05);
        var maximus = radius == maxRadius;

        RigidShape shape;
        if (dZ > 0) {
            shape = new Pyramid(pos, DELTA_1D_POS_HEIGHT, radius);
        } else {
            shape = new Cone(pos, DELTA_1D_NEG_HEIGHT, radius);
            shape.setTilt(Angle.POS180);
        }

        var alarmLevel = p.ext().getAlarmLevelHeight(o);
//        var attrs = mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, maximus);
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, false, maximus);

        shape.setAttributes(attrs);
        addRenderable(shape, true, GraphicItem.DELTA, sMapObjects);
    }

    private void plotTrace(BStructuralCrackPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.TRACE, position)) {
            return;
        }
        var reversedList = p.ext().getObservationsTimeFiltered().reversed();
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = timeSpan / 24000.0;
            if (height <= 0) {
                continue;
            }
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            if (Double.isNaN(o.ext().getDeltaZ())) {
                continue;
            }

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 10.0;

            var mScale1dH = 1;
            var dZ = o.ext().getDeltaZ();
            var radius = Math.min(maxRadius, Math.abs(dZ * 1000) * mScale1dH + 0.05);
            var maximus = radius == maxRadius;
            RigidShape shape;
            if (dZ > 0) {
                shape = new Box(pos, radius, height / 2, radius);
            } else {
                shape = new Cylinder(pos, height, radius);
            }

            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, false, maximus);

            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
                attrs.setOutlineOpacity(0.20);
            }

            shape.setAttributes(attrs);
            addRenderable(shape, true, GraphicItem.TRACE, sMapObjects);
        }
    }

    private void plotTraceOld(BStructuralCrackPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.TRACE, position)) {
            return;
        }
        var reversedList = p.ext().getObservationsTimeFiltered().reversed();
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            if (Double.isNaN(o.ext().getDeltaZ())) {
                continue;
            }

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 10.0;

            var mScale1dH = 0.02;
            var dZ = o.ext().getDeltaZ();
            var radius = Math.min(maxRadius, Math.abs(dZ) * mScale1dH + 0.05);
            var maximus = radius == maxRadius;

            var cylinder = new Box(pos, radius, height, radius);
            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            var rise = Math.signum(dZ) > 0;
            var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, maximus);

            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
                attrs.setOutlineOpacity(0.20);
            }

            cylinder.setAttributes(attrs);
            addRenderable(cylinder, true, GraphicItem.TRACE, sMapObjects);
        }
    }
}
