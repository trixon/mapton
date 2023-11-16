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

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.Cylinder;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPointObservation;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRendererCircle extends ComponentRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (sCheckModel.isChecked(ComponentRendererItem.CIRCLE_1D) && p.getDimension() == BDimension._1d) {
            plot1dCircle(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot1dCircle(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (isPlotLimitReached(ComponentRendererItem.CIRCLE_1D, position)) {
            return;
        }

        var height = 0.4;
        var pos = WWHelper.positionFromPosition(position, height * 0.5 * 2);
        var maxRadius = 10.0;
        BTopoControlPointObservation o = p.ext().getObservationFilteredLast();

        var dZ = o.ext().getDeltaZ();
        if (dZ == null) {
            return;
        }
        var radius = Math.min(maxRadius, Math.abs(dZ) * 250 + 0.05);
        var maximus = radius == maxRadius;
        var rise = Math.signum(dZ) > 0;

        var cylinder = new Cylinder(pos, height, radius);
        var alarmLevel = p.ext().getAlarmLevelHeight(o);
        var attrs = mAttributeManager.getComponentCircle1dAttributes(p, alarmLevel, rise, maximus);
//        if (i == 0 && ChronoUnit.DAYS.between(o.getDate(), LocalDateTime.now()) > 180) {
//            attrs = new BasicShapeAttributes(attrs);
//            attrs.setInteriorOpacity(0.25);
//            attrs.setOutlineOpacity(0.20);
//        }

        cylinder.setAttributes(attrs);
        addRenderable(cylinder, true);
        incPlotCounter(ComponentRendererItem.CIRCLE_1D);
    }
}
