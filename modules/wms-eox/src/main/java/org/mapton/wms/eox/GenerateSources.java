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
package org.mapton.wms.eox;

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
        layers.put("blackmarble", "at.eox.blackmarble");
        layers.put("bluemarble", "at.eox.bluemarble");
        layers.put("coastline", "at.eox.coastline");
        layers.put("hydrography", "at.eox.hydrography");
        layers.put("osm", "at.eox.osm");
        layers.put("s2cloudless-2020", "at.eox.s2cloudless");
        layers.put("streets", "at.eox.streets");
        layers.put("terrain", "at.eox.terrain");
        layers.put("terrain-light", "at.eox.terrain-light");

        var s2Attribution = new MAttribution();
        s2Attribution.setOnlyRaw(true);
        s2Attribution.setRawHtml("<a href=\"https://s2maps.eu\">Sentinel-2 cloudless – https://s2maps.eu</a> by <a href=\"https://eox.at/\">EOX IT Services GmbH</a> (Contains modified Copernicus Sentinel data 2020)");

        var generalAttribution = new MAttribution();
        generalAttribution.setOnlyRaw(true);
        generalAttribution.setRawHtml("Data &copy; <a href=\"http://www.openstreetmap.org/copyright\">OpenStreetMap</a> contributors and <a href=\"https://maps.eox.at/#data\">others</a>, Rendering &copy; <a href=\"https://eox.at\">EOX</a>");

        var nasaAttribution = new MAttribution();
        nasaAttribution.setOnlyRaw(true);
        nasaAttribution.setRawHtml("Data &copy; <a href=\"https://neo.sci.gsfc.nasa.gov\">NASA</a>, Rendering &copy; <a href=\"https://eox.at\">EOX</a>");

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        attributions.put("at.eox.blackmarble", nasaAttribution);
        attributions.put("at.eox.bluemarble", nasaAttribution);

        attributions.put("at.eox.coastline", generalAttribution);
        attributions.put("at.eox.hydrography", generalAttribution);
        attributions.put("at.eox.osm", generalAttribution);
        attributions.put("at.eox.streets", generalAttribution);
        attributions.put("at.eox.terrain", generalAttribution);
        attributions.put("at.eox.terrain-light", generalAttribution);

        attributions.put("at.eox.s2cloudless", s2Attribution);

        mSources.add(createSource(
                "EOX",
                "http://tiles.maps.eox.at/wms?service=wms&request=getcapabilities",
                layers,
                attributions,
                enabled
        ));
    }
}
