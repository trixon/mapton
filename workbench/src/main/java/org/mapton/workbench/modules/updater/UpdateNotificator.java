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
package org.mapton.workbench.modules.updater;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.concurrent.TimeUnit;
import javafx.util.Duration;
import javax.swing.Timer;
import org.apache.commons.lang3.ObjectUtils;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MPrint;
import org.mapton.api.MUpdater;
import org.mapton.api.Mapton;
import org.mapton.workbench.api.WorkbenchManager;
import org.mapton.workbench.modules.UpdaterModule;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class UpdateNotificator {

    private MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);
    private final Timer mTimer;

    public UpdateNotificator() {
        mTimer = new Timer((int) TimeUnit.HOURS.toMillis(1), (ActionEvent e) -> {
            check();
        });

        mTimer.setInitialDelay((int) TimeUnit.SECONDS.toMillis(15));
        mTimer.start();
    }

    private void alert(MUpdater.ByFile updater) {
        String name = updater.getName();
        if (updater.getCategory() != null) {
            name = String.format("%s/%s", updater.getCategory(), updater.getName());
        }

        Action action = new Action(Dict.UPDATER.toString(), (eventHandler) -> {
            WorkbenchManager.getInstance().openModule(UpdaterModule.class);
        });

        Mapton.notification(MKey.NOTIFICATION_WARNING, Dict.UPDATE.toString(), name, Duration.seconds(10), action);
    }

    private void check() {
        Lookup.getDefault().lookupAll(MUpdater.ByFile.class).stream()
                .forEach((updater) -> {
                    final File file = updater.getFile();
                    if (ObjectUtils.allNotNull(updater.getAgeLimit(), file)) {
                        if (!file.exists() || SystemHelper.age(file.lastModified()) >= updater.getAgeLimit()) {
                            mPrint.out(String.format("%s: %s is out of date", Dict.UPDATER.toString(), updater.getName()));
                            alert(updater);
                        }
                    }
                });
    }
}
