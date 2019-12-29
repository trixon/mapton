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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import org.mapton.api.MBackgroundImage;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class BackgroundImageLayer extends LayerBundle {

    private final RenderableLayer mLayer = new RenderableLayer();
    private final ScreenImage mScreenImage = new ScreenImage();

    public BackgroundImageLayer() {
        init();
        initListeners();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        setPopulated(true);
    }

    private void init() {
        mLayer.setName(Dict.BACKGROUND_IMAGE.toString());
        setCategorySystem(mLayer);
        setName(Dict.IMAGE.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(false);
        mLayer.addRenderable(mScreenImage);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            MBackgroundImage backgroundImage = evt.getValue();
            mScreenImage.setImageSource(backgroundImage.getImageSource());
            mScreenImage.setOpacity(backgroundImage.getOpacity());

            refresh();
        }, MKey.BACKGROUND_IMAGE);
    }

    private void refresh() {
        try {
            LayerBundleManager.getInstance().redraw();
        } catch (Exception e) {
            //nvm
        }
    }
}
