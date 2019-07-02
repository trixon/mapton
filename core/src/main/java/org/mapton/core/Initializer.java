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
package org.mapton.core;

import java.beans.PropertyChangeEvent;
import javafx.application.Platform;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.*;
import org.mapton.core.ui.AppToolBarProvider;
import org.openide.awt.Actions;
import org.openide.modules.OnStart;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.DarculaDefaultsManager;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.swing.RootPaneLayout;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.IconColor;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Initializer implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        NbLog.i("System", SystemHelper.getSystemInfo());
        Platform.setImplicitExit(false);

        DarculaDefaultsManager darculaDefaultsManager = DarculaDefaultsManager.getInstance();
        darculaDefaultsManager.putIfAbsent("invertIcons", "true");
        darculaDefaultsManager.putIfAbsent("stretchedTabs", "true");

        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");
        System.setProperty("netbeans.winsys.status_line.path", "AppStatusPanel.instance");

        boolean fullscreen = mOptions.isFullscreen();
        FxHelper.setDarkThemeEnabled(mOptions.is(KEY_UI_LAF_DARK, DEFAULT_UI_LAF_DARK));

        SwingUtilities.invokeLater(() -> {
            IconColor.setDefault(FxHelper.isDarkThemeEnabled() ? IconColor.WHITE : IconColor.BLACK);
            JFrame frame = (JFrame) Almond.getFrame();
            JComponent toolbar = AppToolBarProvider.getDefault().createToolbar();
            frame.getRootPane().setLayout(new RootPaneLayout(toolbar));
            toolbar.putClientProperty(JLayeredPane.LAYER_PROPERTY, 0);
            frame.getRootPane().getLayeredPane().add(toolbar, 0);
        });

        final WindowManager windowManager = WindowManager.getDefault();

        windowManager.invokeWhenUIReady(() -> {
            if (fullscreen) {
                Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction").actionPerformed(null);
            }

            if (mOptions.isMapOnly()) {
                windowManager.findTopComponent("MapTopComponent").requestActive();;
                Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
            }

            //Pre-load but don't display
            Almond.getTopComponent("ObjectPropertiesTopComponent");
            //Actions.forID("Window", "org.mapton.core.ui.MapTopComponent").actionPerformed(null);
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
