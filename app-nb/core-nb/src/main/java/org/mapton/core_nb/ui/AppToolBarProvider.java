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
package org.mapton.core_nb.ui;

import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AppToolBarProvider {

    private final AppToolBar mToolBar;
    private final JFXPanel mToolBarPanel = new JFXPanel();

    public static AppToolBarProvider getInstance() {
        return Holder.INSTANCE;
    }

    private AppToolBarProvider() {
        mToolBarPanel.setVisible(false);
        mToolBarPanel.setPreferredSize(new Dimension(100, (int) (getIconSizeToolBar() * 1.2)));

        mToolBar = new AppToolBar();
        Platform.runLater(() -> {
            Scene scene = new Scene(new BorderPane(mToolBar)); //Wrap it in order to be able to change the background
            mToolBarPanel.setScene(scene);
            mToolBarPanel.setVisible(true);
            FxHelper.loadDarkTheme(mToolBarPanel.getScene());
        });
    }

    public AppToolBar getToolBar() {
        return mToolBar;
    }

    public JFXPanel getToolBarPanel() {
        return mToolBarPanel;
    }

    private static class Holder {

        private static final AppToolBarProvider INSTANCE = new AppToolBarProvider();
    }
}
