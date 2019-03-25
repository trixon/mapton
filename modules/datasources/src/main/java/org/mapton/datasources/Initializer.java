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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.Preferences;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MKey;
import org.mapton.api.MWmsSource;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import static org.mapton.datasources.DataSourcesPane.*;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.NbLog;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    static final Gson gson = new GsonBuilder()
            .setVersion(1.0)
            .setPrettyPrinting()
            .setDateFormat(DATE_FORMAT)
            .create();
    private static final String LOG_TAG = "DataSources";
    private final Preferences mPreferences = NbPreferences.forModule(DataSourcesPane.class);

    public Initializer() {
        mPreferences.addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            switch (evt.getKey()) {
                case KEY_WMS_SOURCE:
                    applyWmsSource();
                    break;

                case KEY_WMS_STYLE:
                    applyWmsStyle();
                    break;
            }
        });
    }

    @Override
    public void run() {
        applyWmsSource();
        applyWmsStyle();
    }

    private void applyWmsSource() {
        ArrayList<MWmsSource> allSources = new ArrayList<>();

        for (String json : getJsons(mPreferences.get(KEY_WMS_SOURCE, ""))) {
            try {
                ArrayList<MWmsSource> sources = gson.fromJson(json, new TypeToken<ArrayList<MWmsSource>>() {
                }.getType());

                allSources.addAll(sources);
            } catch (JsonSyntaxException ex) {
                NbLog.i(LOG_TAG, ex.toString());
            }
        }

        Mapton.getGlobalState().put(MKey.DATA_SOURCES_WMS_SOURCES, allSources);
    }

    private void applyWmsStyle() {
        ArrayList<MWmsStyle> allStyles = new ArrayList<>();

        for (String json : getJsons(mPreferences.get(KEY_WMS_STYLE, ""))) {
            try {
                ArrayList<MWmsStyle> styles = gson.fromJson(json, new TypeToken<ArrayList<MWmsStyle>>() {
                }.getType());

                allStyles.addAll(styles);
            } catch (JsonSyntaxException ex) {
                NbLog.i(LOG_TAG, ex.toString());
            }
        }

        Mapton.getGlobalState().put(MKey.DATA_SOURCES_WMS_STYLES, allStyles);
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
                NbLog.i(LOG_TAG, ex.toString());
            }

            jsons.add(json);
        }

        return jsons;
    }
}
