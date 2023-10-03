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
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.ScreenImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class LogoLayerBundle extends LayerBundle {

    private final int STARTUP_DELAY = 10000;
    private final RenderableLayer mLayer = new RenderableLayer();

    public LogoLayerBundle() {
        mLayer.setPickEnabled(false);
        setVisibleInLayerManager(mLayer, false);
        initRepaint();
        repaint(STARTUP_DELAY);
    }

    @Override
    public void populate() {
        getLayers().addAll(mLayer);
        setPopulated(true);
        mLayer.setEnabled(true);
    }

    private void initRepaint() {
        setPainter(() -> {
            removeAllRenderables();
            var screenImage = new ScreenImage();
            try {
                URL url = Mapton.getGlobalState().get(MKey.MAP_LOGO_URL);
                screenImage.setImageSource(ImageIO.read(url));
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

            screenImage.setImageOffset(Offset.fromFraction(-.1, -.1));
            screenImage.setScreenOffset(Offset.fromFraction(0.0, 0.0));
            mLayer.addRenderable(screenImage);
        });
    }
}
