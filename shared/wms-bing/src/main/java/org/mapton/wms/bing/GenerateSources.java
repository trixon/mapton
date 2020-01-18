/* 
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.wms.bing;

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
        layers.put("ve-a", "com.microsoft.ve-a");
        layers.put("ve-h", "com.microsoft.ve-h");
        layers.put("ve-r", "com.microsoft.ve-r");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Bing (NASA)");
        attribution.setProviderUrl("https://nasa.gov");
        attribution.setLicenseName("Microsoft Bing Maps ToU");
        attribution.setLicenseUrl("https://www.microsoft.com/maps/assets/docs/terms.aspx/");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("com.microsoft.ve-a", attribution);
        attributions.put("com.microsoft.ve-h", attribution);
        attributions.put("com.microsoft.ve-r", attribution);

        mSources.add(createSource(
                "Bing",
                "https://worldwind27.arc.nasa.gov/wms/virtualearth",
                layers,
                attributions,
                enabled
        ));
    }
}
