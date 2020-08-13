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
package org.mapton.base.ui.updater;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mapton.api.MKey;
import org.mapton.api.MMaskerPaneBase;
import org.mapton.api.MPrint;
import org.mapton.api.MUpdater;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterMaskerPane extends MMaskerPaneBase {

    private MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);
    private final BooleanProperty mRunningProperty = new SimpleBooleanProperty(false);

    public UpdaterMaskerPane() {
    }

    public BooleanProperty runningProperty() {
        return mRunningProperty;
    }

    public void update(ObservableList<MUpdater> updaters, Runnable r) {
        mMaskerPane.setVisible(true);
        mRunningProperty.set(true);
        new Thread(() -> {
            for (MUpdater updater : FXCollections.observableArrayList(updaters)) {//Avoid java.util.ConcurrentModificationException
                if (updater.isMarkedForUpdate()) {
                    mPrint.out(String.format("%s %s/%s", "Update", updater.getCategory(), updater.getName()));
                    updater.run();
                    mPrint.out(String.format("%s %s/%s, %s", "Update", updater.getCategory(), updater.getName(), Dict.DONE.toString().toLowerCase()));
                }
            }

            Platform.runLater(() -> {
                r.run();
                mMaskerPane.setVisible(false);
                notify(Dict.OPERATION_COMPLETED.toString());
                mRunningProperty.set(false);
            });
        }).start();
    }

}
