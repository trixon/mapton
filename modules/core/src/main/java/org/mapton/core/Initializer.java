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
package org.mapton.core;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.prefs.BackingStoreException;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.ImageView;
import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.KEY_UI_LAF_DARK;
import org.mapton.api.Mapton;
import org.mapton.core.api.MaptonNb;
import org.mapton.core.updater.UpdateNotificator;
import org.openide.awt.Actions;
import org.openide.awt.HtmlBrowser;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.about.AboutAction;
import se.trixon.almond.nbp.dialogs.NbOptionalDialog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PrefsHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.AboutModel;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.PopOverWatcher;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        NbOptionalDialog.setPreferences(NbPreferences.forModule(NbOptionalDialog.class).node("optionalDialogState"));

        try {
            final String key = "laf";
            final String defaultLAF = "com.formdev.flatlaf.FlatDarkLaf";
            var preferences = NbPreferences.root().node("laf");
            PrefsHelper.putIfAbsent(preferences, key, defaultLAF);
        } catch (BackingStoreException ex) {
            //Exceptions.printStackTrace(ex);
        }

        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");

        boolean fullscreen = mOptions.isFullscreen();
        boolean mapOnly = mOptions.isMapOnly();
        FxHelper.setDarkThemeEnabled(mOptions.is(KEY_UI_LAF_DARK));

        SwingUtilities.invokeLater(() -> {
            var iconColor = mOptions.getIconColor();
            MaterialIcon.setDefaultColor(iconColor);
            se.trixon.almond.util.icons.material.swing.MaterialIcon.setDefaultColor(FxHelper.colorToColor(iconColor));
        });

        var windowManager = WindowManager.getDefault();
        windowManager.invokeWhenUIReady(() -> {
            PopOverWatcher.getInstance().setFrame((JFrame) Almond.getFrame());

            if (SystemUtils.IS_OS_MAC) {
                AboutAction.setFx(true);
                try {
                    String path = "/" + SystemHelper.getPackageAsPath(Initializer.class) + "logo.png";
                    var bufferedImage = ImageIO.read(Initializer.class.getResource(path));
                    var imageView = new ImageView(SwingFXUtils.toFXImage(bufferedImage, null));
                    AboutAction.setAboutModel(new AboutModel(SystemHelper.getBundle(Initializer.class, "about"), imageView));
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

            MaptonNb.progressStart(Dict.WARMING_UP.toString());
            Mapton.getGlobalState().put(MKey.APP_TOOL_LABEL, " ");//Forces repaint of AppMenuToolBar
            Mapton.getLog().setUseTimestamps(false);
            NbLog.setUseGlobalTag(false);
            Mapton.getLog().setOut((String s) -> {
                NbLog.i("", s);
            });
            Mapton.getLog().setErr((String s) -> {
                NbLog.e("", s);
            });
            Mapton.log(SystemHelper.getSystemInfo());

            if (fullscreen) {
                Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
            }

            if (mapOnly) {
                windowManager.findTopComponent("MapTopComponent").requestActive();
                Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
            }

            Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED, () -> {
                MaptonNb.progressStop(Dict.WARMING_UP.toString());
                SwingHelper.runLaterDelayed(10000, () -> {
                    //Pre-load but don't display
                    Almond.getTopComponent("ReportsTopComponent");
                    Almond.getTopComponent("EditorsTopComponent");
                    Almond.getTopComponent("UpdaterTopComponent");
                    Almond.getTopComponent("PropertiesTopComponent");
                    Almond.getTopComponent("ChartTopComponent");

                    if (mOptions.isFirstRun()) {
                        Actions.forID("Mapton", "org.mapton.core.actions.AboutMapsAction").actionPerformed(null);
                    }
                });
            });

            new UpdateNotificator();
        });

        Mapton.getGlobalState().addListener(gsce -> {
            Almond.openAndActivateTopComponent(gsce.getValue());
        }, MKey.LAYER_FAST_OPEN_TOOL);

        SystemHelper.setDesktopBrowser(url -> {
            try {
                HtmlBrowser.URLDisplayer.getDefault().showURL(new URL(url));
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

}
