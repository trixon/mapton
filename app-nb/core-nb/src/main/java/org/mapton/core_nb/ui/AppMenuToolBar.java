/*
 * Copyright 2020 Patrik Karlström.
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

import java.awt.BorderLayout;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import org.controlsfx.control.Notifications;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.base.ui.FxOnScreenDummy;
import se.trixon.almond.util.GlobalState;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AppMenuToolBar extends JPanel {

    private JLabel mStatusLabel;

    public AppMenuToolBar() {
        init();
        initListeners();
    }

    private void init() {
        setLayout(new BorderLayout());
        setBackground(FxHelper.colorToColor(Mapton.getThemeColor()));
        add(mStatusLabel = new JLabel("", SwingConstants.CENTER), BorderLayout.CENTER);
    }

    private void initListeners() {
        final GlobalState globalState = Mapton.getGlobalState();

        globalState.addListener(gsce -> {
            mStatusLabel.setText(gsce.getValue());
        }, MKey.APP_TOOL_LABEL);

        globalState.addListener(gsce -> {
            SwingHelper.runLater(() -> {
                setBackground(FxHelper.colorToColor(Mapton.getThemeColor()));
            });
        }, MKey.APP_THEME_BACKGROUND);

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                Notifications notifications = evt.getValue();
                notifications.owner(FxOnScreenDummy.getInstance()).position(Pos.TOP_RIGHT);

                switch (evt.getKey()) {
                    case MKey.NOTIFICATION:
                        notifications.show();
                        break;

                    case MKey.NOTIFICATION_CONFIRM:
                        notifications.showConfirm();
                        break;

                    case MKey.NOTIFICATION_ERROR:
                        notifications.showError();
                        break;

                    case MKey.NOTIFICATION_INFORMATION:
                        notifications.showInformation();
                        break;

                    case MKey.NOTIFICATION_WARNING:
                        notifications.showWarning();
                        break;

                    default:
                        throw new AssertionError();
                }
            });
        }, MKey.NOTIFICATION, MKey.NOTIFICATION_CONFIRM, MKey.NOTIFICATION_ERROR, MKey.NOTIFICATION_INFORMATION, MKey.NOTIFICATION_WARNING);
    }
}
