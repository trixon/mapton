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
import org.mapton.api.MWmsStyle;
import se.trixon.almond.util.SystemHelper;

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

        styles.add(createStyle(
                "Swe LM Orto 1960",
                "Lantmäteriet",
                "",
                "Historiska ortofoton, 1960 : Historiska ortofoton"
        ));

        styles.add(createStyle(
                "Swe LM Orto 1975",
                "Lantmäteriet",
                "",
                "Historiska ortofoton, 1975 : Historiska ortofoton"
        ));

        String json = Initializer.gson.toJson(styles);
        System.out.println(json);
        SystemHelper.copyToClipboard(json);
    }

    private MWmsStyle createStyle(String name, String supplier, String description, String... layers) {
        MWmsStyle style = new MWmsStyle();
        style.setName(name);
        style.setSupplier(supplier);
        style.setDescription(description);
        style.setLayers(new ArrayList<>(Arrays.asList(layers)));

        return style;
    }
}
