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
package org.mapton.butterfly_tmo.tunnelvatten.graphics;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.tmo.BTunnelvatten;
import org.mapton.butterfly_tmo.TmoAttributeManager;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    private static final int DAYS_TO_DIM_FIRST_OBSERVATION = 30;
    private static final int DAYS_TO_SKIP = 30;
    private static final int KEEP_END = 10;
    private static final int KEEP_START = 10;

    private final TmoAttributeManager mAttributeManager = TmoAttributeManager.getInstance();

    public GraphicRenderer(RenderableLayer layer, IndexedCheckModel<GraphicItem> checkModel) {
        sInteractiveLayer = layer;
        sCheckModel = checkModel;
    }

    public void addToAllowList(String name) {
        sPlotLimiter.addToAllowList(name);
    }

    public void plot(BTunnelvatten p, Position position, ArrayList<AVListImpl> mapObjects) {
        GraphicRendererBase.sMapObjects = mapObjects;

        plotMomentarily(p, position);

    }

    public void reset() {
        sPlotLimiter.reset();
    }

    private void plotMomentarily(BTunnelvatten p, Position position) {
        if (!sCheckModel.isChecked(GraphicItem.MOMENTARILY)
                || isPlotLimitReached(p, GraphicItem.MOMENTARILY, position)) {
            return;
        }

        var first = true;
        for (var o : p.ext().getObservationsTimeFiltered()) {
            if (o.getDate() == null) {
                continue;
            }
            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), LocalDateTime.now());
            var altitude = timeSpan / 24000.0;
            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 100.0;

            var dZ = o.getValue();
            if (dZ == 0) {
                continue;
            }
            var scale = 1.0;
            var radius = Math.min(maxRadius, dZ * scale);
            var cylinder = new Cylinder(pos, 0.1, radius);
            var attrs = mAttributeManager.getTimeSeriesAttributes(p);

            cylinder.setAttributes(attrs);
            addRenderable(cylinder, true);
            sPlotLimiter.incPlotCounter(GraphicItem.MOMENTARILY);

            if (first) {
                var path = new Path(pos, position);
                path.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                addRenderable(path, true);
                first = false;
            }
        }
    }
}
