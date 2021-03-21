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
package org.mapton.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TreeMap;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import static org.mapton.api.MKey.*;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class MDataSourceInitializer implements Runnable {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .setVersion(1.0)
            .setPrettyPrinting()
            .setDateFormat(DATE_FORMAT)
            .create();
    private static final String LOG_TAG = "DataSources";
    private final Preferences mPreferences = NbPreferences.forModule(MDataSourceInitializer.class);

    public MDataSourceInitializer() {
        mPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case DATA_SOURCES_FILES:
                    applyFile();
                    break;

                case DATA_SOURCES_WMS_SOURCES:
                    applyWmsSource();
                    break;

                case DATA_SOURCES_WMS_STYLES:
                    applyWmsStyle();
                    break;
            }
        });

        Lookup.getDefault().lookupResult(MWmsSourceProvider.class).addLookupListener((LookupEvent ev) -> {
            applyWmsSource();
        });

        Lookup.getDefault().lookupResult(MWmsStyleProvider.class).addLookupListener((LookupEvent ev) -> {
            applyWmsStyle();
        });
    }

    @Override
    public void run() {
        applyWmsSource();
        applyWmsStyle();
        applyFile();
    }

    private void applyFile() {
        //TODO Read files and create point, lines and polygons for publishing
        //TODO Support simple attributes such as colors, line width and markers.
        //TODO Don't put any burden on map engine renderers, unless needed
        //TODO Get importers from lookup
        ArrayList<File> files = new ArrayList<>();
        for (String line : mPreferences.get(DATA_SOURCES_FILES, "").split("\n")) {
            File file = new File(line);
            if (file.exists()) {
                files.add(file);
            }
        }

        Mapton.getGlobalState().put(MKey.DATA_SOURCES_FILES, files);
    }

    private void applyWmsSource() {
        ArrayList<MWmsSource> allSources = new ArrayList<>();

        for (String json : getJsons(mPreferences.get(DATA_SOURCES_WMS_SOURCES, MDataSource.getDefaultSources()))) {
            try {
                deserializeSource(json).stream()
                        .filter((wmsSource) -> (wmsSource.isEnabled()))
                        .forEachOrdered((wmsSource) -> {
                            allSources.add(wmsSource);
                        });
            } catch (NullPointerException ex) {
                //nvm
            } catch (JsonSyntaxException ex) {
                Mapton.getLog().i(LOG_TAG, ex.toString());
            }
        }

        for (MWmsSourceProvider wmsSourceProvider : Lookup.getDefault().lookupAll(MWmsSourceProvider.class)) {
            try {
                deserializeSource(wmsSourceProvider.getJson()).stream()
                        .filter((wmsSource) -> (wmsSource.isEnabled()))
                        .forEachOrdered((wmsSource) -> {
                            allSources.add(wmsSource);
                        });
            } catch (NullPointerException ex) {
                //nvm
            } catch (JsonSyntaxException ex) {
                Mapton.getLog().i(LOG_TAG, ex.toString());
            }
        }

        Mapton.getGlobalState().put(DATA_SOURCES_WMS_SOURCES, allSources);

        TreeMap<String, MAttribution> attributions = new TreeMap<>();
        for (MWmsSource wmsSource : allSources) {
            attributions.putAll(wmsSource.getAttributions());
        }

        Mapton.getGlobalState().put(DATA_SOURCES_WMS_ATTRIBUTIONS, attributions);
    }

    private void applyWmsStyle() {
        ArrayList<MWmsStyle> allStyles = new ArrayList<>();

        for (String json : getJsons(mPreferences.get(DATA_SOURCES_WMS_STYLES, MDataSource.getDefaultStyles()))) {
            try {
                deserializeStyle(json).stream()
                        .filter((wmsStyle) -> (wmsStyle.isEnabled()))
                        .forEachOrdered((wmsStyle) -> {
                            allStyles.add(wmsStyle);
                        });
            } catch (NullPointerException | JsonSyntaxException ex) {
                Mapton.getLog().i(LOG_TAG, ex.toString());
            }
        }

        for (MWmsStyleProvider wmsStyleProvider : Lookup.getDefault().lookupAll(MWmsStyleProvider.class)) {
            try {
                deserializeStyle(wmsStyleProvider.getJson()).stream()
                        .filter((wmsStyle) -> (wmsStyle.isEnabled()))
                        .forEachOrdered((wmsStyle) -> {
                            allStyles.add(wmsStyle);
                        });
            } catch (JsonSyntaxException ex) {
                Mapton.getLog().i(LOG_TAG, ex.toString());
            }
        }

        Mapton.getGlobalState().put(DATA_SOURCES_WMS_STYLES, allStyles);
    }

    private ArrayList<MWmsSource> deserializeSource(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<MWmsSource>>() {
        }.getType());
    }

    private ArrayList<MWmsStyle> deserializeStyle(String json) {
        return gson.fromJson(json, new TypeToken<ArrayList<MWmsStyle>>() {
        }.getType());
    }

    private ArrayList<String> getJsons(String lines) {
        ArrayList<String> jsons = new ArrayList<>();

        for (String line : lines.split("\n")) {
            if (line.startsWith("#") || StringUtils.isBlank(line)) {
                continue;
            }

            String json = "";
            try {
                if (line.contains("//")) {
                    json = IOUtils.toString(new URI(line), "utf-8");
                } else {
                    File file = new File(line);
                    json = FileUtils.readFileToString(file, "utf-8");
                }
            } catch (URISyntaxException | IOException ex) {
                Mapton.getLog().i(LOG_TAG, ex.toString());
            }

            jsons.add(json);
        }

        return jsons;
    }
}
