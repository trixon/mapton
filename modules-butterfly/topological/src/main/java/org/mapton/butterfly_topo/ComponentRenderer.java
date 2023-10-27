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
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Path;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRenderer extends ComponentRendererBase {

    private final ComponentRendererTrace mTraceRenderer = new ComponentRendererTrace();
    private final ComponentRendererVector mVectorRenderer = new ComponentRendererVector();

    public ComponentRenderer(RenderableLayer layer, IndexedCheckModel<RenderComponent> checkModel) {
        sInteractiveLayer = layer;
        sCheckModel = checkModel;
    }

    public void plot(BTopoControlPoint p, Position position, ArrayList<AVListImpl> mapObjects) {
        ComponentRendererBase.sMapObjects = mapObjects;
        plotBearing(p, position);

        if (p.ext().getNumOfObservationsFiltered() > 1) {
            mTraceRenderer.plot(p, position);
            mVectorRenderer.plot(p, position);
        }
    }

    public void reset() {
        sPointToPositionMap.clear();
    }

    private void plotBearing(BTopoControlPoint p, Position position) {
        int size = p.ext().getObservationsFiltered().size();
        if (!sCheckModel.isChecked(RenderComponent.BEARING)
                || p.getDimension() == BDimension._1d
                || p.ext().getNumOfObservationsFiltered() == 0) {
            return;
        }

        int maxNumberOfItemsToPlot = Math.min(10, p.ext().getNumOfObservationsFiltered());

        boolean first = true;
        for (int i = size - 1; i >= size - maxNumberOfItemsToPlot + 1; i--) {
            var o = p.ext().getObservationsFiltered().get(i);

            try {
                var bearing = o.ext().getBearing();
                if (bearing == null || bearing.isNaN()) {
                    continue;
                }

                var length = 10.0;
                var p2 = WWHelper.movePolar(position, bearing, length);
                var z = first ? 0.2 : 0.1;
                position = WWHelper.positionFromPosition(position, z);
                p2 = WWHelper.positionFromPosition(p2, z);
                var path = new Path(position, p2);
                path.setAttributes(mAttributeManager.getBearingAttribute(first));
                first = false;

                addRenderable(path, true);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
    }

}
