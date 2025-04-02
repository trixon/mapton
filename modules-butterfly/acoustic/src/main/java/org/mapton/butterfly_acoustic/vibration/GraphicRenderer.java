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
package org.mapton.butterfly_acoustic.vibration;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Cylinder;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BAcousticVibrationPoint point, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicRendererItem.TRACE)) {
            plotTrace(point, position);
        }
    }

    @Override
    public void reset() {
        resetPlotLimiter();
    }

    private void plotTrace(BAcousticVibrationPoint p, Position position) {
        if (isPlotLimitReached(p, GraphicRendererItem.TRACE, position)) {
            return;
        }

        var endPosition = Position.ZERO;
        for (var o : p.ext().getObservationsTimeFiltered().reversed()) {
            if (o.ext().getDeltaZ() == null) {
                continue;
            }

            var timeSpan = ChronoUnit.MINUTES.between(o.getDate(), LocalDateTime.now());
            var altitude = timeSpan / 24000.0;
            var pos = WWHelper.positionFromPosition(position, altitude);
            var maxRadius = 10.0;
            var mScale1dH = 1.0;
            var z = o.getMeasuredZ();
            var radius = Math.min(maxRadius, Math.abs(z) * mScale1dH + 0.1);
            var maximus = radius == maxRadius;
            var cylinder = new Cylinder(pos, 0.025, radius);
            var attrs = mAttributeManager.getComponentTracedAttributes(0, maximus);

            if (o.getLimit() != null && z >= o.getLimit()) {
                attrs = new BasicShapeAttributes(attrs);
                attrs.setInteriorMaterial(Material.RED);
            }

            cylinder.setAttributes(attrs);
            addRenderable(cylinder, true, GraphicRendererItem.TRACE, sMapObjects);
            endPosition = pos;
        }

        var startPosition = WWHelper.positionFromPosition(position, 0.0);
        var groundPath = new Path(startPosition, endPosition);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true, GraphicRendererItem.TRACE, sMapObjects);
    }

}
