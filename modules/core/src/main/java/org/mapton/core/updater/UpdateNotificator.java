/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.core.updater;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import javax.swing.Timer;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MNotificationIcons;
import org.mapton.api.MUpdater;
import org.openide.awt.NotificationDisplayer;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class UpdateNotificator {

    private final ResourceBundle mBundle = NbBundle.getBundle(UpdateNotificator.class);
    private final Timer mTimer;

    public UpdateNotificator() {
        mTimer = new Timer((int) TimeUnit.HOURS.toMillis(1), (ActionEvent e) -> {
            check();
        });

        mTimer.setInitialDelay((int) TimeUnit.MINUTES.toMillis(2));
        mTimer.start();
    }

    private void check() {
        for (var updater : Lookup.getDefault().lookupAll(MUpdater.ByFile.class)) {
            var file = updater.getFile();
            if (ObjectUtils.allNotNull(updater.getAgeLimit(), file)) {
                if (!file.exists() || SystemHelper.age(file.lastModified()) >= updater.getAgeLimit()) {
                    displayNotification();
                    break;
                }
            }
        }
    }

    private void displayNotification() {
        NotificationDisplayer.getDefault().notify(
                mBundle.getString("update_available"),
                MNotificationIcons.getInformationIcon(),
                mBundle.getString("updater_tool"),
                actionEvent -> {
                    Almond.openAndActivateTopComponent("UpdaterTopComponent");
                },
                NotificationDisplayer.Priority.NORMAL
        );
    }
}
