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
package org.mapton.butterfly_topo.convergence;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Pyramid;
import gov.nasa.worldwind.render.Renderable;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePoint;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final ConvergenceAttributeManager mAttributeManager = ConvergenceAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mEllipsoidLayer;
    private final RenderableLayer mGroundConnectorLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final RenderableLayer mSurfaceLayer;

    public GraphicRenderer(RenderableLayer ellipsoidLayer, RenderableLayer groundConnectorLayer, RenderableLayer surfaceLayer, IndexedCheckModel<GraphicRendererItem> checkModel) {
        mEllipsoidLayer = ellipsoidLayer;
        mGroundConnectorLayer = groundConnectorLayer;
        mSurfaceLayer = surfaceLayer;
        mCheckModel = checkModel;
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

    public void plot(BTopoConvergencePoint convergencePoint, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        if (mCheckModel.isChecked(GraphicRendererItem.BALLS)) {
            plotPoints(convergencePoint, position, mapObjects);
        }
    }

    public void reset() {
    }

    private void plotPoints(BTopoConvergencePoint convergencePoint, Position position, ArrayList<AVListImpl> mapObjects) {
        var offset = convergencePoint.ext2().getControlPoints().stream()
                .map(p -> p.getZeroZ())
                .mapToDouble(Double::doubleValue).min().orElse(0);
        if (offset < 0) {
            offset = offset * -1.0;
        }
        offset += 2;

        for (var controlPoint : convergencePoint.ext2().getControlPoints()) {
            var altitude = controlPoint.getZeroZ() + offset;
            var p = Position.fromDegrees(controlPoint.getLat(), controlPoint.getLon(), altitude);
            var radius = 0.6;
            var pyramid = new Pyramid(p, radius * 1.0, radius * 1.0);

            pyramid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(mEllipsoidLayer, pyramid);

            for (var cp2 : convergencePoint.ext2().getControlPoints()) {
                if (cp2 == controlPoint) {
                    continue;
                }
                var altitude2 = cp2.getZeroZ() + offset;
                var p2 = Position.fromDegrees(cp2.getLat(), cp2.getLon(), altitude2);
                var groundPath = new Path(p, p2);
                groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
                addRenderable(mGroundConnectorLayer, groundPath);
            }
        }
    }

}
