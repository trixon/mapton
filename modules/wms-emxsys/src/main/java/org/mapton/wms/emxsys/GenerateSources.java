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
package org.mapton.wms.emxsys;

import java.util.TreeMap;
import org.mapton.api.MAttribution;
import org.mapton.api.MWmsGenerator;

/**
 *
 * @author Patrik Karlström
 */
public class GenerateSources extends MWmsGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GenerateSources();
    }

    public GenerateSources() {
        init(true);

        System.out.println(getSourceJson());
    }

    private void init(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("ve-a", "net.emxsys.ve-a");
        layers.put("ve-h", "net.emxsys.ve-h");
        layers.put("ve-r", "net.emxsys.ve-r");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Bing");
        attribution.setProviderUrl("http://emxsys.com");
        attribution.setLicenseName("\"Unknown\"");
        attribution.setLicenseUrl("https://example.org/");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("net.emxsys.ve-a", attribution);
        attributions.put("net.emxsys.ve-h", attribution);
        attributions.put("net.emxsys.ve-r", attribution);

        mSources.add(createSource(
                "Bing",
                "https://emxsys.net/worldwind27/wms/virtualearth?request=GetCapabilities&service=WMS",
                layers,
                attributions,
                enabled
        ));
    }
}
