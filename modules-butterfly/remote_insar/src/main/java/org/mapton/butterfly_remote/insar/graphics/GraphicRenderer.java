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
package org.mapton.butterfly_remote.insar.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Box;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.RigidShape;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.math3.util.FastMath;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_remote.insar.InsarAttributeManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private final InsarAttributeManager mAttributeManager = InsarAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    @Override
    public void plot(BRemoteInsarPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicItem.ALARM_CONSUMPTION)) {
            plotAlarmConsumption(p, position);
        }

        if (sCheckModel.isChecked(GraphicItem.TRACE)) {
            plotTrace(p, position);
        }
    }

    private void plotAlarmConsumption(BRemoteInsarPoint p, Position position) {
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
        if (dZ != null) {
            rise = Math.signum(o.ext().getDeltaZ()) > 0;
        }
        var attrs = mAttributeManager.getComponentTrace1dAttributes(alarmLevel, rise, false);
        var pos = WWHelper.positionFromPosition(position, PERCENTAGE_ALTITUDE * percentH / 100.0);
        double symbolSize = PERCENTAGE_SIZE / 3;
        var box = new Box(pos, symbolSize, symbolSize, symbolSize);
        box.setAttributes(attrs);
        addRenderable(box, true, GraphicItem.ALARM_CONSUMPTION, sMapObjects);

        var alarm = p.ext().getAlarm(BComponent.HEIGHT);
        double symbolSizeAlarm = PERCENTAGE_SIZE_ALARM / 3;
        double symbolSizeAlarmHeight = PERCENTAGE_SIZE_ALARM_HEIGHT / 3;
        var alarmShape = new Box(position, symbolSizeAlarm, symbolSizeAlarmHeight, symbolSizeAlarm);
        plotPercentageAlarmIndicator(position, alarm, alarmShape, false);

        plotPercentageRod(position, p.ext().getAlarmPercent());
    }

    private void plotTrace(BRemoteInsarPoint p, Position position) {
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
            var height = FastMath.max(0.01, timeSpan / 24000.0);
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            if (o.ext().getDeltaZ() == null) {
                continue;
            }

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 10.0;

            var dZ = o.ext().getDeltaZ();
            var mScale1dH = 250;
            var radius = Math.min(maxRadius, Math.abs(dZ) * mScale1dH + 0.05);
            var maximus = radius == maxRadius;
            RigidShape shape;
            if (dZ > 0) {
                shape = new Box(pos, radius, height, radius);
            } else {
                shape = new Cylinder(pos, height, radius);
            }

            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            var attrs = new BasicShapeAttributes(mAttributeManager.getComponentTrace1dAttributes(alarmLevel, false, maximus));
            var color = ButterflyHelper.getRangeColor(dZ, 0.01);
            attrs.setInteriorMaterial(new Material(color));
            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 56) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorOpacity(0.25);
                attrs.setOutlineOpacity(0.20);
            }

            shape.setAttributes(attrs);
            addRenderable(shape, true, GraphicItem.TRACE, sMapObjects);
        }
    }
}
