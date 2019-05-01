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
package org.mapton.wms_sources;

import java.util.ArrayList;
import java.util.TreeMap;
import org.mapton.api.MWmsSource;

/**
 *
 * @author Patrik Karlström
 */
public class GenerateSources extends Generator {

    private final ArrayList<MWmsSource> mSources = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GenerateSources();
    }

    public GenerateSources() {
        initLantmateriet(true);
        initSwedGeo(false);
        initEOX(true);
        initNASA(true);

        String json = gson.toJson(mSources);
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

    private void initEOX(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("blackmarble", "at.eox.blackmarble");
        layers.put("coastline", "at.eox.coastline");
        layers.put("hydrography", "at.eox.hydrography");
        layers.put("osm", "at.eox.osm");
        layers.put("s2cloudless-2018", "at.eox.s2cloudless");
        layers.put("streets", "at.eox.streets");
        layers.put("terrain", "at.eox.terrain");
        layers.put("terrain-light", "at.eox.terrain-light");

        mSources.add(createSource(
                "EOX",
                "http://tiles.maps.eox.at/wms?service=wms&request=getcapabilities",
                layers,
                enabled
        ));
    }

    private void initLantmateriet(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("OI.Histortho_60", "se.lm.hist_orto_60");
        layers.put("OI.Histortho_75", "se.lm.hist_orto_75");

        mSources.add(createSource(
                "Lantmäteriet",
                "https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms",
                layers,
                enabled
        ));
    }

    private void initNASA(boolean enabled) {
        TreeMap<String, String> layers = new TreeMap<>();
        layers.put("AURA_UVI_CLIM_M", "gov.nasa.neo.uv");

        mSources.add(createSource(
                "NEO",
                "https://neo.sci.gsfc.nasa.gov/wms/wms?version=1.3.0&service=WMS&request=GetCapabilities",
                layers,
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

        mSources.add(createSource(
                "Lantmäteriet",
                "http://gis.swedgeo.se/geoserver/lantmateriet/wms?",
                layers,
                enabled
        ));
    }
}
