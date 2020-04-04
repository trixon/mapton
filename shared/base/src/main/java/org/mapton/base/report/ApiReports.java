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
package org.mapton.base.report;

import java.util.TreeMap;
import java.util.TreeSet;
import org.mapton.api.MApiReport;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MCooTrans;
import org.mapton.api.MEngine;
import org.mapton.api.MPoiProvider;
import org.mapton.api.MSearchEngine;
import org.mapton.api.MUpdater;
import org.mapton.api.MWhatsHereEngine;
import org.mapton.api.MWmsSourceProvider;
import org.mapton.api.MWmsStyleProvider;
import org.mapton.api.report.MEditor;
import org.mapton.api.report.MReport;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.core.news.NewsProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MApiReport.class)
public class ApiReports implements MApiReport {

    private final String mCategory = "Core/";
    private final TreeMap<String, TreeSet<String>> mItems = new TreeMap<>();

    @Override
    public TreeMap<String, TreeSet<String>> getItems() {
        mItems.clear();

        TreeSet<String> copyImplementations = new TreeSet<>();
        TreeSet<String> extrasImplementations = new TreeSet<>();
        TreeSet<String> openImplementations = new TreeSet<>();

        for (MContextMenuItem implementation : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            switch (implementation.getType()) {
                case COPY:
                    copyImplementations.add(implementation.getName());
                    break;

                case EXTRAS:
                    extrasImplementations.add(implementation.getName());
                    break;

                case OPEN:
                    openImplementations.add(implementation.getName());
                    break;

                default:
                    throw new AssertionError();
            }
        }

        mItems.put(mCategory + "MContextMenu Copy", copyImplementations);
        mItems.put(mCategory + "MContextMenu Extras", extrasImplementations);
        mItems.put(mCategory + "MContextMenu Open", openImplementations);

        populate(MCooTrans.class);
        populate(MEngine.class);
        populate(MSearchEngine.class);
        populate(MWhatsHereEngine.class);
        populate(MUpdater.class);
        populate(MWmsSourceProvider.class);
        populate(MWmsStyleProvider.class);
        populate(NewsProvider.class);
        populate(MPoiProvider.class);
        populate(MEditor.class);
        populate(MReport.class);

        return mItems;
    }

    private void populate(Class clazz) {
        TreeSet<String> implementations = new TreeSet<>();
        for (Object implementation : Lookup.getDefault().lookupAll(clazz)) {
            implementations.add(implementation.getClass().getCanonicalName());
        }

        mItems.put(mCategory + clazz.getSimpleName(), implementations);
    }
}
