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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.prefs.Preferences;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.Strings;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class MDataSource {

    private static final Preferences sPreferences = NbPreferences.forModule(MDataSource.class);

    public static void addIfMissing(String key, File file) {
        var defaultContent = "";
        switch (key) {
            case MKey.DATA_SOURCES_WMS_SOURCES:
                defaultContent = getDefaultSources();
                break;
            case MKey.DATA_SOURCES_WMS_STYLES:
                defaultContent = getDefaultStyles();
                break;
        }

        var content = sPreferences.get(key, defaultContent);
        var absolutePath = file.getAbsolutePath();

        if (!Strings.CI.contains(content, absolutePath)) {
            var addRow = !content.isEmpty() && !Strings.CS.endsWith(content, "\n");
            var prefix = addRow ? "\n" : "";

            sPreferences.put(key, content + prefix + absolutePath + "\n");
        }
    }

    public static String getDefaultSources() {
        try {
            return IOUtils.toString(new URI("https://mapton.org/files/data_sources_wms_sources_defaults"), "utf-8") + "\n";
        } catch (IOException | URISyntaxException ex) {
            return "# Defaults not found\n";
        }
    }

    public static String getDefaultStyles() {
        try {
            return IOUtils.toString(new URI("https://mapton.org/files/data_sources_wms_styles_defaults"), "utf-8") + "\n";
        } catch (IOException | URISyntaxException ex) {
            return "# Defaults not found\n";
        }
    }

    public static Preferences getPreferences() {
        return sPreferences;
    }

    public MDataSource() {
    }
}
