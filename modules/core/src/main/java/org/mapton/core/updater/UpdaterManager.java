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
package org.mapton.core.updater;

import java.util.ArrayList;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mapton.api.MKey;
import org.mapton.api.MPrint;
import org.mapton.api.MUpdater;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public class UpdaterManager {

    private final ObjectProperty<ObservableList<MUpdater>> mItemsProperty = new SimpleObjectProperty<>();
    private final MPrint mPrint = new MPrint(MKey.UPDATER_LOGGER);
    private final BooleanProperty mSelectedProperty = new SimpleBooleanProperty(false);

    public static UpdaterManager getInstance() {
        return Holder.INSTANCE;
    }

    private UpdaterManager() {
        mItemsProperty.set(FXCollections.observableArrayList());

        Lookup.getDefault().lookupResult(MUpdater.class).addLookupListener(lookupEvent -> {
            populate();
        });

        populate();
    }

    public ObservableList<MUpdater> getItems() {
        return mItemsProperty.get();
    }

    public ObjectProperty<ObservableList<MUpdater>> itemsProperty() {
        return mItemsProperty;
    }

    public void populate() {
        new Thread(() -> {
            var updaters = new ArrayList<>(Lookup.getDefault().lookupAll(MUpdater.class));
            for (var updater : updaters) {
                updater.setMarkedForUpdate(updater.isOutOfDate());
                if (updater.isAutoUpdate()) {
                    updater.setAutoUpdatePostRunnable(() -> {
                        populate();
                    });
                }
                String status;
                if (updater.isOutOfDate()) {
                    status = "is out of date";
                } else {
                    status = "OK";
                }

                mPrint.out("%s: %s %s".formatted("Status check", updater.getName(), status));
            }

            Comparator<MUpdater> c1 = (o1, o2) -> Boolean.compare(o2.isOutOfDate(), o1.isMarkedForUpdate());
            Comparator<MUpdater> c2 = (o1, o2) -> o1.getCategory().compareTo(o2.getCategory());
            Comparator<MUpdater> c3 = (o1, o2) -> o1.getName().compareTo(o2.getName());

            updaters.sort(c1.thenComparing(c2).thenComparing(c3));

            Platform.runLater(() -> {
                getItems().setAll(updaters);
                refreshSelectedProperty();
            });
        }, getClass().getCanonicalName()).start();
    }

    public void refreshSelectedProperty() {
        var markedForUpdate = false;

        for (var item : getItems()) {
            if (item.isMarkedForUpdate()) {
                markedForUpdate = true;
                break;
            }
        }

        mSelectedProperty.set(markedForUpdate);
    }

    public BooleanProperty selectedProperty() {
        return mSelectedProperty;
    }

    private static class Holder {

        private static final UpdaterManager INSTANCE = new UpdaterManager();
    }
}
