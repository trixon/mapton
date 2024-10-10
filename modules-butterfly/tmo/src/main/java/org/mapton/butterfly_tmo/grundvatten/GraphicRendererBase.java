/*
 * Copyright 2024 Patrik Karlström.
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
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_core.api.PlotLimiter;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GraphicRendererBase {

    protected static IndexedCheckModel<GraphicRendererItem> sCheckModel;
    protected static RenderableLayer sInteractiveLayer;
    protected static ArrayList<AVListImpl> sMapObjects;
    protected static PlotLimiter sPlotLimiter = new PlotLimiter();
//    protected static HashMap<BTopoControlPoint, Position[]> sPointToPositionMap = new HashMap<>();
//    protected final TopoAttributeManager mAttributeManager = TopoAttributeManager.getInstance();
//    protected final TopoManager mManager = TopoManager.getInstance();

    public GraphicRendererBase() {
        for (var renderItem : GraphicRendererItem.values()) {
            sPlotLimiter.setLimit(renderItem, renderItem.getPlotLimit());
        }
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            sInteractiveLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                sMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    protected boolean isPlotLimitReached(BGrundvatten p, Object key, Position position) {
        if (sPlotLimiter.isLimitReached(key, p.getName())) {
            addRenderable(sPlotLimiter.getPlotLimitIndicator(position, p.ext().getObservationsTimeFiltered().isEmpty()), true);
            return true;
        } else {
            return false;
        }
    }

}
