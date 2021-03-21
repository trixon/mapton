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
package org.mapton.wms.lantmateriet;

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
        initFastighet(false);
        initHillshade(false);
        initOrto025(false);
        initOrto1960(true);
        initOrto1975(true);
        initTopo(false);
        initLmTopoDim(false);

        System.out.println(getStyleJson());
    }

    private void initFastighet(boolean enabled) {
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

    private void initHillshade(boolean enabled) {
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

    private void initOrto025(boolean enabled) {
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

    private void initOrto1960(boolean enabled) {
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

    private void initOrto1975(boolean enabled) {
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

    private void initTopo(boolean enabled) {
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

}
