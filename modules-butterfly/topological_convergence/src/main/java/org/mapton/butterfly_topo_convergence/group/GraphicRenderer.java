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
package org.mapton.butterfly_topo_convergence.group;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import java.util.ArrayList;
import java.util.Random;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer extends GraphicRendererBase {

    public GraphicRenderer(RenderableLayer layer, RenderableLayer passiveLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        super(layer, passiveLayer);
        sCheckModel = checkModel;
    }

    public void plot(BTopoConvergenceGroup convergenceGroup, Position position, ArrayList<AVListImpl> mapObjects) {
        sMapObjects = mapObjects;

        if (sCheckModel.isChecked(GraphicRendererItem.NONE)) {
        }
    }

    @Override
    public void reset() {
        resetPlotLimiter();
    }

    private void plotPoints(BTopoConvergenceGroup convergenceGroup, Position position, ArrayList<AVListImpl> mapObjects) {
        var offset = convergenceGroup.ext2().getControlPoints().stream()
                .map(p -> p.getZeroZ())
                .mapToDouble(Double::doubleValue).min().orElse(0);
        if (offset < 0) {
            offset = offset * -1.0;
        }
        offset += 2;
        var random = new Random();
        for (var controlPoint : convergenceGroup.ext2().getControlPoints()) {
            var altitude = controlPoint.getZeroZ() + offset;
            var p = Position.fromDegrees(controlPoint.getLat(), controlPoint.getLon(), altitude);
            var radius = 0.6;
            var pyramid = new Pyramid(p, radius * 1.0, radius * 1.0);

            pyramid.setAttributes(mAttributeManager.getNodeAttributes());
            addRenderable(pyramid, true, GraphicRendererItem.NONE, sMapObjects);

            for (var cp2 : convergenceGroup.ext2().getControlPoints()) {
                if (cp2 == controlPoint) {
                    continue;
                }
                var altitude2 = cp2.getZeroZ() + offset;
                var p2 = Position.fromDegrees(cp2.getLat(), cp2.getLon(), altitude2);
                var pairPath = new Path(p, p2);
                var attrs = new BasicShapeAttributes(mAttributeManager.getPairPathAttributes());
                int colorIndex = random.nextInt(0, 3);
                switch (colorIndex) {
                    case 0 ->
                        attrs.setOutlineMaterial(Material.YELLOW);
                    case 1 ->
                        attrs.setOutlineMaterial(Material.RED);
                    case 2 ->
                        attrs.setOutlineMaterial(Material.GREEN);
                    case 3 ->
                        attrs.setOutlineMaterial(Material.BLUE);
                    default ->
                        attrs.setOutlineMaterial(Material.MAGENTA);
                }

                if (random.nextBoolean()) {
                    attrs.setOutlineStippleFactor(3);
                }

                pairPath.setAttributes(attrs);
                addRenderable(pairPath, true, null, null);
            }
        }
    }

}
