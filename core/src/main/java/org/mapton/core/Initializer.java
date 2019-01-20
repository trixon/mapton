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

import javafx.application.Platform;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLayeredPane;
import javax.swing.SwingUtilities;
import org.mapton.api.MOptions;
import org.mapton.core.ui.AppToolBarProvider;
import org.openide.awt.Actions;
import org.openide.modules.OnStart;
import org.openide.windows.WindowManager;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.nbp.swing.RootPaneLayout;
import se.trixon.almond.util.SystemHelper;
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

        System.setProperty("netbeans.winsys.no_help_in_dialogs", "true");
        System.setProperty("netbeans.winsys.no_toolbars", "true");
        System.setProperty("netbeans.winsys.status_line.path", "AppStatusPanel.instance");

        boolean fullscreen = mOptions.isFullscreen();

        SwingUtilities.invokeLater(() -> {
            IconColor.initFx();
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

            //Actions.forID("Window", "org.mapton.core.ui.MapTopComponent").actionPerformed(null);
        });
    }
}
