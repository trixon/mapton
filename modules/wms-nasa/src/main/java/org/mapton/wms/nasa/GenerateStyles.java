/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.wms.nasa;

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
        initNasaUV(true);

        System.out.println(getStyleJson());
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

}
