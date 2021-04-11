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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.RenderableLayer;
import org.mapton.worldwind.api.CoordinateFileRendererWW;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class FilesLayerBundle extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();

    public FilesLayerBundle() {
        init();
        initRepaint();
        initListeners();
        initRenderers();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(DEFAULT_REPAINT_DELAY);
    }

    private void init() {
        setParentLayer(mLayer);
        mLayer.setName(Dict.FILES.toString());
        setCategorySystem(mLayer);
        setName(Dict.FILES.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(true);
        attachTopComponentToLayer("FilesTopComponent", mLayer);
    }

    private void initListeners() {
        var result = Lookup.getDefault().lookupResult(CoordinateFileRendererWW.class);
        result.addLookupListener(lookupEvent -> {
            //initRenderers();
        });
    }

    private void initRenderers() {
        for (var coordinateFileRendererWW : Lookup.getDefault().lookupAll(CoordinateFileRendererWW.class)) {
            coordinateFileRendererWW.init(this);
        }
    }

    private void initRepaint() {
        setPainter(() -> {
        });
    }
}
