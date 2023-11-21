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
import gov.nasa.worldwind.render.Ellipsoid;
import gov.nasa.worldwind.render.Path;
import gov.nasa.worldwind.render.Renderable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.acoustic.BAcoBlast;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ComponentRenderer {

    private final BlastAttributeManager mAttributeManager = BlastAttributeManager.getInstance();
    private final RenderableLayer mLayer;
    private ArrayList<AVListImpl> mMapObjects;

    public ComponentRenderer(RenderableLayer layer) {
        mLayer = layer;
    }

    public void addRenderable(Renderable renderable, boolean interactiveLayer) {
        if (interactiveLayer) {
            mLayer.addRenderable(renderable);
            if (renderable instanceof AVListImpl avlist) {
                mMapObjects.add(avlist);
            }
        } else {
            //mLayerXYZ.addRenderable(renderable); //TODO Add to a non responsive layer
        }
    }

    public void plot(BAcoBlast p, Position position, ArrayList<AVListImpl> mapObjects) {
        mMapObjects = mapObjects;

        var timeSpan = ChronoUnit.MINUTES.between(p.getDateTime(), LocalDateTime.now());
        var altitude = timeSpan / 24000.0;
        var startPosition = WWHelper.positionFromPosition(position, 0.0);
        var endPosition = WWHelper.positionFromPosition(position, altitude);
        var radius = 1.2;
        var endEllipsoid = new Ellipsoid(endPosition, radius, radius, radius);
        endEllipsoid.setAttributes(mAttributeManager.getComponentEllipsoidAttributes());
        addRenderable(endEllipsoid, true);

        var groundPath = new Path(startPosition, endPosition);
        groundPath.setAttributes(mAttributeManager.getComponentGroundPathAttributes());
        addRenderable(groundPath, true);
    }

    public void reset() {
    }

}
