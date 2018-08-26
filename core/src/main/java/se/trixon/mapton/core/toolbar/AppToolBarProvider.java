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
package se.trixon.mapton.core.toolbar;

import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javax.swing.JComponent;
import org.openide.util.Lookup;
import se.trixon.mapton.core.api.Mapton;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBar;

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
            fxPanel.setPreferredSize(new Dimension(100, (int) (getIconSizeToolBar() * 1.2)));

            Platform.runLater(() -> {
                AppToolBar appToolBar = new AppToolBar();
                Mapton.setToolBar(appToolBar);
                Scene scene = new Scene(appToolBar);
                fxPanel.setScene(scene);
            });

            return fxPanel;
        }
    }
}
