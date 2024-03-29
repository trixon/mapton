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
package org.mapton.api;

import java.util.ArrayList;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.ObjectUtils;

/**
 *
 * @author Patrik Karlström
 */
public class MTemporaryPoiManager {

    private final ObjectProperty<ObservableList<MPoi>> mItemsProperty = new SimpleObjectProperty<>();

    public static void clear() {
        getInstance().getItems().clear();
    }

    public static MTemporaryPoiManager getInstance() {
        return Holder.INSTANCE;
    }

    private MTemporaryPoiManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
    }

    public void fitToBounds() {
        ArrayList<MLatLon> latLons = new ArrayList<>();
        getItems().stream()
                .filter(poi -> ObjectUtils.allNotNull(poi.getLatitude(), poi.getLongitude()))
                .forEach(poi -> {
                    latLons.add(new MLatLon(poi.getLatitude(), poi.getLongitude()));
                });

        Mapton.getEngine().fitToBounds(new MLatLonBox(latLons));
    }

    public final ObservableList<MPoi> getItems() {
        return mItemsProperty.get();
    }

    public void goTo(MPoi poi) {
        Mapton.getEngine().panTo(new MLatLon(poi.getLatitude(), poi.getLongitude()), poi.getZoom());
    }

    public final ObjectProperty<ObservableList<MPoi>> itemsProperty() {
        return mItemsProperty;
    }

    private static class Holder {

        private static final MTemporaryPoiManager INSTANCE = new MTemporaryPoiManager();
    }
}
