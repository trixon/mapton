/*
 * Copyright 2020 Patrik Karlström.
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

import java.util.HashMap;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class MStringStorageManager {

    private final HashMap<Class, Preferences> mClassToPreferenceNode = new HashMap<>();
    private final Preferences mPreferences = NbPreferences.forModule(MStringStorageManager.class);

    public static MStringStorageManager getInstance() {
        return Holder.INSTANCE;
    }

    private MStringStorageManager() {
    }

//    public String getValue(Class c) {
//        return getNode(c).get(c.getName(), null);
//    }
    public String getValue(Class c, String defaultValue) {
        return getNode(c).get(c.getName(), defaultValue);
    }

    public <T> void putValue(Class<? extends MStringStorage> c, String value) {
        getNode(c).put(c.getName(), StringUtils.defaultString(value));
    }

    private Preferences getNode(Class c) {
        var parentNode = mPreferences.node("string_storage");
        if (MStringStorage.ApiKey.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MStringStorage.ApiKey.class, k -> parentNode.node("api_key"));
        } else if (MStringStorage.Path.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MStringStorage.Path.class, k -> parentNode.node("path"));
        } else if (MStringStorage.Url.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MStringStorage.Url.class, k -> parentNode.node("url"));
        } else if (MStringStorage.Misc.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MStringStorage.Misc.class, k -> parentNode.node("misc"));
        } else {
            return parentNode.node("unknown");
        }
    }

    private static class Holder {

        private static final MStringStorageManager INSTANCE = new MStringStorageManager();
    }
}
