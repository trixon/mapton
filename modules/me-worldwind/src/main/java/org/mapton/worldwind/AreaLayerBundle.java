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
import javafx.collections.ListChangeListener;
import org.mapton.api.MArea;
import org.mapton.api.MAreaManager;
import org.mapton.api.MDict;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class AreaLayerBundle extends LayerBundle {

    private final MAreaManager mAreaManager = MAreaManager.getInstance();
    private final RenderableLayer mLayer = new RenderableLayer();

    public AreaLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(2000);
    }

    private void init() {
        String name = MDict.AREAS.toString();
        mLayer.setName(name);
        setCategorySystem(mLayer);
        setName(name);
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        mAreaManager.getItems().addListener((ListChangeListener.Change<? extends MArea> c) -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();

            for (var area : mAreaManager.getItems()) {
            }

            setDragEnabled(false);
        });
    }
}
