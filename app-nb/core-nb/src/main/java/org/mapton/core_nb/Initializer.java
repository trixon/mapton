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
package org.mapton.core_nb;

import java.beans.PropertyChangeEvent;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.KEY_UI_LAF_DARK;
import org.mapton.api.Mapton;
import org.mapton.core_nb.api.MMapMagnet;
import org.mapton.core_nb.ui.AppToolBarProvider;
import org.mapton.core_nb.updater.UpdateNotificator;
import org.openide.awt.Actions;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.about.AboutAction;
import se.trixon.almond.nbp.swing.RootPaneLayout;
import se.trixon.almond.util.PrefsHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.SystemHelperFx;
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
        try {
            final String key = "laf";
            final String defaultLAF = "com.formdev.flatlaf.FlatDarkLaf";
            Preferences preferences = NbPreferences.root().node("laf");
            PrefsHelper.putIfAbsent(preferences, key, defaultLAF);
            if (preferences.get(key, "-").equalsIgnoreCase("com.bulenkov.darcula.DarculaLaf")) {
                preferences.put(key, defaultLAF);
            }
        } catch (BackingStoreException ex) {
            //Exceptions.printStackTrace(ex);
        }
        Mapton.getLog().setUseTimestamps(false);
        NbLog.setUseGlobalTag(false);
        Mapton.getLog().setOut((String s) -> {
            NbLog.i("", s);
        });
        Mapton.getLog().setErr((String s) -> {
            NbLog.e("", s);
        });
        Mapton.log(SystemHelper.getSystemInfo());

//        Platform.setImplicitExit(false);
//        new JFXPanel();
        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");
        System.setProperty("netbeans.winsys.status_line.path", "AppStatusPanel.instance");

        boolean fullscreen = mOptions.isFullscreen();
        boolean mapOnly = mOptions.isMapOnly();
        FxHelper.setDarkThemeEnabled(mOptions.is(KEY_UI_LAF_DARK));

        SwingUtilities.invokeLater(() -> {
            MaterialIcon.setDefaultColor(mOptions.getIconColor());
            SwingHelper.runLaterDelayed(10, () -> {
                JFrame frame = (JFrame) Almond.getFrame();
                PopOverWatcher.getInstance().setFrame(frame);
                JComponent toolbar = AppToolBarProvider.getInstance().getToolBarPanel();
                frame.getRootPane().setLayout(new RootPaneLayout(toolbar));
                toolbar.putClientProperty(JLayeredPane.LAYER_PROPERTY, 0);
                frame.getRootPane().getLayeredPane().add(toolbar, 0);

                if (SystemUtils.IS_OS_MAC) {
                    AboutAction.setFx(true);
                    AboutAction.setAboutModel(new AboutModel(SystemHelper.getBundle(Initializer.class, "about"), SystemHelperFx.getResourceAsImageView(Initializer.class, "logo.png")));
                }
            });
        });

        final WindowManager windowManager = WindowManager.getDefault();

        windowManager.invokeWhenUIReady(() -> {
            if (fullscreen) {
                Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
            }

            if (mapOnly) {
                windowManager.findTopComponent("MapTopComponent").requestActive();;
                Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
            }

            SwingHelper.runLaterDelayed(45 * 1000, () -> {
                //Pre-load but don't display
                Almond.getTopComponent("ReportsTopComponent");
                Almond.getTopComponent("EditorsTopComponent");
                Almond.getTopComponent("UpdaterTopComponent");
                Almond.getTopComponent("ObjectPropertiesTopComponent");
                Almond.getTopComponent("ChartTopComponent");
            });
            new Thread().start();

            new UpdateNotificator();
        });

        //Activate MapTopComponent when opening MapMagnets
        TopComponent.getRegistry().addPropertyChangeListener((PropertyChangeEvent evt) -> {
            if (evt.getPropertyName().equals("tcOpened")) {
                TopComponent tc = (TopComponent) evt.getNewValue();
                if (tc instanceof MMapMagnet) {
                    Almond.requestActive("MapTopComponent");
                    //tc.requestActive();
                }
            }
        });
    }
}
