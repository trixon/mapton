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

import java.io.File;
import java.text.DateFormat;
import javafx.scene.paint.Color;
import org.apache.commons.lang.StringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import se.trixon.mapton.core.toolbar.AppToolBar;

/**
 *
 * @author Patrik Karlström
 */
public class Mapton {

    public static final String LOG_TAG = "Mapton";
    public static final double MYLAT = 57.66;
    public static final double MYLON = 12.0;

    private static final File CACHE_DIR;
    private static final File CONFIG_DIR;
    private static final int ICON_SIZE_CONTEXT_MENU = 16;
    private static final int ICON_SIZE_TOOLBAR = 36;
    private static final int ICON_SIZE_TOOLBAR_INT = 24;
    private static AppToolBar sAppToolBar;
    private static final Color sIconColor = Color.BLACK;

    static {
        CONFIG_DIR = new File(System.getProperty("netbeans.user"));
        CACHE_DIR = new File(CONFIG_DIR, "cache");
    }

    public static void clearStatusText() {
        setStatusText("");
    }

    public static AppToolBar getAppToolBar() {
        return sAppToolBar;
    }

    public static File getCacheDir() {
        return CACHE_DIR;
    }

    public static File getConfigDir() {
        return CONFIG_DIR;
    }

    public static DateFormat getDefaultDateFormat() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    }

    public static MapEngine getEngine() {
        MapEngine defaultEngine = null;

        for (MapEngine mapEngine : Lookup.getDefault().lookupAll(MapEngine.class)) {
            if (StringUtils.equalsIgnoreCase(mapEngine.getName(), MaptonOptions.getInstance().getMapEngine())) {
                return mapEngine;
            } else {
                defaultEngine = mapEngine;
            }
        }

        return defaultEngine;
    }

    public static Color getIconColor() {
        return sIconColor;
    }

    public static int getIconSizeContextMenu() {
        return ICON_SIZE_CONTEXT_MENU;
    }

    public static int getIconSizeToolBar() {
        return ICON_SIZE_TOOLBAR;
    }

    @Deprecated
    public static int getIconSizeToolBarInt() {
        return ICON_SIZE_TOOLBAR_INT;
    }

    public static Mapton getInstance() {
        return Holder.INSTANCE;
    }

    public static void setStatusText(String text, int importance) {
        StatusDisplayer.getDefault().setStatusText(text, importance);
    }

    public static void setStatusText(String text) {
        setStatusText(text, StatusDisplayer.IMPORTANCE_ANNOTATION);
    }

    public static void setToolBar(AppToolBar appToolBar) {
        sAppToolBar = appToolBar;
    }

    private Mapton() {
    }

    private static class Holder {

        private static final Mapton INSTANCE = new Mapton();
    }
}
