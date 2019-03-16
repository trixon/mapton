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
package org.mapton.ww_lm.wms;

import gov.nasa.worldwind.layers.Layer;
import java.net.URI;
import org.apache.commons.lang3.StringUtils;
import org.mapton.worldwind.api.WmsService;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = WmsService.class)
public class LmWms extends WmsService {

    public LmWms() {
    }

    @Override
    public String getName() {
        return "Lantmäteriet";
    }

    @Override
    public void populate() throws Exception {
        addService(new URI("https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms"));

        for (LayerInfo layerInfo : getLayerInfos()) {
            if (StringUtils.equalsAny(layerInfo.getName(),
                    "OI.Histortho_60",
                    "OI.Histortho_75"
            )) {
                Object component = createComponent(layerInfo.getWmsCapabilities(), layerInfo.getParams());
                if (component instanceof Layer) {
                    getLayers().add((Layer) component);
                }
            }
        }

        setPopulated(true);
    }
}
