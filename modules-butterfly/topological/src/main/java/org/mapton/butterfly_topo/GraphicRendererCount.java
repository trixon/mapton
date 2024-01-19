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
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRendererCount extends GraphicRendererBase {

    public ArrayList<AVListImpl> plot(BTopoControlPoint p, Position position) {
        var mapObjects = new ArrayList<AVListImpl>();

        if (sCheckModel.isChecked(GraphicRendererItem.MEASUREMENTS)) {
            plot(p, position, mapObjects);
        }

        return mapObjects;
    }

    private void plot(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        if (isPlotLimitReached(p, GraphicRendererItem.MEASUREMENTS, position)) {
            return;
        }
        var count = p.ext().getObservationsTimeFiltered().size();
        var cylinder = new Cylinder(position, count * .25, 0.5);
        cylinder.setAttributes(mAttributeManager.getComponentMeasurementsAttributes(p));
        addRenderable(cylinder, true);
    }
}
