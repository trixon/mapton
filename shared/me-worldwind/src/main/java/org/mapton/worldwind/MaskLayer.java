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
import gov.nasa.worldwind.render.Size;
import java.awt.Color;
import java.awt.Point;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.LayerBundleManager;

/**
 *
 * @author Patrik Karlström
 */
public class MaskLayer extends RenderableLayer {

    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private ScreenImage mScreenImage;

    public MaskLayer() {
        setName("Mask");
        setPickEnabled(false);

        init();
        initListeners();

        refresh();
    }

    private void init() {
        final int dim = 3000;
        Size size = Size.fromPixels(dim, dim);
        mScreenImage = new ScreenImage();
        mScreenImage.setScreenLocation(new Point(dim / 2, dim / 2));
        mScreenImage.setSize(size);

        addRenderable(mScreenImage);
    }

    private void initListeners() {
        mOptions.getPreferences().addPreferenceChangeListener(evt -> {
            switch (evt.getKey()) {
                case KEY_DISPLAY_MASK:
                case KEY_MASK_COLOR:
                case KEY_MASK_OPACITY:
                    refresh();
                    break;
            }
        });
    }

    private void refresh() {
        setEnabled(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));

        String colorString = mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR);
        float opacity = mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY);
        Color c = Color.decode("0x" + colorString);
        float[] a = c.getRGBComponents(null);
        Color c2 = new Color(a[0], a[1], a[2], opacity);

        mScreenImage.setColor(c2);
        try {
            LayerBundleManager.getInstance().redraw();
        } catch (Exception e) {
            //nvm
        }
    }
}
