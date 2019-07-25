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
package org.mapton.wms.lantmateriet;

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
        layers.put("OI.Histortho_60", "se.lm.hist_orto_60");
        layers.put("OI.Histortho_75", "se.lm.hist_orto_75");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("Lantmäteriet");
        attribution.setProviderUrl("https://opendata.lantmateriet.se/#apis?api=Historiska-ortofoton_WMS&version=v1");
        attribution.setLicenseName("Creative Commons - CC0");
        attribution.setLicenseUrl("https://creativecommons.org/publicdomain/zero/1.0/");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("se.lm.hist_orto_60", attribution);
        attributions.put("se.lm.hist_orto_75", attribution);

        mSources.add(createSource(
                "Lantmäteriet",
                "https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms",
                layers,
                attributions,
                enabled
        ));
    }
}
