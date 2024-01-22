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
import gov.nasa.worldwind.geom.Angle;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.render.airspaces.BasicAirspaceAttributes;
import gov.nasa.worldwind.render.airspaces.PartialCappedCylinder;
import java.util.ArrayList;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererAlarmLevel extends GraphicRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (sCheckModel.isChecked(GraphicRendererItem.ALARM_LEVEL)) {
            plot(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (isPlotLimitReached(p, GraphicRendererItem.ALARM_LEVEL, position)) {
            return;
        }

        if (p.getDimension() == BDimension._1d || p.getDimension() == BDimension._3d) {
            plotH(p, position);
        }
        if (p.getDimension() == BDimension._2d || p.getDimension() == BDimension._3d) {
            plotP(p, position);
        }

    }

    private void plotH(BTopoControlPoint p, Position position) {
        var o = p.ext().getObservationFilteredLast();
        var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelHeight(o));
        var attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(material);

        var partCyl = new PartialCappedCylinder(attrs);
        partCyl.setCenter(position);
        partCyl.setRadii(1, 2);
        partCyl.setAltitudes(0.0, 0.25);
        if (p.getDimension() == BDimension._1d) {
            partCyl.setAzimuths(Angle.fromDegrees(10.0), Angle.fromDegrees(350.0));
        } else {
            partCyl.setAzimuths(Angle.fromDegrees(0.0), Angle.fromDegrees(180.0));
        }

        addRenderable(partCyl, true);
    }

    private void plotP(BTopoControlPoint p, Position position) {
        var o = p.ext().getObservationFilteredLast();
        var material = ButterflyHelper.getAlarmMaterial(p.ext().getAlarmLevelPlane(o));
        var attrs = new BasicAirspaceAttributes();
        attrs.setInteriorMaterial(material);

        var partCyl = new PartialCappedCylinder(attrs);
        partCyl.setCenter(position);
        partCyl.setRadii(0, 1);
        partCyl.setAltitudes(0.0, 0.25);
        if (p.getDimension() == BDimension._2d) {
            partCyl.setAzimuths(Angle.fromDegrees(10.0), Angle.fromDegrees(350.0));
        } else {
            partCyl.setAzimuths(Angle.fromDegrees(180.0), Angle.fromDegrees(360.0));
        }

        addRenderable(partCyl, true);
    }
}
