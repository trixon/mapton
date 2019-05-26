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
package org.mapton.mapollage.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Patrik Karlström
 */
public class Mapo {

    public static final String KEY_MAPO = "mapo";
    public static final String KEY_SOURCE_MANAGER = "mapollage.source_manager";

    private static final Gson sGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    @SerializedName("date_pattern")
    private String mDatePattern = "yyyy-MM-dd HH.mm";
    @SerializedName("name_by")
    private NameBy mNameBy = NameBy.NONE;
    @SerializedName("symbol_as")
    private SymbolAs mSymbolAs = SymbolAs.PHOTO;

    public static Gson getGson() {
        return sGson;
    }

    public Mapo() {
    }

    public enum NameBy {
        NONE,
        FILE,
        DATE;
    }

    public enum SymbolAs {
        PHOTO,
        PIN;
    }

}
