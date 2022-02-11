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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.WorldWindow;
import gov.nasa.worldwind.layers.Layer;
import org.mapton.api.MEngine;
import org.mapton.worldwind.WorldWindMapEngine;
import org.mapton.worldwind.WorldWindowPanel;
import se.trixon.almond.util.swing.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class LayerBundleManager {

    private final DelayedResetRunner mDelayedResetRunner;
    private WorldWindMapEngine mEngine;
    private WorldWindowPanel mMap;

    public static LayerBundleManager getInstance() {
        return Holder.INSTANCE;
    }

    private LayerBundleManager() {
        mDelayedResetRunner = new DelayedResetRunner(50, () -> {
            try {
                getMap().redraw();
            } catch (Exception e) {
                //nvm Called before map was initialized
            }
        });
    }

    public void add(Layer layer) {
        getMap().addCustomLayer(layer);
    }

    public WorldWindow getWwd() {
        return mMap.getWwd();
    }

    public void redraw() {
        mDelayedResetRunner.reset();
    }

    public void remove(Layer layer) {
        getMap().removeCustomLayer(layer);
    }

    private WorldWindMapEngine getEngine() {
        if (mEngine == null) {
            mEngine = (WorldWindMapEngine) MEngine.byName("WorldWind");
        }
        return mEngine;
    }

    private WorldWindowPanel getMap() {
        if (mMap == null) {
            mMap = getEngine().getMap();
        }

        return mMap;
    }

    private static class Holder {

        private static final LayerBundleManager INSTANCE = new LayerBundleManager();
    }
}
