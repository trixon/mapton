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
import java.util.TreeMap;
import org.mapton.api.MWmsStyle;

/**
 *
 * @author Patrik Karlström
 */
public class WmsStyleGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new WmsStyleGenerator();
    }

    public WmsStyleGenerator() {
        ArrayList<MWmsStyle> styles = new ArrayList<>();
        TreeMap<String, String> names1 = new TreeMap<>();
        names1.put("", "Swe LM Ortho 1960");
        names1.put("sv", "Sve LM Orto 1960");

        styles.add(createStyle(
                names1,
                "Lantmäteriet",
                "",
                true,
                "se.lm.hist_orto_60"
        ));

        TreeMap<String, String> names2 = new TreeMap<>();
        names2.put("", "Swe LM Ortho 1975");
        names2.put("sv", "Sve LM Orto 1975");
        styles.add(createStyle(
                names2,
                "Lantmäteriet",
                "",
                true,
                "se.lm.hist_orto_75"
        ));

        TreeMap<String, String> names3 = new TreeMap<>();
        names3.put("", "Coastline & Hydrography");
        names3.put("sv", "Kustlinje & hydrografi");
        styles.add(createStyle(
                names3,
                "EOX",
                "",
                true,
                "at.eox.coastline",
                "at.eox.hydrography"
        ));

        TreeMap<String, String> names4 = new TreeMap<>();
        names4.put("", "OpenStreetMap");
        styles.add(createStyle(
                names4,
                "EOX",
                "",
                true,
                "at.eox.osm"
        ));

        TreeMap<String, String> names5 = new TreeMap<>();
        names5.put("", "Satellite, Sentinel-2");
        names5.put("sv", "Satellit, Sentinel-2");
        styles.add(createStyle(
                names5,
                "EOX & ESA",
                "",
                true,
                "at.eox.s2cloudless-2018"
        ));

        TreeMap<String, String> names6 = new TreeMap<>();
        names6.put("", "Terrain");
        names6.put("sv", "Terräng");
        styles.add(createStyle(
                names6,
                "EOX & ESA",
                "",
                true,
                "at.eox.terrain"
        ));

        TreeMap<String, String> names7 = new TreeMap<>();
        names7.put("", "Terrain (light)");
        names7.put("sv", "Terräng (ljus)");
        styles.add(createStyle(
                names7,
                "EOX & ESA",
                "",
                true,
                "at.eox.terrain-light"
        ));

        TreeMap<String, String> names8 = new TreeMap<>();
        names8.put("", "Earth by night");
        names8.put("sv", "Jorden på natten");
        styles.add(createStyle(
                names8,
                "EOX & ESA",
                "",
                true,
                "at.eox.blackmarble"
        ));

        TreeMap<String, String> names9 = new TreeMap<>();
        names9.put("", "Satellite & Street, Sentinel-2");
        names9.put("sv", "Satellit & väg, Sentinel-2");
        styles.add(createStyle(
                names9,
                "EOX & ESA",
                "",
                true,
                "at.eox.streets",
                "at.eox.s2cloudless-2018"
        ));

        TreeMap<String, String> names10 = new TreeMap<>();
        names10.put("", "NEO UV Index");
        names10.put("sv", "NEO UV-index");
        styles.add(createStyle(
                names10,
                "NASA",
                "",
                true,
                "gov.nasa.neo.uv"
        ));

        String json = Initializer.gson.toJson(styles);
        System.out.println(json);
    }

    private MWmsStyle createStyle(TreeMap<String, String> names, String supplier, String description, boolean enabled, String... layers) {
        MWmsStyle style = new MWmsStyle();
        style.setNames(names);
        style.setEnabled(enabled);
        style.setSupplier(supplier);
        style.setDescription(description);
        style.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return style;
    }
}
