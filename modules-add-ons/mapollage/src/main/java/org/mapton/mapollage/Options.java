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
package org.mapton.mapollage;

import java.util.Locale;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    public static final Locale DEFAULT_LOCALE = Locale.getDefault();
    public static final int DEFAULT_THUMBNAIL_BORDER_SIZE = 3;
    public static final int DEFAULT_THUMBNAIL_SIZE = 1000;
    public static final String KEY_GLOBAL_CLAMP_TO_GROUND = "global_clamp_to_ground";
    public static final String KEY_LOCALE = "locale";
    public static final String KEY_SOURCES = "sources";
    public static final String KEY_THUMBNAIL_BORDER_SIZE = "thumbnail_border_size";
    public static final String KEY_THUMBNAIL_SIZE = "thumbnail_size";

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        setPreferences(NbPreferences.forModule(Options.class));
    }

    public Locale getLocale() {
        return Locale.forLanguageTag(mPreferences.get(KEY_LOCALE, DEFAULT_LOCALE.toLanguageTag()));
    }

    public void setLocale(Locale locale) {
        mPreferences.put(KEY_LOCALE, locale.toLanguageTag());
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
