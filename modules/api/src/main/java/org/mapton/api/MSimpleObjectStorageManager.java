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
package org.mapton.api;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class MSimpleObjectStorageManager {

    private final ConcurrentHashMap<Class, Preferences> mClassToPreferenceNode = new ConcurrentHashMap<>();
    private final Preferences mPreferences = NbPreferences.forModule(MSimpleObjectStorageManager.class).node("simple_object_storage");

    public static MSimpleObjectStorageManager getInstance() {
        return Holder.INSTANCE;
    }

    private MSimpleObjectStorageManager() {
    }

    public void addListener(PreferenceChangeListener pcl, Class<? extends MSimpleObjectStorage> clazz) {
        String category;
        if (MSimpleObjectStorageBoolean.class.isAssignableFrom(clazz)) {
            category = "boolean";
        } else {
            category = "string";
        }

        getNode(category, clazz).addPreferenceChangeListener(pcl);
    }

    @SafeVarargs
    public final void addListeners(PreferenceChangeListener pcl, Class<? extends MSimpleObjectStorage>... clazzes) {
        for (var clazz : clazzes) {
            addListener(pcl, clazz);
        }
    }

    public Boolean getBoolean(Class c, Boolean defaultValue) {
        return getNode("boolean", c).getBoolean(c.getName(), defaultValue);
    }

    public File getFile(Class c, File defaultValue) {
        return new File(getString(c, defaultValue.getAbsolutePath()));
    }

    public String getString(Class c, String defaultValue) {
        return getNode("string", c).get(c.getName(), defaultValue);
    }

    public void putBoolean(Class<? extends MSimpleObjectStorageBoolean> c, Boolean value) {
        getNode("boolean", c).putBoolean(c.getName(), value);
    }

    public void putString(Class<? extends MSimpleObjectStorageString> c, String value) {
        getNode("string", c).put(c.getName(), StringUtils.defaultString(value));
    }

    private Preferences getNode(String category, Class c) {
        var parentNode = mPreferences.node(category);

        if (MSimpleObjectStorageString.ApiKey.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageString.ApiKey.class, k -> parentNode.node("api_key"));
        } else if (MSimpleObjectStorageString.Path.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageString.Path.class, k -> parentNode.node("path"));
        } else if (MSimpleObjectStorageString.Url.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageString.Url.class, k -> parentNode.node("url"));
        } else if (MSimpleObjectStorageString.Misc.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageString.Misc.class, k -> parentNode.node("misc"));
        } else if (MSimpleObjectStorageBoolean.AutoOpen.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageBoolean.AutoOpen.class, k -> parentNode.node("autoOpen"));
        } else if (MSimpleObjectStorageBoolean.UpdaterAutoUpdate.class.isAssignableFrom(c)) {
            return mClassToPreferenceNode.computeIfAbsent(MSimpleObjectStorageBoolean.UpdaterAutoUpdate.class, k -> parentNode.node("updaterAutoUpdate"));
        } else {
            return parentNode.node("unknown");
        }
    }

    private static class Holder {

        private static final MSimpleObjectStorageManager INSTANCE = new MSimpleObjectStorageManager();
    }
}
