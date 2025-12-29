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
package org.mapton.addon.photos.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 *
 * @author Patrik Karlström
 */
public class Mapo {

    public static final String KEY_SOURCE_UPDATED = "photos.source_updated";
    public static final String KEY_TEMPORAL_PREFIX = "org.mapton.addon.mapollage.";

    private static final Gson sGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static Gson getGson() {
        return sGson;
    }

    public static Mapo getInstance() {
        return Holder.INSTANCE;
    }

    private Mapo() {
    }

    private static class Holder {

        private static final Mapo INSTANCE = new Mapo();
    }
}
