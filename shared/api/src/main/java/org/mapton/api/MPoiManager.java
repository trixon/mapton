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

import java.util.ArrayList;
import java.util.Comparator;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public class MPoiManager {

    private final ObjectProperty<ObservableList<MPoi>> mAllItems = new SimpleObjectProperty<>();
    private final ObjectProperty<ObservableList<MPoi>> mFilteredItems = new SimpleObjectProperty<>();

    public static MPoiManager getInstance() {
        return Holder.INSTANCE;
    }

    private MPoiManager() {
        mAllItems.setValue(FXCollections.observableArrayList());
        mFilteredItems.setValue(FXCollections.observableArrayList());

        initListeners();

        refresh("");
    }

    public ObjectProperty<ObservableList<MPoi>> allItemsProperty() {
        return mAllItems;
    }

    public ObjectProperty<ObservableList<MPoi>> filteredItemsProperty() {
        return mFilteredItems;
    }

    public final ObservableList<MPoi> getAllItems() {
        return mAllItems == null ? null : mAllItems.get();
    }

    public final ObservableList<MPoi> getFilteredItems() {
        return mFilteredItems == null ? null : mFilteredItems.get();
    }

    public void refresh(String filter) {
        ArrayList<MPoi> allPois = new ArrayList<>();
        ArrayList<MPoi> filteredPois = new ArrayList<>();

        for (MPoiProvider poiProvider : Lookup.getDefault().lookupAll(MPoiProvider.class)) {
            for (MPoi poi : poiProvider.getPois()) {
                poi.setProvider(poiProvider.getName());
                allPois.add(poi);
                if (validPoi(poi, filter)) {
                    filteredPois.add(poi);
                }
            }
        }

        Comparator<MPoi> comparator = Comparator.comparing(MPoi::getProvider)
                .thenComparing(Comparator.comparing(MPoi::getCategory))
                .thenComparing(Comparator.comparing(MPoi::getName));
        filteredPois.sort(comparator);

        mAllItems.getValue().setAll(allPois);
        mFilteredItems.getValue().setAll(filteredPois);
    }

    private void initListeners() {
        Lookup.getDefault().lookupResult(MPoiProvider.class).addLookupListener(lookupEvent -> {
            refresh("");
        });
    }

    private boolean validPoi(MPoi poi, String filter) {
        boolean valid
                = StringUtils.containsIgnoreCase(poi.getProvider(), filter)
                || StringUtils.containsIgnoreCase(poi.getName(), filter)
                || StringUtils.containsIgnoreCase(poi.getCategory(), filter)
                || StringUtils.containsIgnoreCase(poi.getGroup(), filter);

        return valid;
    }

    private static class Holder {

        private static final MPoiManager INSTANCE = new MPoiManager();
    }
}
