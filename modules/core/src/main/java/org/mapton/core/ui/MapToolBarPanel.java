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
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import org.controlsfx.control.NotificationPane;
import org.mapton.api.MBannerManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MapToolBarPanel {

    private Label mNotificationLabel;
    private NotificationPane mNotificationPane;
    private final MapToolBar mToolBar;
    private final JFXPanel mToolBarPanel = new JFXPanel();

    public static MapToolBarPanel getInstance() {
        return Holder.INSTANCE;
    }

    private MapToolBarPanel() {
        mToolBarPanel.setVisible(false);
        mToolBarPanel.setPreferredSize(new Dimension(100, (int) (getIconSizeToolBarInt() * 1.7)));

        mToolBar = new MapToolBar();
        Platform.runLater(() -> {
            mNotificationPane = new NotificationPane(mToolBar);
            mNotificationLabel = new Label();
            mNotificationLabel.setBackground(FxHelper.createBackground(Color.RED));
            mNotificationLabel.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8));

            mNotificationLabel.prefHeightProperty().bind(mNotificationPane.heightProperty());
            mNotificationLabel.prefWidthProperty().bind(mNotificationPane.widthProperty());
            mNotificationLabel.setFont(Font.font(Font.getDefault().getSize() * 1.4));
            mNotificationLabel.setTextFill(Color.WHITE);
//            if (!Mapton.isNightMode()) {
//                mNotificationPane.getStyleClass().add(NotificationPane.STYLE_CLASS_DARK);
//            }
            var bannerManager = MBannerManager.getInstance();
            bannerManager.messageProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    FxHelper.runLater(() -> {
                        mNotificationLabel.setText(newValue);
                        mNotificationLabel.setGraphic(bannerManager.getGraphic());

                        mNotificationPane.show("", mNotificationLabel);
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
