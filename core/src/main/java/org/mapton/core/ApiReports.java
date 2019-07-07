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
package org.mapton.core;

import java.util.TreeMap;
import java.util.TreeSet;
import org.mapton.api.MApiReport;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MCooTrans;
import org.mapton.api.MEngine;
import org.mapton.api.MSearchEngine;
import org.mapton.api.MUpdater;
import org.mapton.api.MWhatsHereEngine;
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
        String category = "Core/";
        TreeMap<String, TreeSet<String>> items = new TreeMap<>();

        TreeSet<String> implementations;
        implementations = new TreeSet<>();
        for (MCooTrans implementation : Lookup.getDefault().lookupAll(MCooTrans.class)) {
            implementations.add(implementation.getName());
        }
        items.put(category + "MCooTrans", implementations);

        implementations = new TreeSet<>();
        for (MContextMenuItem implementation : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            if (implementation.getType() == MContextMenuItem.ContextType.COPY) {
                implementations.add(implementation.getName());
            }
        }
        items.put(category + "MContextMenu Copy", implementations);

        implementations = new TreeSet<>();
        for (MContextMenuItem implementation : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            if (implementation.getType() == MContextMenuItem.ContextType.EXTRAS) {
                implementations.add(implementation.getName());
            }
        }
        items.put(category + "MContextMenu Extras", implementations);

        implementations = new TreeSet<>();
        for (MContextMenuItem implementation : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            if (implementation.getType() == MContextMenuItem.ContextType.OPEN) {
                implementations.add(implementation.getName());
            }
        }
        items.put(category + "MContextMenu Open", implementations);

        implementations = new TreeSet<>();
        for (MEngine implementation : Lookup.getDefault().lookupAll(MEngine.class)) {
            implementations.add(implementation.getName());
        }
        items.put(category + "MEngine", implementations);

        implementations = new TreeSet<>();
        for (MSearchEngine implementation : Lookup.getDefault().lookupAll(MSearchEngine.class)) {
            implementations.add(implementation.getName());
        }
        items.put(category + "MSearchEngine", implementations);

        implementations = new TreeSet<>();
        for (MWhatsHereEngine implementation : Lookup.getDefault().lookupAll(MWhatsHereEngine.class)) {
            implementations.add(implementation.getName());
        }
        items.put(category + "MWhatsHereEngine", implementations);

        implementations = new TreeSet<>();
        for (MUpdater implementation : Lookup.getDefault().lookupAll(MUpdater.class)) {
            implementations.add(implementation.getName());
        }
        items.put(category + "MUpdater", implementations);

        return items;
    }

}
