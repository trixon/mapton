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
package org.mapton.core_sw.ui;

import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.JComponent;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.mapton.core_sw.api.Mapton;
import org.openide.util.Lookup;
import se.trixon.almond.util.fx.FxHelper;

/**
 * From https://dzone.com/articles/how-create-tabbed-toolbar-on-nb
 *
 * @author Patrik Karlström
 */
public abstract class AppToolBarProvider {

    public static AppToolBarProvider getDefault() {
        AppToolBarProvider provider = Lookup.getDefault().lookup(AppToolBarProvider.class);

        if (provider == null) {
            provider = new DefaultToolbarComponentProvider();
        }

        return provider;
    }

    public abstract JComponent createToolbar();

    private static class DefaultToolbarComponentProvider extends AppToolBarProvider {

        @Override
        public JComponent createToolbar() {
            JFXPanel fxPanel = new JFXPanel();
            fxPanel.setVisible(false);

            fxPanel.setPreferredSize(new Dimension(100, (int) (getIconSizeToolBar() * 1.2)));
            AppToolBar appToolBar = new AppToolBar();
            Mapton.setToolBar(appToolBar);
            Platform.runLater(() -> {
                Scene scene = new Scene(appToolBar);
                fxPanel.setScene(scene);
                fxPanel.setVisible(true);
                FxHelper.loadDarkTheme(fxPanel.getScene());
            });

            return fxPanel;
        }
    }
}
