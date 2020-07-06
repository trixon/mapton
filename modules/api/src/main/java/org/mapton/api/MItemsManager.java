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
package org.mapton.api;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class MItemsManager<T> {

    private final ObservableList<T> mFilteredItems = FXCollections.observableArrayList();
    private final ObservableList<T> mRawItems = FXCollections.observableArrayList();
    private final ObservableList<T> mTimeFilteredItems = FXCollections.observableArrayList();

    public MItemsManager() {
    }

    public ObservableList<T> getFilteredItems() {
        return mFilteredItems;
    }

    public ObservableList<T> getRawItems() {
        return mRawItems;
    }

    public ObservableList<T> getTimeFilteredItems() {
        return mTimeFilteredItems;
    }
}
