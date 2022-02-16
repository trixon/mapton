/*
 * Copyright 2022 Patrik Karlström.
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
import java.util.ArrayList;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
public class IndicatorLayerBundle extends LayerBundle {

    private GlobalStateChangeEvent mGsce;
    private final RenderableLayer mLayer = new RenderableLayer();

    public IndicatorLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() {
        getLayers().add(mLayer);
    }

    private void init() {
        setCategorySystem(mLayer);
        setName(Dict.INDICATORS.toString());
        mLayer.setName(Dict.INDICATORS.toString());
        mLayer.setPickEnabled(false);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            mGsce = gsce;
            repaint(0);
        }, MKey.INDICATOR_LAYER_LOAD);
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            if (mGsce.getValue() instanceof Renderable renderable) {
                mLayer.addRenderable(renderable);
            } else if (mGsce.getValue() instanceof Renderable[] renderables) {
                for (var renderable : renderables) {
                    mLayer.addRenderable(renderable);
                }
            } else if (mGsce.getValue() instanceof ArrayList) {
                ArrayList<?> arrayList = mGsce.getValue();
                if (!arrayList.isEmpty() && arrayList.get(0) instanceof Renderable) {
                    for (var renderable : (ArrayList<Renderable>) arrayList) {
                        mLayer.addRenderable(renderable);
                    }
                }
            }

            setDragEnabled(false);
        });
    }
}
