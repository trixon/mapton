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
package org.mapton.datasources;

import java.util.ArrayList;
import java.util.TreeMap;
import org.mapton.api.MWmsSource;

/**
 *
 * @author Patrik Karlström
 */
public class WmsSourceGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new WmsSourceGenerator();
    }

    public WmsSourceGenerator() {
        ArrayList<MWmsSource> sources = new ArrayList<>();
        TreeMap<String, String> layers1 = new TreeMap<>();
        layers1.put("OI.Histortho_60", "se.lm.hist_orto_60");
        layers1.put("OI.Histortho_75", "se.lm.hist_orto_75");
        sources.add(createSource(
                "Lantmäteriet",
                "https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms",
                layers1,
                true
        ));

        TreeMap<String, String> layers2 = new TreeMap<>();
        layers2.put("blackmarble", "at.eox.blackmarble");
        layers2.put("coastline", "at.eox.coastline");
        layers2.put("hydrography", "at.eox.hydrography");
        layers2.put("osm", "at.eox.osm");
        layers2.put("s2cloudless-2018", "at.eox.s2cloudless-2018");
        layers2.put("streets", "at.eox.streets");
        layers2.put("terrain", "at.eox.terrain");
        layers2.put("terrain-light", "at.eox.terrain-light");
        sources.add(createSource(
                "EOX",
                "https://tiles.maps.eox.at/wms?service=wms&request=getcapabilities",
                layers2,
                true
        ));

        TreeMap<String, String> layers3 = new TreeMap<>();
        layers3.put("AURA_UVI_CLIM_M", "gov.nasa.neo.uv");
        sources.add(createSource(
                "NEO",
                "https://neo.sci.gsfc.nasa.gov/wms/wms?version=1.3.0&service=WMS&request=GetCapabilities",
                layers3,
                true
        ));

        String json = Initializer.gson.toJson(sources);
        System.out.println(json);
    }

    private MWmsSource createSource(String name, String url, TreeMap<String, String> layers, boolean enabled) {
        MWmsSource source = new MWmsSource();
        source.setName(name);
        source.setEnabled(enabled);
        source.setUrl(url);
        source.setLayers(layers);

        return source;
    }
}
