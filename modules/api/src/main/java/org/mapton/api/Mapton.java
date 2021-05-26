/*
 * Copyright 2021 Patrik Karlström.
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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.File;
import java.text.DateFormat;
import java.util.HashMap;
import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.Glyph;
import org.openide.modules.InstalledFileLocator;
import org.openide.modules.Places;
import org.openide.util.Lookup;
import se.trixon.almond.util.ExecutionFlow;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.Log;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Mapton {

    private static final Gson sGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .setPrettyPrinting()
            .create();

    public static final int ICON_SIZE_MODULE = 32;
    public static final int ICON_SIZE_MODULE_TOOLBAR = 40;
    public static final int ICON_SIZE_PROFILE = 32;
    public static final int ICON_SIZE_TOOLBAR = 32;
    public static final int ICON_SIZE_DRAWER = ICON_SIZE_TOOLBAR / 2;
    public static final String LOG_TAG = "Mapton";
    public static final int MODULE_ICON_SIZE = 32;
    public static final double MYLAT = 57.66;
    public static final double MYLON = 12.0;

    private static File CACHE_DIR;
    private static File CONFIG_DIR;
    private static final ExecutionFlow EXECUTION_FLOW = new ExecutionFlow();
    private static final Color ICON_COLOR = Color.BLACK;
    private static final HashMap<WebEngine, String> WEB_ENGINE_TO_STYLE = new HashMap<>();
    private static final GlobalState sGlobalState = new GlobalState();
    private static final Log sLog = new Log();
    private DoubleProperty mZoomProperty = new SimpleDoubleProperty();

    public static void applyHtmlCss(WebView webView, String filename) {
        WEB_ENGINE_TO_STYLE.put(webView.getEngine(), filename);
        applyHtmlCss(webView.getEngine(), filename);
        //webView.setFontScale(SwingHelper.getUIScale());
        webView.setZoom(SwingHelper.getUIScale());
    }

    public static void applyHtmlCss(WebEngine webEngine, String filename) {
        var path = String.format("resources/css/%s", filename);
        var codeNameBase = Mapton.class.getPackage().getName();
        var file = InstalledFileLocator.getDefault().locate(path, codeNameBase, false);

        if (isNightMode()) {
            path = StringUtils.replace(path, ".css", "_dark.css");
            var darkFile = InstalledFileLocator.getDefault().locate(path, codeNameBase, false);
            file = darkFile.isFile() ? darkFile : file;
        }

        webEngine.setUserStyleSheetLocation(file.toURI().toString());
    }

    public static void clearBackgroundImage() {
        getGlobalState().put(MKey.BACKGROUND_IMAGE, null);
    }

    public static Glyph createGlyph(FontAwesome.Glyph glyphFont, double size, Color color) {
        return new Glyph("FontAwesome", glyphFont).size(size).color(color);
    }

    public static Glyph createGlyphToolbarForm(FontAwesome.Glyph glyphFont) {
        return createGlyph(glyphFont, getIconSizeToolBarInt(), options().getIconColorForBackground());
    }

    public static Label createTitle(String title) {
        return createTitle(title, Mapton.getDefaultThemeBackground());
    }

    public static Label createTitle(String title, Background background) {
        var label = new Label(title);

        label.setBackground(background);
        var color = (Color) background.getFills().get(0).getFill();
        label.setStyle(String.format("-fx-background-color: %s;", FxHelper.colorToString(color)));
        label.setAlignment(Pos.BASELINE_CENTER);
        label.setFont(new Font(FxHelper.getScaledFontSize() * 1.2));
        label.setTextFill(Color.WHITE);

        return label;
    }

    public static Label createTitleDev(String title) {
        return createTitle(title + "-dev", FxHelper.createBackground(Color.RED));
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

    public static File getCacheDir() {
        if (CACHE_DIR == null) {
            CACHE_DIR = Places.getCacheDirectory();
        }

        return CACHE_DIR;
    }

    public static File getConfigDir() {
        if (CONFIG_DIR == null) {
            CONFIG_DIR = new File(System.getProperty("netbeans.user"), "mapton-modules");
        }

        return CONFIG_DIR;
    }

    public static DateFormat getDefaultDateFormat() {
        return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM);
    }

    public static Background getDefaultThemeBackground() {
        return FxHelper.createBackground(getDefaultThemeColor());
    }

    public static Color getDefaultThemeColor() {
        return Color.web("#102039");
    }

    public static Color getDefaultThemeForegroundColor() {
        return Color.WHITE;
    }

    public static MEngine getEngine() {
        MEngine defaultEngine = null;

        for (var mapEngine : Lookup.getDefault().lookupAll(MEngine.class)) {
            defaultEngine = mapEngine;
            try {
                if (StringUtils.equalsIgnoreCase(mapEngine.getName(), options().getEngine())) {
                    return mapEngine;
                }
            } catch (Exception e) {
            }
        }

        return defaultEngine;
    }

    public static ExecutionFlow getExecutionFlow() {
        return EXECUTION_FLOW;
    }

    public static GlobalState getGlobalState() {
        return sGlobalState;
    }

    public static Gson getGson() {
        return sGson;
    }

    public static Color getIconColor() {
        return ICON_COLOR;
    }

    public static int getIconSizeContextMenu() {
        return (int) (getIconSizeToolBar() / 2.0);
    }

    public static int getIconSizeToolBar() {
        return SwingHelper.getUIScaled(ICON_SIZE_TOOLBAR);
    }

    public static int getIconSizeToolBarInt() {
        return (int) (getIconSizeToolBar() / 1.3);
    }

    public static Mapton getInstance() {
        return Holder.INSTANCE;
    }

    public static Log getLog() {
        return sLog;
    }

    public static Background getThemeBackground() {
        return FxHelper.createBackground(getThemeColor());
    }

    public static Color getThemeColor() {
        return Mapton.getGlobalState().getOrDefault(MKey.APP_THEME_BACKGROUND, getDefaultThemeColor());
    }

    public static Color getThemeForegroundColor() {
        return Mapton.getGlobalState().getOrDefault(MKey.APP_THEME_FOREGROUND, getDefaultThemeForegroundColor());
    }

    public static boolean isNightMode() {
        return options().isNightMode();
    }

    public static void log(String line) {
        sLog.timedOut(line);
    }

    public static void log(String category, String item) {
        sLog.timedOut(String.format("%s: %s ", category, item));
    }

    public static void logDownloading(String category, String item) {
        sLog.timedOut(String.format("Downloading %s: %s ", category, item));
    }

    public static void logLoading(String category, String item) {
        sLog.timedOut(String.format("Loading %s: %s ", category, item));
    }

    public static void logRemoving(String category, String item) {
        sLog.timedOut(String.format("Removing %s: %s ", category, item));
    }

    public static void notification(String type, String title, String text) {
        getGlobalState().send(type, Notifications.create().title(title).text(text));
    }

    public static void notification(String type, String title, String text, Action... actions) {
        var notifications = Notifications.create()
                .title(title)
                .text(text)
                .hideAfter(Duration.INDEFINITE)
                .action(actions);

        getGlobalState().send(type, notifications);
    }

    public static void notification(String type, String title, String text, Duration hideDuration, Action... actions) {
        var notifications = Notifications.create()
                .title(title)
                .text(text)
                .hideAfter(hideDuration)
                .action(actions);

        getGlobalState().send(type, notifications);
    }

    public static MOptions options() {
        return MOptions.getInstance();
    }

    protected Mapton() {
        Platform.runLater(() -> {
            options().nightModeProperty().addListener((observable, oldValue, newValue) -> {
                for (var entry : WEB_ENGINE_TO_STYLE.entrySet()) {
                    applyHtmlCss(entry.getKey(), entry.getValue());
                }
            });
        });
    }

    public DoubleProperty zoomProperty() {
        return mZoomProperty;
    }

    private static class Holder {

        private static final Mapton INSTANCE = new Mapton();
    }
}
