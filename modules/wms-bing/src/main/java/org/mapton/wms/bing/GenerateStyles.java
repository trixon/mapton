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
package org.mapton.wms.bing;

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
        initBingA(true);
        initBingH(true);
        initBingR(true);

        System.out.println(getStyleJson());
    }

    private void initBingA(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Bing Satellite");
        names.put("sv", "Bing Satellit");
        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "Bing (NASA)",
                enabled,
                "com.microsoft.ve-a",
                "com.microsoft.ve-a"
        ));
    }

    private void initBingH(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Bing Hybrid");
        names.put("sv", "Bing Hybrid");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "Bing (NASA)",
                enabled,
                "com.microsoft.ve-h",
                "com.microsoft.ve-h"
        ));
    }

    private void initBingR(boolean enabled) {
        TreeMap<String, String> names = new TreeMap<>();
        names.put("", "Bing Roadmap");
        names.put("sv", "Bing Vägkarta");

        TreeMap<String, String> descriptions = new TreeMap<>();

        mStyles.add(createStyle(
                mCategoriesEarth,
                names,
                descriptions,
                "Bing (NASA)",
                enabled,
                "com.microsoft.ve-r",
                "com.microsoft.ve-r"
        ));
    }
}
