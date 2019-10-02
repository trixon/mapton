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
package org.mapton.api;

import java.io.File;
import javafx.scene.paint.Color;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import se.trixon.almond.util.GlobalState;

/**
 *
 * @author Patrik Karlström
 */
public class Mapton {

    public static final int ICON_SIZE_MODULE = 32;
    public static final int ICON_SIZE_MODULE_TOOLBAR = 40;
    public static final int ICON_SIZE_PROFILE = 32;
    public static final int ICON_SIZE_TOOLBAR = 36;
    public static final int ICON_SIZE_DRAWER = ICON_SIZE_TOOLBAR / 2;
    public static final String LOG_TAG = "Mapton";
    public static final int MODULE_ICON_SIZE = 32;
    public static final double MYLAT = 57.66;
    public static final double MYLON = 12.0;

    private static final File CACHE_DIR;
    private static final File CONFIG_DIR;
    private static final Color ICON_COLOR = Color.BLACK;
    private static final GlobalState sGlobalState = new GlobalState();

    static {
        CONFIG_DIR = new File(System.getProperty("netbeans.user"), "mapton-modules");
        CACHE_DIR = new File(FileUtils.getUserDirectory(), ".cache/mapton");
        System.setProperty("mapton.cache", CACHE_DIR.getAbsolutePath());//Used by WorldWind
    }

    public static MEngine getEngine() {
        MEngine defaultEngine = null;

        for (MEngine mapEngine : Lookup.getDefault().lookupAll(MEngine.class)) {
            if (StringUtils.equalsIgnoreCase(mapEngine.getName(), MOptions.getInstance().getEngine())) {
                return mapEngine;
            } else {
                defaultEngine = mapEngine;
            }
        }

        return defaultEngine;
    }

    public static GlobalState getGlobalState() {
        return sGlobalState;
    }

    public static void logLoading(String category, String item) {
        //NbLog.i("Loading", String.format("%s: %s ", category, item));
        System.out.println(String.format("Loading %s: %s ", category, item));
    }

    public static void logRemoving(String category, String item) {
        //NbLog.i("Removing", String.format("%s: %s ", category, item));
        System.out.println(String.format("Removing %s: %s ", category, item));
    }

    private Mapton() {
    }

    private static class Holder {

        private static final Mapton INSTANCE = new Mapton();
    }
}
