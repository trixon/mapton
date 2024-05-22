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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererSpeed extends GraphicRendererBase {

    public GraphicRendererSpeed() {
    }

    public void plot(BTopoControlPoint p, Position position) {
        if (sCheckModel.isChecked(GraphicRendererItem.SPEED_1D)) {
            plotSpeed(p, position);
        }

        if (sCheckModel.isChecked(GraphicRendererItem.SPEED_1D_TRACE)) {
            plotSpeedTrace(p, position);
        }
    }

    private void plotSpeed(BTopoControlPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.SPEED_1D, position) || p.getDimension() == BDimension._2d) {
            return;
        }

        var height = 0.4;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        var o = p.ext().getObservationFilteredLast();

        if (o.ext().getDeltaZ() == null) {
            return;
        }

        var speed = p.ext().getSpeed()[0];
        var radius = Math.min(maxRadius, Math.abs(speed) * 250 + 0.05);
        var maximus = radius == maxRadius;
        var rise = Math.signum(speed) > 0;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevelHeight(o);
        var attrs = new BasicShapeAttributes(mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, rise, maximus));
        attrs.setInteriorMaterial(TopoHelper.getSpeedMaterial(p));

//        if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorOpacity(0.25);
//            attrs.setOutlineOpacity(0.20);
//        }
        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true);
    }

    private void plotSpeedTrace(BTopoControlPoint p, Position position) {
        var pos = position;
        var cylinder = new Cylinder(pos, 10, 1);
        addRenderable(cylinder, true);
    }
}
