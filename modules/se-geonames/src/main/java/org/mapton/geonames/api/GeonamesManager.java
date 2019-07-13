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
package org.mapton.geonames.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.mapton.geonames.GeonamesGenerator;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class GeonamesManager {

    private final GeonamesGenerator mGenerator = GeonamesGenerator.getInstance();
    private ArrayList<Geoname> mGeonames = new ArrayList<>();

    public static GeonamesManager getInstance() {
        return Holder.INSTANCE;
    }

    private GeonamesManager() {
        init();
    }

    public ArrayList<Geoname> getGeonames() {
        return mGeonames;
    }

    public void init() {
        new Thread(() -> {
            Gson gson = new GsonBuilder().create();

            if (mGenerator.getSearchEngineFile().isFile()) {
                try {
                    String json = FileUtils.readFileToString(mGenerator.getSearchEngineFile(), "utf-8");
                    mGeonames = gson.fromJson(json, new TypeToken<ArrayList<Geoname>>() {
                    }.getType());
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }).start();
    }

    private static class Holder {

        private static final GeonamesManager INSTANCE = new GeonamesManager();
    }
}
