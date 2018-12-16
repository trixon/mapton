/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.ww_mapollage;

import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.layers.RenderableLayer;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.LayerBundleManager;
import static org.mapton.ww_mapollage.Options.*;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = LayerBundle.class)
public class MapollageController extends LayerBundle {

    private int mAltitudeMode;
    private final RenderableLayer mLayer = new RenderableLayer();
    private final Options mOptions = Options.getInstance();

    public MapollageController() {
        mLayer.setName("Mapollage");
        init();
    }

    @Override
    public void populate() throws Exception {
        getLayers().add(mLayer);
        refresh();

        mOptions.getPreferences().addPreferenceChangeListener((event) -> {
            refresh();
        });
    }

    private void init() {
    }

    private void refresh() {
        mLayer.removeAllRenderables();
        mAltitudeMode = mOptions.is(KEY_GLOBAL_CLAMP_TO_GROUND) ? WorldWind.CLAMP_TO_GROUND : WorldWind.ABSOLUTE;

        LayerBundleManager.getInstance().redraw();
    }
}
