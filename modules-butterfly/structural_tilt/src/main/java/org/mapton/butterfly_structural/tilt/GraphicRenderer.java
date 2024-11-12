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
package org.mapton.butterfly_structural.tilt;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.airspaces.Polygon;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.IndexedCheckModel;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_ALTITUDE;
import static org.mapton.butterfly_core.api.BaseGraphicRenderer.PERCENTAGE_SIZE;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
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

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BStructuralTiltPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;
        mPosition = position;
        initPositions(p);

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.AXIS)) {
            plotAxis(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TILT)) {
            plotTilt(p);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TILT_LONGITUDINAL)) {
            plotTiltLongitudinal(p);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TILT_TRANSVERSAL)) {
            plotTiltTransversal(p);
        }
    }

    private Double calcBearing(BStructuralTiltPoint p) {
        var dX = p.ext().deltaZero().getDeltaX();
        var dY = p.ext().deltaZero().getDeltaY();
        if (ObjectUtils.anyNull(p.getDirectionX(), dX, dY)) {
            return null;
        }

        var bearing = MathHelper.convertCcwDegreeToCw(p.getDirectionX());
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

        if (ObjectUtils.anyNull(bearing, p.ext().deltaZero().getDelta2())) {
            return;
        }

        var minLength = 2.0;
        var scaleLength = 100.0;
        var z = 0.1;
        var length = Math.max(Math.abs(scaleLength * p.ext().deltaZero().getDelta2()), minLength);
        mPosition0 = WWHelper.positionFromPosition(mPosition, z);
        mPosition3 = WWHelper.movePolar(mPosition0, bearing, length, z);

        var transDelta = p.ext().deltaZero().getDeltaX();
        if (transDelta != null) {
            var transBearing = MathHelper.convertCcwDegreeToCw(p.getDirectionX());
            if (transDelta < 0) {
                transBearing -= 180.0;
            }
            var transLength = Math.max(Math.abs(scaleLength * transDelta), minLength);
            mPosition1 = WWHelper.movePolar(mPosition0, transBearing, transLength, z);
        }

        var longDelta = p.ext().deltaZero().getDeltaY();
        if (longDelta != null) {
            var longBearing = MathHelper.convertCcwDegreeToCw(p.getDirectionX());
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
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION, position) || p.ext().getObservationFilteredLast() == null) {
            return;
        }

        Integer percentH = p.ext().getAlarmPercent();
        if (percentH == null) {
            percentH = 0;
        }

        int alarmLevel = TiltHelper.getAlarmLevel(p);
//        var dZ = o.ext().getDeltaZ();
        var rise = false;
//        if (dZ != null) {
//            rise = Math.signum(o.ext().getDeltaZ()) > 0;
//        }
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
        var box = new Box(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE * 0.5, PERCENTAGE_SIZE);
        box.setAttributes(attrs);
        addRenderable(box, true, GraphicRendererItem.ALARM_CONSUMPTION, sMapObjects);

        var alarm = p.ext().getAlarm(BComponent.HEIGHT);
        var alarmShape = new Box(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
        plotPercentageAlarmIndicator(position, alarm, alarmShape, false);

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }

    private void plotAxis(BStructuralTiltPoint p, Position position) {
        if (p.getDirectionX() == null) {
            return;
        }

        try {
            var bearing = MathHelper.convertCcwDegreeToCw(p.getDirectionX());
            var length = 2.0;
            var p2 = WWHelper.movePolar(position, bearing, length);
            var z = 0.1;
            position = WWHelper.positionFromPosition(position, z);
            p2 = WWHelper.positionFromPosition(p2, z);
            var arrowHeadSize = 0.15;

            //East - Positive X
            var pathE = new Path(position, p2);
            pathE.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathE, false, null, sMapObjects);

            var x0 = WWHelper.movePolar(p2, bearing + 0, arrowHeadSize);
            var x1 = WWHelper.movePolar(p2, bearing + 120, arrowHeadSize);
            var x2 = WWHelper.movePolar(p2, bearing + 240, arrowHeadSize);
            var xPolygon = new Polygon(List.of(x0, x1, x2));
            xPolygon.setAltitudes(0.0, 0.1);
            xPolygon.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(xPolygon, false, null, null);

            //West
            p2 = WWHelper.movePolar(position, bearing - 180, length);
            var pathW = new Path(position, p2);
            pathW.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathW, false, null, sMapObjects);

            //North
            p2 = WWHelper.movePolar(position, bearing - 90, length);
            var pathN = new Path(position, p2);
            pathN.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathN, false, null, sMapObjects);

            var y0 = WWHelper.movePolar(p2, bearing - 90 + 0, arrowHeadSize);
            var y1 = WWHelper.movePolar(p2, bearing - 90 + 120, arrowHeadSize);
            var y2 = WWHelper.movePolar(p2, bearing - 90 + 240, arrowHeadSize);
            var yPolygon = new Polygon(List.of(y0, y1, y2));
            yPolygon.setAltitudes(0.0, 0.1);
            yPolygon.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(yPolygon, false, null, null);

            //South
            p2 = WWHelper.movePolar(position, bearing + 90, length);
            var pathS = new Path(position, p2);
            pathS.setAttributes(mAttributeManager.getAxisAttributes());
            addRenderable(pathS, false, null, sMapObjects);
        } catch (Exception e) {
            //System.err.println(e);
        }
    }

    private void plotTilt(BStructuralTiltPoint p) {
        var bearing = calcBearing(p);

        try {
            if (bearing == null || mPosition3 == null) {
                plotZeroCircle(mPosition0);
            } else {
                var path = new Path(mPosition0, mPosition3);
                path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevel(p)));

                addRenderable(path, true, GraphicRendererItem.TILT, sMapObjects);
            }
        } catch (Exception e) {
            //System.err.println(e);
        }

    }

    private void plotTiltLongitudinal(BStructuralTiltPoint p) {
        var dY = p.ext().deltaZero().getDeltaY();
        if (ObjectUtils.anyNull(dY, mPosition0, mPosition1)
                || dY == 0.0
                || mPosition2 == null) {
            return;
        }

        var path = new Path(mPosition0, mPosition2);
        path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevelLong(p)));
        addRenderable(path, true, GraphicRendererItem.TILT_LONGITUDINAL, sMapObjects);
    }

    private void plotTiltTransversal(BStructuralTiltPoint p) {
        var dX = p.ext().deltaZero().getDeltaX();
        if (ObjectUtils.anyNull(dX, mPosition0, mPosition1)
                || dX == 0.0
                || mPosition1 == null) {
            return;
        }

        var path = new Path(mPosition0, mPosition1);
        path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevelTrans(p)));
        addRenderable(path, true, GraphicRendererItem.TILT_TRANSVERSAL, sMapObjects);
    }

    private void plotZeroCircle(Position position) {
        var cylinder = new Cylinder(position, 0.1, 0.5);
        cylinder.setAttributes(mAttributeManager.getAlarmOutlineAttributes(0));

        addRenderable(cylinder, true, GraphicRendererItem.TILT, sMapObjects);
    }

}
