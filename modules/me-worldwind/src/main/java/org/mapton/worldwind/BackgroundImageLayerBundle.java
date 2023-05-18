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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.RenderableLayer;
import gov.nasa.worldwind.render.ScreenImage;
import javafx.scene.Node;
import org.mapton.api.MBackgroundImage;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class BackgroundImageLayerBundle extends LayerBundle {

    private MBackgroundImage mBackgroundImage;
    private final RenderableLayer mLayer = new RenderableLayer();
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private BackgroundImageOptionsView mOptionsView;
    private final ScreenImage mScreenImage = new ScreenImage();

    public BackgroundImageLayerBundle() {
        init();
        initRepaint();
        initListeners();
    }

    @Override
    public Node getOptionsView() {
        if (mOptionsView == null) {
            mOptionsView = new BackgroundImageOptionsView();
        }

        return mOptionsView;
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        repaint(0);
    }

    private void init() {
        mLayer.setName(Dict.BACKGROUND_IMAGE.toString());
        setCategorySystem(mLayer);
        setName(Dict.IMAGE.toString());
        mLayer.setEnabled(true);
        mLayer.setPickEnabled(false);
        setParentLayer(mLayer);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            mBackgroundImage = gsce.getValue();
            repaint();
        }, MKey.BACKGROUND_IMAGE);

        mOptions.getPreferences().addPreferenceChangeListener(pce -> {
            repaint();
        });
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            if (mBackgroundImage != null && mBackgroundImage.getImageSource() != null) {
                mLayer.addRenderable(mScreenImage);
                synchronized (this) {
                    mScreenImage.setImageSource(mBackgroundImage.getImageSource());
                    try {
                        mScreenImage.setOpacity(mOptions.getDouble(KEY_BACKGROUND_IMAGE_OPACITY, DEFAULT_BACKGROUND_IMAGE_OPACITY));
                    } catch (Exception e) {
                    }
                }
            }

            setDragEnabled(false);
        });
    }
}
