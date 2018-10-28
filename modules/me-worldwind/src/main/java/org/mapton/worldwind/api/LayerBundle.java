/*
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.worldwind.api;

import gov.nasa.worldwind.layers.Layer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author Patrik Karlström
 */
public abstract class LayerBundle {

    private final ObservableList<Layer> mLayers = FXCollections.observableArrayList();
    private final StringProperty mName = new SimpleStringProperty();
    private boolean mPopulated = false;

    public LayerBundle() {
    }

    public ObservableList<Layer> getLayers() {
        return mLayers;
    }

    public final String getName() {
        return mName.get();
    }

    public boolean isPopulated() {
        return mPopulated;
    }

    public final StringProperty nameProperty() {
        return mName;
    }

    public abstract void populate() throws Exception;

    public final void setName(String value) {
        mName.set(value);
    }

    public void setPopulated(boolean populated) {
        mPopulated = populated;
    }

}
