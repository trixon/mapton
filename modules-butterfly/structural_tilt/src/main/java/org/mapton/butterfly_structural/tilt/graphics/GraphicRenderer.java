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
package org.mapton.butterfly_structural.tilt.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Wedge;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_ALTITUDE;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_SIZE;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_structural.tilt.TiltAttributeManager;
import org.mapton.butterfly_structural.tilt.TiltHelper;
import org.mapton.worldwind.api.WWHelper;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final TiltAttributeManager mAttributeManager = TiltAttributeManager.getInstance();
    private Position mPosition;
    private Position mPosition0;
    private Position mPosition1;
    private Position mPosition2;
    private Position mPosition3;

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    @Override
    public void plot(BStructuralTiltPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;
        mPosition = position;
        initPositions(p);

        if (sCheckModel.isChecked(GraphicItem.TRACE)) {
            plotTrace(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.AXIS)) {
            Double bearing = MathHelper.convertCcwDegreeToCw(p.getAzimuth());

            plotAxis(p, position, DEFAULT_AXIS_LENGTH, bearing);
        }

        if (sCheckModel.isChecked(GraphicItem.TILT)) {
            plotTilt(p);
        }

        if (sCheckModel.isChecked(GraphicItem.TILT_LONGITUDINAL)) {
            plotTiltLongitudinal(p);
        }

        if (sCheckModel.isChecked(GraphicItem.TILT_TRANSVERSAL)) {
            plotTiltTransversal(p);
        }
    }

    private Double calcBearing(BStructuralTiltPoint p) {
        return calcBearing(p, p.ext().deltaZero().getDeltaX(), p.ext().deltaZero().getDeltaY());
    }

    private Double calcBearing(BStructuralTiltPoint p, double dX, double dY) {
        if (ObjectUtils.anyNull(p.getAzimuth()) || Butterfly.anyNaN(dX, dY)) {
            return null;
        }

        var bearing = MathHelper.convertCcwDegreeToCw(p.getAzimuth());
        if (dX == 0.0 && dY == 0.0) {
            return null;
        } else if (dX == 0.0) {
            return bearing + (dY > 0 ? -90.0 : +90.0);
        }

        var v = bearing + MathHelper.convertCcwDegreeToCw(Math.toDegrees(Math.atan(dY / dX)));

        if (dX > 0) {
            v -= 90.0;
        } else {
            v += 90.0;
        }

        return v;
    }

    private void initPositions(BStructuralTiltPoint p) {
        mPosition0 = null;
        mPosition1 = null;
        mPosition2 = null;
        mPosition3 = null;

        var bearing = calcBearing(p);

        if (bearing == null || Double.isNaN(p.ext().deltaZero().getDelta2())) {
            return;
        }

        var minLength = 2.0;
        var scaleLength = 20.0;
        var z = 0.1;
        var length = Math.max(Math.abs(scaleLength * p.ext().deltaZero().getDelta2()), minLength);
        mPosition0 = WWHelper.positionFromPosition(mPosition, z);
        mPosition3 = WWHelper.movePolar(mPosition0, bearing, length, z);

        var transDelta = p.ext().deltaZero().getDeltaX();
        if (!Double.isNaN(transDelta)) {
            var transBearing = MathHelper.convertCcwDegreeToCw(p.getAzimuth());
            if (transDelta < 0) {
                transBearing -= 180.0;
            }
            var transLength = Math.max(Math.abs(scaleLength * transDelta), minLength);
            mPosition1 = WWHelper.movePolar(mPosition0, transBearing, transLength, z);
        }

        var longDelta = p.ext().deltaZero().getDeltaY();
        if (!Double.isNaN(longDelta)) {
            var longBearing = MathHelper.convertCcwDegreeToCw(p.getAzimuth());
            if (longDelta > 0) {
                longBearing -= 90.0;
            } else {
                longBearing += 90.0;
            }
            var longLength = Math.max(Math.abs(scaleLength * longDelta), minLength);
            mPosition2 = WWHelper.movePolar(mPosition0, longBearing, longLength, z);
        }
    }

    private void plotAlarmConsumption(BStructuralTiltPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicItem.ALARM_CONSUMPTION, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        Integer percentH = p.ext().getAlarmPercent();
        if (percentH == null) {
            percentH = 0;
        }

        int alarmLevel = TiltHelper.getAlarmLevel(p);
//        var dZ = o.ext().getDeltaZ();
        var rise = false;
//        if (!Double.isNaN(dZ)) {
//            rise = Math.signum(o.ext().getDeltaZ()) > 0;
//        }
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
        var box = new Box(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE * 0.5, PERCENTAGE_SIZE);
        box.setAttributes(attrs);
        addRenderable(box, true, GraphicItem.ALARM_CONSUMPTION, sMapObjects);

        var alarm = p.ext().getAlarm(BComponent.HEIGHT);
        var alarmShape = new Box(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
        plotPercentageAlarmIndicator(position, alarm, alarmShape, false);

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }

    private void plotTilt(BStructuralTiltPoint p) {
        var bearing = calcBearing(p);

        try {
            if (bearing == null || mPosition3 == null) {
                plotZeroCircle(mPosition0);
            } else {
                var path = new Path(mPosition0, mPosition3);
                path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevel(p)));

                addRenderable(path, true, GraphicItem.TILT, sMapObjects);
            }
        } catch (Exception e) {
            //System.err.println(e);
        }

    }

    private void plotTiltLongitudinal(BStructuralTiltPoint p) {
        var dY = p.ext().deltaZero().getDeltaY();
        if (Double.isNaN(dY) || ObjectUtils.anyNull(mPosition0, mPosition1)
                || dY == 0.0
                || mPosition2 == null) {
            return;
        }

        var path = new Path(mPosition0, mPosition2);
        path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevelLong(p)));
        addRenderable(path, true, GraphicItem.TILT_LONGITUDINAL, sMapObjects);
    }

    private void plotTiltTransversal(BStructuralTiltPoint p) {
        var dX = p.ext().deltaZero().getDeltaX();
        if (Double.isNaN(dX) || ObjectUtils.anyNull(mPosition0, mPosition1)
                || dX == 0.0
                || mPosition1 == null) {
            return;
        }

        var path = new Path(mPosition0, mPosition1);
        path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevelTrans(p)));
        addRenderable(path, true, GraphicItem.TILT_TRANSVERSAL, sMapObjects);
    }

    private void plotTrace(BStructuralTiltPoint p, Position position) {
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

            if (o.isZeroMeasurement()) {
                var pos = WWHelper.positionFromPosition(position, altitude + height * .5);
                var cylinder = new Cylinder(pos, .5, 2.0);
                cylinder.setAttributes(mAttributeManager.getComponentZeroAttributes());
                addRenderable(cylinder, true, null, sMapObjects);

            }

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 40.0;

            var mScale1dH = 0.01;
            var dZ = o.ext().getDeltaZ();
            var radius = Math.min(maxRadius, Math.abs(dZ * 1000) * mScale1dH + 0.05);
            var maximus = radius == maxRadius;
            var angle = 5.0;
            var wedge = new Wedge(pos, Angle.fromDegrees(angle), height, radius);
            var bearing = calcBearing(p, o.ext().getDeltaX(), o.ext().getDeltaY());
            if (bearing == null) {
                continue;
            }
            var az = Angle.normalizedDegrees(bearing - angle / 2);
            wedge.setHeading(Angle.fromDegrees(az));

            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            var attrs = mAttributeManager.getAlarmInteriorAttributes(alarmLevel, maximus);

            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
                attrs.setOutlineOpacity(0.20);
            }

            wedge.setAttributes(attrs);
            addRenderable(wedge, true, GraphicItem.TRACE, sMapObjects);
        }
    }

    private void plotZeroCircle(Position position) {
        var cylinder = new Cylinder(position, 0.1, 0.5);
        cylinder.setAttributes(mAttributeManager.getAlarmOutlineAttributes(0));

        addRenderable(cylinder, true, GraphicItem.TILT, sMapObjects);
    }

}
