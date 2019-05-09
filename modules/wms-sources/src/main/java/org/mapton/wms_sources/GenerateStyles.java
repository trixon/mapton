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
import java.util.Arrays;
import java.util.TreeMap;
import org.mapton.api.MWmsStyle;

/**
 *
 * @author Patrik Karlström
 */
public class GenerateStyles extends Generator {

    private final TreeMap<String, String> mCategoriesEarth = new TreeMap<>();
    private final TreeMap<String, String> mCategoriesSwe = new TreeMap<>();
    private final TreeMap<String, String> mCategoriesVirtualEarth = new TreeMap<>();
    private final ArrayList<MWmsStyle> mStyles = new ArrayList<>();

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new GenerateStyles();
    }

    public GenerateStyles() {
        mCategoriesEarth.put("", "Earth");
        mCategoriesEarth.put("sv", "Jorden");
        mCategoriesSwe.put("", "Sweden");
        mCategoriesSwe.put("sv", "Sverige");
        mCategoriesVirtualEarth.put("", "Virtual Earth");
        mCategoriesVirtualEarth.put("sv", "Virtual Earth");

        initEoxEarthByNight(true);
        initEoxHydro(true);
        initEoxOsm(true);
        initEoxSentinel2(true);
        initEoxSentinel2Streets(true);
        initEoxTerrain(true);
        initEoxTerrainLight(true);

        initLmFastighet(false);
        initLmHillshade(false);
        initLmOrto025(false);
        initLmOrto1960(true);
        initLmOrto1975(true);
        initLmTopo(false);
        initLmTopoDim(false);

        initNasaUV(true);

        initVirtualEarthA(true);
        initVirtualEarthH(true);
        initVirtualEarthR(true);

        String json = gson.toJson(mStyles);
        System.out.println(json);
    }

    private MWmsStyle createStyle(TreeMap<String, String> categories, TreeMap<String, String> names, TreeMap<String, String> descriptions, String supplier, boolean enabled, String id, String... layers) {
        MWmsStyle style = new MWmsStyle();
        style.setCategories(categories);
        style.setNames(names);
        style.setEnabled(enabled);
        style.setSupplier(supplier);
        style.setDescriptions(descriptions);
        style.setId(id);
        style.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return style;
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

    private void initLmFastighet(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Property");
        names.put("sv", "Lantmäteriet Fastighet");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Cadastral.");
        descriptions.put("sv", "Fastighetsgränser.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.fastighet",
                "se.lm.fastighet_text", "se.lm.fastighet_granser", "se.lm.topoweb_dim", "at.eox.terrain-light"
        ));
    }

    private void initLmHillshade(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Hillshade");
        names.put("sv", "Lantmäteriet Terrängskuggning");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Terrain model as hillshade.");
        descriptions.put("sv", "Höjdmodell som terrängskuggning.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.terrangskuggning",
                "se.lm.terrangskuggning", "at.eox.terrain-light"
        ));
    }

    private void initLmOrto025(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Ortho photo");
        names.put("sv", "Lantmäteriet Ortofoto");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Resolution: 0.25 m.");
        descriptions.put("sv", "Upplösning: 0,25 m.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.orto_025",
                "se.lm.orto_025", "at.eox.s2cloudless"
        ));
    }

    private void initLmOrto1960(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Ortho photo 1960");
        names.put("sv", "Lantmäteriet Ortofoto 1960");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Reference year 1960.");
        descriptions.put("sv", "Referensår 1960.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.hist_orto_60",
                "se.lm.hist_orto_60",
                "at.eox.s2cloudless"
        ));
    }

    private void initLmOrto1975(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Ortho photo 1975");
        names.put("sv", "Lantmäteriet Ortofoto 1975");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Reference year 1975.");
        descriptions.put("sv", "Referensår 1975.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.hist_orto_75",
                "se.lm.hist_orto_75",
                "at.eox.s2cloudless"
        ));
    }

    private void initLmTopo(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Topo");
        names.put("sv", "Lantmäteriet Topo");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Topographic web map.");
        descriptions.put("sv", "Topografisk webbkarta.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.topoweb",
                "se.lm.topoweb", "at.eox.terrain"
        ));
    }

    private void initLmTopoDim(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Lantmäteriet Topo Dim");
        names.put("sv", "Lantmäteriet Topo Nedtonad");

        TreeMap<String, String> descriptions = new TreeMap<>();
        descriptions.put("", "Dimmed topographic web map.");
        descriptions.put("sv", "Nedtonad topografisk webbkarta.");

        mStyles.add(createStyle(
                mCategoriesSwe,
                names,
                descriptions,
                "Lantmäteriet",
                enabled,
                "se.lm.topoweb_dim",
                "se.lm.topoweb_dim", "at.eox.terrain-light"
        ));
    }

    private void initNasaUV(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "NEO UV Index");
        names.put("sv", "NEO UV-index");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "NASA",
                enabled,
                "gov.nasa.neo.uv",
                "gov.nasa.neo.uv"
        ));
    }

    private void initVirtualEarthA(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Satellite");
        names.put("sv", "Satellit");
        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesVirtualEarth,
                names,
                descriptions,
                "Emxsys",
                enabled,
                "net.emxsys.ve-a",
                "net.emxsys.ve-a"
        ));
    }

    private void initVirtualEarthH(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Hybrid");
        names.put("sv", "Hybrid");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesVirtualEarth,
                names,
                descriptions,
                "Emxsys",
                enabled,
                "net.emxsys.ve-h",
                "net.emxsys.ve-h"
        ));
    }

    private void initVirtualEarthR(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Roadmap");
        names.put("sv", "Vägkarta");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesVirtualEarth,
                names,
                descriptions,
                "Emxsys",
                enabled,
                "net.emxsys.ve-r",
                "net.emxsys.ve-r"
        ));
    }
}
