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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.Renderable;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class IndicatorLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();

    public IndicatorLayerBundle() {
        init();
        initListeners();
    }

    @Override
    public void populate() {
        getLayers().add(mLayer);
        setPopulated(true);
    }

    private void init() {
        setCategorySystem(mLayer);
        setName(Dict.INDICATORS.toString());
        mLayer.setName(Dict.INDICATORS.toString());
        mLayer.setPickEnabled(false);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsc -> {
            mLayer.removeAllRenderables();
            if (gsc.getValue() instanceof Renderable) {
                mLayer.addRenderable(gsc.getValue());
            }
        }, MKey.INDICATOR_LAYER_LOAD);
    }
}
