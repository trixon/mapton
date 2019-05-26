/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.ww_mapollage;

import gov.nasa.worldwind.layers.RenderableLayer;
import org.mapton.api.Mapton;
import org.mapton.mapollage.api.Mapo;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();

    public MapollageLayerBundle() {
        mLayer.setName("Mapollage-dev");
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            refresh();
        }, Mapo.KEY_COLLECTION);

        setPopulated(true);
    }

    private void refresh() {
    }
}
