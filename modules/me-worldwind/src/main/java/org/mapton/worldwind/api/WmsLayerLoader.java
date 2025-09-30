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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.Factory;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVList;
import gov.nasa.worldwind.exception.WWRuntimeException;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class WmsLayerLoader {

    private final HashMap<String, WMSCapabilities> mUrlToWmsCapabilities = new HashMap<>();

    public WmsLayerLoader() {
    }

    public Layer load(String id, String url, String layerName) {
        url = Mapton.replaceSubstring(url);

        var wmsCapabilities = mUrlToWmsCapabilities.computeIfAbsent(url, k -> {
            try {
                var capabilities = WMSCapabilities.retrieve(new URI(k));
                capabilities.parse();
                return capabilities;
            } catch (WWRuntimeException ex) {
                System.err.println("Failed to load %s\t%s.".formatted(id, layerName));
                Exceptions.printStackTrace(ex);
            } catch (URISyntaxException ex) {
                System.err.println("Failed to load %s\t%s.".formatted(id, layerName));
                Exceptions.printStackTrace(ex);
            } catch (Exception ex) {
                System.err.println("Failed to load %s\t%s.".formatted(id, layerName));
                Exceptions.printStackTrace(ex);
            }
            return null;
        });

        if (wmsCapabilities != null) {
            var wmsLayerCapabilities = wmsCapabilities.getLayerByName(layerName);
            try {
                var wmsLayerStyles = wmsLayerCapabilities.getStyles();
                var layerInfo = new LayerInfo(wmsCapabilities, wmsLayerCapabilities, null);
                var component = createComponent(layerInfo.getWmsCapabilities(), layerInfo.getParams());
                if (component instanceof Layer layer) {
                    layer.setName(id);
                    return layer;
                }
            } catch (Exception e) {
                System.err.println(e);
                return null;
            }
        }

        return null;
    }

    private Object createComponent(WMSCapabilities wmsCapabilities, AVList params) {
        var configParams = params.copy();

        configParams.setValue(AVKey.URL_CONNECT_TIMEOUT, 30000);
        configParams.setValue(AVKey.URL_READ_TIMEOUT, 30000);
        configParams.setValue(AVKey.RETRIEVAL_QUEUE_STALE_REQUEST_LIMIT, 60000);

        try {
            var factoryKey = getFactoryKeyForCapabilities(wmsCapabilities);
            var factory = (Factory) WorldWind.createConfigurationComponent(factoryKey);

            return factory.createFromConfigSource(wmsCapabilities, configParams);
        } catch (IllegalArgumentException | IllegalStateException e) {
            // nvm
        }

        return null;
    }

    private String getFactoryKeyForCapabilities(WMSCapabilities wmsCapabilities) {
        boolean hasApplicationBilFormat = false;

        for (var format : wmsCapabilities.getImageFormats()) {
            if (format.contains("application/bil")) {
                hasApplicationBilFormat = true;
                break;
            }
        }

        return hasApplicationBilFormat ? AVKey.ELEVATION_MODEL_FACTORY : AVKey.LAYER_FACTORY;
    }

}
