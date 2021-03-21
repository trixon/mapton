/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.demo.ww;

import gov.nasa.worldwind.geom.Sector;
import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.SurfaceImage;
import org.mapton.worldwind.api.LayerBundle;

/**
 *
 * @author Patrik Karlström
 */
@org.openide.util.lookup.ServiceProvider(service = LayerBundle.class)
public class TrixonLayerBundle extends LayerBundle {

    private final RenderableLayer mRenderableLayer = new RenderableLayer();

    public TrixonLayerBundle() {
        SurfaceImage surfaceImage = new SurfaceImage("https://trixon.se/files/pata.jpg", Sector.FULL_SPHERE);
        mRenderableLayer.setName("trixon");
        mRenderableLayer.setEnabled(true);
        mRenderableLayer.addRenderable(surfaceImage);
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mRenderableLayer);
        setPopulated(true);
    }
}
