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

import gov.nasa.worldwind.avlist.AVKey;
import gov.nasa.worldwind.avlist.AVListImpl;
import gov.nasa.worldwind.ogc.wms.WMSCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerCapabilities;
import gov.nasa.worldwind.ogc.wms.WMSLayerStyle;
import gov.nasa.worldwind.util.WWUtil;

/**
 *
 * @author Patrik Karlström
 */
public class LayerInfo {

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
