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
package org.mapton.butterfly_core.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.ArrayList;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class FilterFavoriteManager {

    private final Gson mGson = new GsonBuilder()
            .setVersion(1.0)
            .serializeNulls()
            .create();
    private final Preferences mPreferences = NbPreferences.forModule(FilterFavoriteManager.class);

    public static FilterFavoriteManager getInstance() {
        return Holder.INSTANCE;
    }

    private FilterFavoriteManager() {
    }

    public <T> ArrayList<T> load(Class cls) {
        //TODO Return list for
        return null;
        //return ny get nedan
    }

    public <T extends BaseFilterFavorite> ObjectProperty<ObservableList<T>> createItemsProperty(Class cls) {
        var json = mPreferences.get(cls.getName(), "");
        var items = mGson.fromJson(json, ArrayList.class);

        var itemsProperty = new SimpleObjectProperty<ObservableList<T>>();
        itemsProperty.setValue(FXCollections.observableArrayList());

        if (items != null) {
            itemsProperty.get().setAll(items);
        }

        return itemsProperty;
    }
//    private final HashMap<Class, SimpleObjectProperty<ObservableList>> mClassToItems = new HashMap<>();
////    private final HashMap<Class, ObjectProperty<ObservableList>> mClassToItems = new HashMap<>();
//
//    public <T extends BaseFilterFavorite> ObjectProperty<ObservableList<T>> itemsProperty(Class cls) {
//        if (!mClassToItems.containsKey(cls)) {
//            var itemsProperty = new SimpleObjectProperty<ObservableList<T>>();
//            mClassToItems.put(cls, itemsProperty);
//        }
//
//        var xx = mClassToItems.computeIfAbsent(cls, k -> {
//
//            var itemsProperty = new SimpleObjectProperty<ObservableList<T>>();
////            itemsProperty.se
//            itemsProperty.setValue(FXCollections.observableArrayList());
//
//            return itemsProperty;
//        });
//
//        return (ObjectProperty<ObservableList<T>>) xx;
//    }
//    private final HashMap<Class, String> mClassToItems2 = new HashMap<>();
//
//    public <T extends BaseFilterFavorite> String itemsProperty2(Class cls) {
//        return mClassToItems2.computeIfAbsent(cls, k -> {
//
//            var itemsProperty = "INITIAL VALUE";
////            itemsProperty.se
////            itemsProperty.setValue(FXCollections.observableArrayList());
//
//            return itemsProperty;
//        });
//    }

    public <T> void save(Class cls, ArrayList<T> list) {
        mPreferences.put(cls.getName(), mGson.toJson(list));
    }

    private static class Holder {

        private static final FilterFavoriteManager INSTANCE = new FilterFavoriteManager();
    }
    /*
    private final ObjectProperty<ObservableList<Task>> mItemsProperty = new SimpleObjectProperty<>();

<T> getItems(class cls)
    computeifabsent
tar bort behovet av list arg i save
     */

}
