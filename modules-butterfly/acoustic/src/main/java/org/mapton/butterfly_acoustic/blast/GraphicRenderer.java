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
package org.mapton.butterfly_acoustic.blast;

import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.geom.Position;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.BasicShapeAttributes;
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import gov.nasa.worldwind.render.SurfaceCircle;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.butterfly_format.types.acoustic.BBlast;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GraphicRenderer {

    private final BlastAttributeManager mAttributeManager = BlastAttributeManager.getInstance();
    private final IndexedCheckModel<GraphicRendererItem> mCheckModel;
    private final RenderableLayer mEllipsoidLayer;
    private ArrayList<AVListImpl> mMapObjects;
    private final RenderableLayer mGroundConnectorLayer;
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

    public void plot(BBlast p, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;
        if (mCheckModel.isChecked(GraphicRendererItem.BALLS)) {

            var timeSpan = ChronoUnit.MINUTES.between(p.getDateTime(), LocalDateTime.now());
            var altitude = timeSpan / 24000.0;
            var startPosition = WWHelper.positionFromPosition(position, 0.0);
            var endPosition = WWHelper.positionFromPosition(position, altitude);
            var radius = 1.2;
            var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
            endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
            addRenderable(mEllipsoidLayer, endEllipsoid);

            var groundPath = new Path(startPosition, endPosition);
            groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
            addRenderable(mGroundConnectorLayer, groundPath);
        }

        if (mCheckModel.isChecked(GraphicRendererItem.RECENT)) {
            var age = p.ext().getAge(ChronoUnit.DAYS);
            var maxAge = 30.0;

            if (age < maxAge) {
                var circle = new SurfaceCircle(position, 100.0);
                var attrs = new BasicShapeAttributes(mAttributeManager.getSurfaceAttributes());
                var reducer = age / maxAge;//  1/30   15/30 30/30
                var maxOpacity = 0.2;
                var opacity = maxOpacity - reducer * maxOpacity;
                attrs.setInteriorOpacity(opacity);
                circle.setAttributes(attrs);

                addRenderable(mSurfaceLayer, circle);
            }
        }
    }

    public void reset() {
    }

}
