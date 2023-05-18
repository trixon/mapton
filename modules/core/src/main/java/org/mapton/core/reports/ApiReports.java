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
package org.mapton.core.reports;

import java.util.TreeMap;
import java.util.TreeSet;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MApiReport;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MCooTrans;
import org.mapton.api.MCoordinateFileOpener;
import org.mapton.api.MEngine;
import org.mapton.api.MPoiProvider;
import org.mapton.api.MSimpleObjectStorageBoolean;
import org.mapton.api.MSimpleObjectStorageString;
import org.mapton.api.MToolMapCommand;
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

        var copyImplementations = new TreeSet<String>();
        var extrasImplementations = new TreeSet<String>();
        var openImplementations = new TreeSet<String>();

        for (var implementation : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            switch (implementation.getType()) {
                case COPY ->
                    copyImplementations.add(implementation.getClass().getCanonicalName());
                case EXTRAS ->
                    extrasImplementations.add(implementation.getClass().getCanonicalName());
                case OPEN ->
                    openImplementations.add(implementation.getClass().getCanonicalName());
                default ->
                    throw new AssertionError();
            }
        }

        mItems.put(mCategory + "MContextMenu Copy", copyImplementations);
        mItems.put(mCategory + "MContextMenu Extras", extrasImplementations);
        mItems.put(mCategory + "MContextMenu Open", openImplementations);

        populate(null, MCooTrans.class);
        populate(null, MCoordinateFileOpener.class);
        populate(null, MEngine.class);
        populate(null, MWhatsHereEngine.class);
        populate(null, MUpdater.class);
        populate(null, MWmsSourceProvider.class);
        populate(null, MWmsStyleProvider.class);
        populate(null, NewsProvider.class);
        populate(null, MPoiProvider.class);
        populate(null, MEditor.class);
        populate(null, MReport.class);
        populate(null, MToolMapCommand.class);
        populate(null, MSimpleObjectStorageBoolean.class);
        populate("MSimpleObjectStorageString ", MSimpleObjectStorageString.ApiKey.class);
        populate("MSimpleObjectStorageString ", MSimpleObjectStorageString.Misc.class);
        populate("MSimpleObjectStorageString ", MSimpleObjectStorageString.Path.class);
        populate("MSimpleObjectStorageString ", MSimpleObjectStorageString.Url.class);

        return mItems;
    }

    private void populate(String prefix, Class clazz) {
        var implementations = new TreeSet<String>();

        for (var implementation : Lookup.getDefault().lookupAll(clazz)) {
            implementations.add(implementation.getClass().getCanonicalName());
        }

        mItems.put(mCategory + StringUtils.defaultString(prefix) + clazz.getSimpleName(), implementations);
    }
}
