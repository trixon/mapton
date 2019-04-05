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

    private ArrayList<MWmsStyle> mStyles = new ArrayList<>();
    private TreeMap<String, String> mCategoriesEarth = new TreeMap<>();
    private TreeMap<String, String> mCategoriesSwe = new TreeMap<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new WmsStyleGenerator();
    }

    public WmsStyleGenerator() {
        mCategoriesEarth.put("", "Earth");
        mCategoriesEarth.put("sv", "Jorden");
        mCategoriesSwe.put("", "Swedish");
        mCategoriesSwe.put("sv", "Sverige");

        initEoxEarthByNight(true);
        initEoxHydro(true);
        initEoxOsm(true);
        initEoxSentinel2(true);
        initEoxSentinel2Streets(true);
        initEoxTerrain(true);
        initEoxTerrainLight(true);

        initLmOrto025(true);
        initLmOrto1960(true);
        initLmOrto1975(true);
        initLmTopo(true);
        initLmTopoDim(true);

        initNasaUV(true);

        String json = Initializer.gson.toJson(mStyles);
        System.out.println(json);
    }

    private MWmsStyle createStyle(TreeMap<String, String> categories, TreeMap<String, String> names, String supplier, String description, boolean enabled, String... layers) {
        MWmsStyle style = new MWmsStyle();
        style.setCategories(categories);
        style.setNames(names);
        style.setEnabled(enabled);
        style.setSupplier(supplier);
        style.setDescription(description);
        style.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return style;
    }

    private void initEoxEarthByNight(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Earth by night");
        names.put("sv", "Jorden på natten");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX & ESA",
                "",
                enabled,
                "at.eox.blackmarble"
        ));
    }

    private void initEoxHydro(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Coastline & Hydrography");
        names.put("sv", "Kustlinje & hydrografi");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX",
                "",
                enabled,
                "at.eox.coastline",
                "at.eox.hydrography"
        ));
    }

    private void initEoxOsm(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "OpenStreetMap");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX",
                "",
                enabled,
                "at.eox.osm"
        ));
    }

    private void initEoxSentinel2(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Satellite, Sentinel-2");
        names.put("sv", "Satellit, Sentinel-2");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX & ESA",
                "",
                enabled,
                "at.eox.s2cloudless"
        ));
    }

    private void initEoxSentinel2Streets(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Satellite & Street, Sentinel-2");
        names.put("sv", "Satellit & väg, Sentinel-2");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX & ESA",
                "",
                enabled,
                "at.eox.streets",
                "at.eox.s2cloudless"
        ));
    }

    private void initEoxTerrain(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Terrain");
        names.put("sv", "Terräng");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX & ESA",
                "",
                enabled,
                "at.eox.terrain"
        ));
    }

    private void initEoxTerrainLight(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Terrain (light)");
        names.put("sv", "Terräng (ljus)");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "EOX & ESA",
                "",
                enabled,
                "at.eox.terrain-light"
        ));
    }

    private void initLmOrto025(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Swe LM Ortho 0.25m");
        names.put("sv", "Sve LM Orto 0,25m");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                "Lantmäteriet",
                "",
                enabled,
                "se.lm.orto_025"
        ));
    }

    private void initLmOrto1960(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Swe LM Ortho 1960");
        names.put("sv", "Sve LM Orto 1960");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                "Lantmäteriet",
                "",
                enabled,
                "se.lm.hist_orto_60"
        ));
    }

    private void initLmOrto1975(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Swe LM Ortho 1975");
        names.put("sv", "Sve LM Orto 1975");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                "Lantmäteriet",
                "",
                enabled,
                "se.lm.hist_orto_75"
        ));
    }

    private void initLmTopo(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Swe LM Topo Web");
        names.put("sv", "Sve LM Topo Web");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                "Lantmäteriet",
                "",
                enabled,
                "se.lm.topoweb"
        ));
    }

    private void initLmTopoDim(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Swe LM Topo Dim");
        names.put("sv", "Sve LM Topo Nedtonad");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                "Lantmäteriet",
                "",
                enabled,
                "se.lm.topoweb_dim"
        ));
    }

    private void initNasaUV(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "NEO UV Index");
        names.put("sv", "NEO UV-index");

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                "NASA",
                "",
                enabled,
                "gov.nasa.neo.uv"
        ));
    }
}
