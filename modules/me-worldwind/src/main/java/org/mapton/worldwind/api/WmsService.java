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
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.WWUtil;
import java.net.URI;
import java.util.ArrayList;
import java.util.TreeSet;
import org.mapton.api.MWmsSource;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public abstract class WmsService {

    private final TreeSet<LayerInfo> mLayerInfos = new TreeSet<>((LayerInfo o1, LayerInfo o2) -> o1.getName().compareTo(o2.getName()));
    private final ArrayList<Layer> mLayers = new ArrayList<>();
    private boolean mPopulated = false;

    public static WmsService createFromWmsSource(MWmsSource wmsSource) {
        WmsService wmsService = new WmsService() {
            @Override
            public String getName() {
                return wmsSource.getName();
            }

            @Override
            public void populate() throws Exception {
                var url = Mapton.replaceSubstring(wmsSource.getUrl());
                addService(new URI(url));

                for (var layerInfo : getLayerInfos()) {
                    if (wmsSource.getLayers().keySet().contains(layerInfo.getName()) || wmsSource.getLayers().values().contains(layerInfo.getName())) {
                        var component = createComponent(layerInfo.getWmsCapabilities(), layerInfo.getParams());
                        if (component instanceof Layer layer) {
                            layer.setName(wmsSource.getLayerName(layerInfo.getName()));
                            getLayers().add(layer);
                        }
                    }
                }

                setPopulated(true);
            }
        };

        return wmsService;
    }

    public WmsService() {
    }

    public TreeSet<LayerInfo> getLayerInfos() {
        return mLayerInfos;
    }

    public ArrayList<Layer> getLayers() {
        return mLayers;
    }

    public abstract String getName();

    public boolean isPopulated() {
        return mPopulated;
    }

    public abstract void populate() throws Exception;

    public void setPopulated(boolean populated) {
        mPopulated = populated;
    }

    protected void addService(URI uri) throws Exception {
        var wmsCapabilities = WMSCapabilities.retrieve(uri);
        wmsCapabilities.parse();

        for (var wmsLayerCapabilities : wmsCapabilities.getNamedLayers()) {
            var wmsLayerStyles = wmsLayerCapabilities.getStyles();
            if (wmsLayerStyles == null || wmsLayerStyles.isEmpty()) {
                mLayerInfos.add(new LayerInfo(wmsCapabilities, wmsLayerCapabilities, null));
            } else {
                for (var wmsLayerStyle : wmsLayerStyles) {
                    mLayerInfos.add(new LayerInfo(wmsCapabilities, wmsLayerCapabilities, wmsLayerStyle));
                }
            }
        }
    }

    protected Object createComponent(WMSCapabilities wmsCapabilities, AVList params) {
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

    protected class LayerInfo {

        private final AVListImpl mParams = new AVListImpl();
        private final WMSCapabilities mWmsCapabilities;

        public LayerInfo(WMSCapabilities wmsCapabilities, WMSLayerCapabilities wmsLayerCapabilities, WMSLayerStyle wmsLayerstyle) {
            mWmsCapabilities = wmsCapabilities;
            mParams.setValue(AVKey.LAYER_NAMES, wmsLayerCapabilities.getName());
            if (wmsLayerstyle != null) {
                mParams.setValue(AVKey.STYLE_NAMES, wmsLayerstyle.getName());
            }
            String abs = wmsLayerCapabilities.getLayerAbstract();
            if (!WWUtil.isEmpty(abs)) {
                mParams.setValue(AVKey.LAYER_ABSTRACT, abs);
            }

        }

        public String getAbstract() {
            return mParams.getStringValue(AVKey.LAYER_ABSTRACT);
        }

        public String getName() {
            return mParams.getStringValue(AVKey.LAYER_NAMES);
        }

        public AVListImpl getParams() {
            return mParams;
        }

        public String getTitle() {
            return mParams.getStringValue(AVKey.DISPLAY_NAME);
        }

        public WMSCapabilities getWmsCapabilities() {
            return mWmsCapabilities;
        }
    }
}
