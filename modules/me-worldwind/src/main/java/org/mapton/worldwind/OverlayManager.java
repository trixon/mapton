/*
 * Copyright 2024 Patrik Karlström.
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

import gov.nasa.worldwind.layers.Layer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import static org.mapton.api.MKey.DATA_SOURCES_WMS_SOURCES;
import org.mapton.api.MWmsSource;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.WmsLayerLoader;

/**
 *
 * @author Patrik Karlström
 */
public class OverlayManager {

    private final ObservableList<String> mAvailableOverlays;
    private final LinkedHashMap<String, Layer> mIdToLayerMap = new LinkedHashMap<>();
    private final WmsLayerLoader mWmsLayerLoader = new WmsLayerLoader();

    public static OverlayManager getInstance() {
        return Holder.INSTANCE;
    }

    private OverlayManager() {
        mAvailableOverlays = FXCollections.<String>observableArrayList();
    }

    public ObservableList<String> getAvailableOverlays() {
        return mAvailableOverlays;
    }

    public LinkedHashMap<String, Layer> getIdToLayerMap() {
        return mIdToLayerMap;
    }

    public void populateOverlayLayers() {
        var wmsSources = Mapton.getGlobalState().<ArrayList<MWmsSource>>get(DATA_SOURCES_WMS_SOURCES);

        if (wmsSources != null) {
            var availableOverlays = new ArrayList<String>();

            wmsSources.stream()
                    .filter(wms -> !wms.getOverlays().isEmpty())
                    .forEachOrdered(wmsSource -> {
                        wmsSource.getLayers().forEach((String key, String id) -> {
                            if (wmsSource.getOverlays().contains(id)) {
                                var layer = mWmsLayerLoader.load("fix_this_id", wmsSource.getUrl(), key);
                                if (layer != null) {
                                    layer.setValue(ModuleOptions.KEY_MAP_OVERLAYS, "1");
                                    mIdToLayerMap.put(id, layer);
                                    availableOverlays.add(id);
                                }
                            }
                        });
                    });

            mAvailableOverlays.setAll(availableOverlays);
        }
    }

    private static class Holder {

        private static final OverlayManager INSTANCE = new OverlayManager();
    }
}
