/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.api;

import java.util.Locale;
import java.util.ResourceBundle;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum DictMT {
    COPY_LOCATION,
    DISPLAY_PLACEMARK,
    SET_HOME;
    private final ResourceBundle mResourceBundle = ResourceBundle.getBundle(SystemHelper.getPackageAsPath(DictMT.class) + "DictMT", Locale.getDefault());

    private static String getString(ResourceBundle bundle, String key) {
        if (bundle.containsKey(key)) {
            return bundle.getString(key);
        } else {
            return "Key not found: " + key;
        }
    }

    @Override
    public String toString() {
        return getString(mResourceBundle, name().toLowerCase());
    }
}