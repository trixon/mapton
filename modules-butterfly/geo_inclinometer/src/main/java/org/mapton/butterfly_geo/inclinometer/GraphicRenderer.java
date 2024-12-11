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

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.geo.BGeoInclinometerPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final InclinoAttributeManager mAttributeManager = InclinoAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BGeoInclinometerPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.TRACE)) {
            plotTrace(p, position);
        }
    }

    private void plotAlarmConsumption(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_CONSUMPTION, position) || p.ext().getObservationFilteredLast() == null) {
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
        if (dZ != null) {
            rise = Math.signum(o.ext().getDeltaZ()) > 0;
        }
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
        var box = new Box(pos, PERCENTAGE_SIZE, PERCENTAGE_SIZE, PERCENTAGE_SIZE);
        box.setAttributes(attrs);
        addRenderable(box, true, GraphicRendererItem.ALARM_CONSUMPTION, sMapObjects);

        var alarm = p.ext().getAlarm(BComponent.HEIGHT);
        var alarmShape = new Box(position, PERCENTAGE_SIZE_ALARM, PERCENTAGE_SIZE_ALARM_HEIGHT, PERCENTAGE_SIZE_ALARM);
        plotPercentageAlarmIndicator(position, alarm, alarmShape, false);

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }

    private void plotTrace(BGeoInclinometerPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.TRACE, position)) {
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

            if (o.ext().getDeltaZ() == null) {
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
            addRenderable(cylinder, true, GraphicRendererItem.TRACE, sMapObjects);
        }
    }
}
