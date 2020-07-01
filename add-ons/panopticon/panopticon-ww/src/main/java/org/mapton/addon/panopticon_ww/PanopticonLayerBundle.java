/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.panopticon_ww;

import gov.nasa.worldwind.layers.RenderableLayer;
import org.mapton.addon.panopticon.api.Panopticon;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class PanopticonLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();

    public PanopticonLayerBundle() {
        mLayer.setName(Panopticon.NAME);
        setCategoryAddOns(mLayer);
        setName(Panopticon.NAME);

        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(0);
    }

    private void init() {
        mLayer.setPickEnabled(true);
        attachTopComponentToLayer("PanopticonTopComponent", mLayer);
    }

    private void initListeners() {
    }

    private void initRepaint() {
        setPainter(() -> {
            mLayer.removeAllRenderables();
        });
    }
}
