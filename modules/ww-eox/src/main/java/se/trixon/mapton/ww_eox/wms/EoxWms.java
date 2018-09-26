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
package se.trixon.mapton.ww_eox.wms;

import gov.nasa.worldwind.layers.Layer;
import java.net.URI;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.worldwind.api.WmsService;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = WmsService.class)
public class EoxWms extends WmsService {

    public EoxWms() {
    }

    @Override
    public void populate() throws Exception {
        addService(new URI("https://tiles.maps.eox.at/wms?service=wms&request=getcapabilities"));

        for (LayerInfo layerInfo : getLayerInfos()) {
            if (layerInfo.getName().equalsIgnoreCase("osm")) {
                Object component = createComponent(layerInfo.getWmsCapabilities(), layerInfo.getParams());
                if (component instanceof Layer) {
                    getLayers().add((Layer) component);
                }
            }
        }

        setPopulated(true);
    }
}
