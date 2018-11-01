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
package org.mapton.api;

import java.io.File;
import java.text.DateFormat;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.StatusDisplayer;
import org.openide.util.Lookup;
import org.mapton.core.ui.AppToolBar;

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
    private static final Color ICON_COLOR = Color.BLACK;
    private static final int ICON_SIZE_CONTEXT_MENU = 16;
    private static final int ICON_SIZE_TOOLBAR = 36;
    private static final int ICON_SIZE_TOOLBAR_INT = 24;
    private static AppToolBar sAppToolBar;

    static {
        CONFIG_DIR = new File(System.getProperty("netbeans.user"));
        CACHE_DIR = new File(CONFIG_DIR, "cache");
    }

    public static void clearStatusText() {
        setStatusText("");
    }

    /**
     * Run in the thread of the map engine type
     *
     * @param runnable
     */
    public static void execute(Runnable runnable) {
        if (getEngine().isSwing()) {
            SwingUtilities.invokeLater(runnable);
        } else {
            Platform.runLater(runnable);
        }
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

    public static MEngine getEngine() {
        MEngine defaultEngine = null;

        for (MEngine mapEngine : Lookup.getDefault().lookupAll(MEngine.class)) {
            if (StringUtils.equalsIgnoreCase(mapEngine.getClass().getName(), MOptions.getInstance().getEngine())) {
                return mapEngine;
            } else {
                defaultEngine = mapEngine;
            }
        }

        return defaultEngine;
    }

    public static Color getIconColor() {
        return ICON_COLOR;
    }

    public static int getIconSizeContextMenu() {
        return ICON_SIZE_CONTEXT_MENU;
    }

    public static int getIconSizeToolBar() {
        return ICON_SIZE_TOOLBAR;
    }

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
