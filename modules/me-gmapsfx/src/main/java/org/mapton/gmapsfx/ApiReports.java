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
package org.mapton.gmapsfx;

import java.util.TreeMap;
import java.util.TreeSet;
import org.mapton.api.MApiReport;
import org.mapton.gmapsfx.api.MapStyle;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MApiReport.class)
public class ApiReports implements MApiReport {

    @Override
    public TreeMap<String, TreeSet<String>> getItems() {
        var category = "GMapsFX/";
        TreeMap<String, TreeSet<String>> items = new TreeMap<>();

        TreeSet<String> implementations;
        implementations = new TreeSet<>();
        for (MapStyle implementation : Lookup.getDefault().lookupAll(MapStyle.class)) {
            implementations.add(implementation.getClass().getCanonicalName());
        }
        items.put(category + "MapStyle", implementations);

        return items;
    }

}
