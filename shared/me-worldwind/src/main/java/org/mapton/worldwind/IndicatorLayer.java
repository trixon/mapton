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

/**
 *
 * @author Patrik Karlström
 */
public class IndicatorLayer extends RenderableLayer {

    public IndicatorLayer() {
        setName("Indicator");
        setPickEnabled(false);

        init();
        initListeners();
    }

    private void init() {
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsc -> {
            removeAllRenderables();
            if (gsc.getValue() instanceof Renderable) {
                addRenderable(gsc.getValue());
            }
        }, MKey.INDICATOR_LAYER_LOAD);
    }
}
