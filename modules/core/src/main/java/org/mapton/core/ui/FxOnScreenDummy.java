/*
 * Copyright 2022 Patrik Karlström.
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

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import org.controlsfx.control.Notifications;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class FxOnScreenDummy extends Label {

    public static FxOnScreenDummy getInstance() {
        return Holder.INSTANCE;
    }

    private FxOnScreenDummy() {
        initListener();
    }

    private void initListener() {
        Mapton.getGlobalState().addListener(gsce -> {
            Platform.runLater(() -> {
                Notifications notifications = gsce.getValue();
                notifications.owner(this).position(Pos.TOP_RIGHT);

                switch (gsce.getKey()) {
                    case MKey.NOTIFICATION_FX:
                        notifications.show();
                        break;

                    case MKey.NOTIFICATION_FX_CONFIRM:
                        notifications.showConfirm();
                        break;

                    case MKey.NOTIFICATION_FX_ERROR:
                        notifications.showError();
                        break;

                    case MKey.NOTIFICATION_FX_INFORMATION:
                        notifications.showInformation();
                        break;

                    case MKey.NOTIFICATION_FX_WARNING:
                        notifications.showWarning();
                        break;

                    default:
                        throw new AssertionError();
                }
            });
        }, MKey.NOTIFICATION_FX, MKey.NOTIFICATION_FX_CONFIRM, MKey.NOTIFICATION_FX_ERROR, MKey.NOTIFICATION_FX_INFORMATION, MKey.NOTIFICATION_FX_WARNING);
    }

    private static class Holder {

        private static final FxOnScreenDummy INSTANCE = new FxOnScreenDummy();
    }
}
