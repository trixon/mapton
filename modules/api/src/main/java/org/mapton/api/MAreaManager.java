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
package org.mapton.api;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

/**
 *
 * @author Patrik Karlström
 */
public class MAreaManager {

    private final ObjectProperty<ObservableList<MArea>> mItemsProperty = new SimpleObjectProperty<>();

    public static MAreaManager getInstance() {
        return Holder.INSTANCE;
    }

    private MAreaManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        initListeners();
    }

    public final ObservableList<MArea> getItems() {
        return mItemsProperty.get();
    }

    public final ObjectProperty<ObservableList<MArea>> itemsProperty() {
        return mItemsProperty;
    }

    private void initListeners() {
        getItems().addListener((ListChangeListener.Change<? extends MArea> c) -> {
            while (c.next()) {
            }
        });
    }

    private static class Holder {

        private static final MAreaManager INSTANCE = new MAreaManager();
    }
}
