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
package org.mapton.core.ui;

import java.awt.Dimension;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.controlsfx.control.NotificationPane;
import org.mapton.api.MBannerManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MapToolBarPanel {

    private final MapToolBar mToolBar;
    private final JFXPanel mToolBarPanel = new JFXPanel();
    private NotificationPane mNotificationPane;

    public static MapToolBarPanel getInstance() {
        return Holder.INSTANCE;
    }

    private MapToolBarPanel() {
        mToolBarPanel.setVisible(false);
        mToolBarPanel.setPreferredSize(new Dimension(100, (int) (getIconSizeToolBarInt() * 1.7)));

        mToolBar = new MapToolBar();
        Platform.runLater(() -> {
            mNotificationPane = new NotificationPane(mToolBar);
            if (Mapton.isNightMode()) {
                mNotificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
            }

            var bannerManager = MBannerManager.getInstance();
            bannerManager.messageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    FxHelper.runLater(() -> {
                        mNotificationPane.show(newValue, bannerManager.getGraphic());
                    });
                }
            });

            Scene scene = new Scene(mNotificationPane);
            mToolBarPanel.setScene(scene);
            mToolBarPanel.setVisible(true);
            FxHelper.loadDarkTheme(mToolBarPanel.getScene());
        });
    }

    public MapToolBar getToolBar() {
        return mToolBar;
    }

    public JFXPanel getToolBarPanel() {
        return mToolBarPanel;
    }

    private static class Holder {

        private static final MapToolBarPanel INSTANCE = new MapToolBarPanel();
    }
}
