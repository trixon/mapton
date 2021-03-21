/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.wms.nasa;

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
        //initSwedGeo(false);
        init(true);

        System.out.println(getSourceJson());
    }

    private void init(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("AURA_UVI_CLIM_M", "gov.nasa.neo.uv");

        MAttribution attribution = new MAttribution();
        attribution.setProviderName("NASA Earth Observations");
        attribution.setProviderUrl("https://neo.sci.gsfc.nasa.gov/view.php?datasetId=AURA_UVI_CLIM_M");
        attribution.setLicenseName("\"Freely available for public use\"");
        attribution.setLicenseUrl("https://neo.sci.gsfc.nasa.gov/about/");
        attribution.setRawHtml("Imagery by Jesse Allen, NASA Earth Observatory, based on data provided by Jerry Ziemke from the Ozone Monitoring Instrument (OMI) science team.");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("gov.nasa.neo.uv", attribution);

        mSources.add(createSource(
                "NEO",
                "https://neo.sci.gsfc.nasa.gov/wms/wms?version=1.3.0&service=WMS&request=GetCapabilities",
                layers,
                attributions,
                enabled
        ));
    }

    private void initSwedGeo(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("orto025", "se.lm.orto_025");
        layers.put("topowebbkartan", "se.lm.topoweb");
        layers.put("topowebbkartan_nedtonad", "se.lm.topoweb_dim");
        layers.put("fastighet_text", "se.lm.fastighet_text");
        layers.put("fastighet_granser", "se.lm.fastighet_granser");
        layers.put("terrangskuggning", "se.lm.terrangskuggning");

        MAttribution attribution = new MAttribution();

        TreeMap<String, MAttribution> attributions = new TreeMap<>();

        mSources.add(createSource(
                "Lantmäteriet",
                "http://gis.swedgeo.se/geoserver/lantmateriet/wms?",
                layers,
                attributions,
                enabled
        ));
    }
}
