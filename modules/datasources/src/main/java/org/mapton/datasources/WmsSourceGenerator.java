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
import java.util.Arrays;
import org.mapton.api.MWmsSource;
import se.trixon.almond.util.SystemHelper;

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

        sources.add(createSource(
                "Lantmäteriet",
                "https://api.lantmateriet.se/historiska-ortofoton/wms/v1/token/6633c97e-a9b3-3f0f-95d1-4b50401ac8cd/?request=getcapabilities&service=wms",
                "OI.Histortho_60",
                "OI.Histortho_75"
        ));

        sources.add(createSource(
                "EOX",
                "https://tiles.maps.eox.at/wms?service=wms&request=getcapabilities",
                "coastline",
                "hydrography",
                "s2cloudless-2018",
                "osm",
                "terrain",
                "terrain-light"
        ));

        String json = Initializer.gson.toJson(sources);
        System.out.println(json);
        SystemHelper.copyToClipboard(json);
    }

    private MWmsSource createSource(String name, String url, String... layers) {
        MWmsSource source = new MWmsSource();
        source.setName(name);
        source.setUrl(url);
        source.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return source;
    }
}
