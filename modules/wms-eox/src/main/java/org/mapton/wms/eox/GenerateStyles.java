/*
 * Copyright 2022 Patrik Karlström.
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
import org.mapton.api.MWmsGenerator;

/**
 *
 * @author Patrik Karlström
 */
public class GenerateStyles extends MWmsGenerator {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GenerateStyles();
    }

    public GenerateStyles() {
        initEoxEarthByDay(true);
        initEoxEarthByNight(true);
        initEoxHydro(true);
        initEoxOsm(true);
        initEoxSentinel2(true);
        initEoxSentinel2Streets(true);
        initEoxTerrain(true);
        initEoxTerrainLight(true);

        System.out.println(getStyleJson());
    }

    private void initEoxEarthByDay(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Earth by day");
        names.put("sv", "Jorden på dagen");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.bluemarble",
                "at.eox.bluemarble"
        ));
    }

    private void initEoxEarthByNight(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Earth by night");
        names.put("sv", "Jorden på natten");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.blackmarble",
                "at.eox.blackmarble"
        ));
    }

    private void initEoxHydro(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Coastline & Hydrography");
        names.put("sv", "Kustlinje & hydrografi");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX",
                enabled,
                "at.eox.coasthydro",
                "at.eox.coastline",
                "at.eox.hydrography"
        ));
    }

    private void initEoxOsm(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "OpenStreetMap");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "A global road map created by the\nOpenStreetMap (OSM) projekt.");
        descriptions.put("sv", "En global vägkarta skapad av\nprojektet OpenStreetMap (OSM).");

        mStyles.add(createStyle(
                null,
                names,
                descriptions,
                "EOX",
                enabled,
                "at.eox.osm",
                "at.eox.osm"
        ));
    }

    private void initEoxSentinel2(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Satellite");
        names.put("sv", "Satellit");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "World wide satellite imagery\nfrom ESA & EOX.");
        descriptions.put("sv", "Världstäckande satellitbild\nfrån ESA & EOX.");

        mStyles.add(createStyle(
                null,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.s2cloudless",
                "at.eox.s2cloudless"
        ));
    }

    private void initEoxSentinel2Streets(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Satellite & Street");
        names.put("sv", "Satellit & väg");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.streets",
                "at.eox.streets",
                "at.eox.s2cloudless"
        ));
    }

    private void initEoxTerrain(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Terrain");
        names.put("sv", "Terräng");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.terrain",
                "at.eox.terrain"
        ));
    }

    private void initEoxTerrainLight(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Terrain (light)");
        names.put("sv", "Terräng (ljus)");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "EOX & ESA",
                enabled,
                "at.eox.terrain-light",
                "at.eox.terrain-light"
        ));
    }

}
