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
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
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

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BStructuralTiltPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.DIRECTION_X)) {
            plotDirectionX(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.DIRECTION)) {
            plotDirection(p, position);
        }
    }

    private Double calcBearing(BStructuralTiltPoint p) {
        if (p.getDirectionX() == null) {
            return null;
        }
        var bearing = MathHelper.convert(p.getDirectionX());
        var o0 = p.ext().getObservationFilteredFirst();
        var o1 = p.ext().getObservationFilteredLast();

        if (ObjectUtils.anyNull(o0, o1) || ObjectUtils.anyNull(o0.getMeasuredX(), o1.getMeasuredX(), o0.getMeasuredY(), o1.getMeasuredY())) {
            return null;
        }

        var v0 = Math.atan(o0.getMeasuredY() / o0.getMeasuredX());
        var v1 = Math.atan(o1.getMeasuredY() / o1.getMeasuredX());
        var delta = Math.toDegrees(v1 - v0);
        var r = bearing + delta;

        return r;
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

    private void plotDirection(BStructuralTiltPoint p, Position position) {
        var bearing = calcBearing(p);
        if (bearing == null) {
            return;
        }

        try {
            var length = Math.abs(100.0 * p.ext().deltaZero().getDelta2());
            length = Math.max(length, 2.0);
            var p2 = WWHelper.movePolar(position, bearing, length);
            var z = 0.2;
            position = WWHelper.positionFromPosition(position, z);
            p2 = WWHelper.positionFromPosition(p2, z);
            var path = new Path(position, p2);
            path.setAttributes(mAttributeManager.getAlarmOutlineAttributes(TiltHelper.getAlarmLevel(p)));

            addRenderable(path, true, GraphicRendererItem.DIRECTION, sMapObjects);
        } catch (Exception e) {
            System.err.println(e);
        }

    }

    private void plotDirectionX(BStructuralTiltPoint p, Position position) {
        if (p.getDirectionX() == null) {
            return;
        }

        try {
            var bearing = MathHelper.convert(p.getDirectionX());

            var length = 10.0;
            var p2 = WWHelper.movePolar(position, bearing, length);
            var z = 0.1;
            position = WWHelper.positionFromPosition(position, z);
            p2 = WWHelper.positionFromPosition(p2, z);
            var path = new Path(position, p2);
            path.setAttributes(mAttributeManager.getDirectionXAttributes());

            addRenderable(path, true, GraphicRendererItem.DIRECTION, sMapObjects);
        } catch (Exception e) {
            System.err.println(e);
        }
    }

}
