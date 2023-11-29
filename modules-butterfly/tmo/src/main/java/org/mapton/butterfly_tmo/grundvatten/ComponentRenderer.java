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
package org.mapton.butterfly_tmo.grundvatten;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Renderable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRenderer {

    private final GrundvattenAttributeManager mAttributeManager = GrundvattenAttributeManager.getInstance();
    private final RenderableLayer mEllipsoidLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final RenderableLayer mGroundConnectorLayer;
    private final RenderableLayer mSurfaceLayer;
    int limit = 0;

    public ComponentRenderer(RenderableLayer ellipsoidLayer, RenderableLayer groundConnectorLayer, RenderableLayer surfaceLayer) {
        mEllipsoidLayer = ellipsoidLayer;
        mGroundConnectorLayer = groundConnectorLayer;
        mSurfaceLayer = surfaceLayer;
    }

    public void addRenderable(RenderableLayer layer, Renderable renderable) {
        layer.addRenderable(renderable);
        if (layer == mEllipsoidLayer) {
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    public void plot(BGrundvatten p, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;
        if (limit > 1000) {
//            return;
        }
        limit++;
//        if (isPlotLimitReached(p.getName(), ComponentRendererItem.TRACE_1D, position)) {
//            return;
//        }
        var reversedList = p.ext().getObservationsTimeFiltered().reversed();
        var prevDate = LocalDateTime.now();
        var altitude = 0.0;
        var prevHeight = 0.0;

        for (int i = 0; i < reversedList.size(); i++) {
            var o = reversedList.get(i);

            if (ChronoUnit.DAYS.between(o.getDate(), prevDate) < 100) {
                continue;
            }

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), prevDate);
            var height = timeSpan / 24000.0;
            altitude = altitude + height * 0.5 + prevHeight * 0.5;
            prevDate = o.getDate();
            prevHeight = height;

            if (ObjectUtils.anyNull(p.getReferensnivå(), o.getNivå())) {
                continue;
            }

            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 10.0;

            var dZ = p.getReferensnivå() - o.getNivå();
            var radius = Math.min(maxRadius, Math.abs(dZ) * 1 + 0.05);
            var maximus = radius == maxRadius;

            var cylinder = new Cylinder(pos, height, radius);
//            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            var rise = Math.signum(dZ) > 0;
            var attrs = mAttributeManager.getTimeSeriesAttributes(p);

//            if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
//                attrs = new BasicShapeAttributes(attrs);
//                attrs.setInteriorOpacity(0.25);
//                attrs.setOutlineOpacity(0.20);
//            }
            cylinder.setAttributes(attrs);
            addRenderable(mEllipsoidLayer, cylinder);
//            incPlotCounter(ComponentRendererItem.TRACE_1D);
        }

    }

    public void reset() {
    }

}
