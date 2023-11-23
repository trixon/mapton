/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_core.indicators;

import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Patrik Karlström
 */
public class ScaleRulerManager {

    private final ObjectProperty<ObservableList<Position>> mItemsProperty = new SimpleObjectProperty<>();

    public static ScaleRulerManager getInstance() {
        return Holder.INSTANCE;
    }

    private ScaleRulerManager() {
        mItemsProperty.setValue(FXCollections.synchronizedObservableList(FXCollections.observableArrayList()));
    }

    public void add(Position position) {
        getItems().add(position);
    }

    public void clear() {
        getItems().clear();
    }

    public ObservableList<Position> getItems() {
        return mItemsProperty.get();
    }

    public ObjectProperty<ObservableList<Position>> itemsProperty() {
        return mItemsProperty;
    }

    private static class Holder {

        private static final ScaleRulerManager INSTANCE = new ScaleRulerManager();
    }
}
