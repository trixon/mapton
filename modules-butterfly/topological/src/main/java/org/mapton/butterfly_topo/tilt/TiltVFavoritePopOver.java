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
package org.mapton.butterfly_topo.tilt;

import org.mapton.butterfly_core.api.BaseFilterFavoritePopOver;
import org.mapton.butterfly_core.api.BaseFilterPopOver;

/**
 *
 * @author Patrik Karlström
 */
public class TiltVFavoritePopOver extends BaseFilterFavoritePopOver<TiltVFilterFavorite> {

    public TiltVFavoritePopOver(BaseFilterPopOver filterPopOver) {
        super(TiltVFilterFavorite.class, filterPopOver);
//        filterPopOver.applyFilter(this);
//        ObjectProperty<ObservableList<TopoFilterFavorite>> xx = mManager.<TopoFilterFavorite>itemsProperty(getClass());
//        mEditableList.getListView().setItems(mManager.itemsProperty(getClass()));
    }

    @Override
    public void onEdit(String title, TiltVFilterFavorite filterFavorite) {
        if (filterFavorite == null) {
            filterFavorite = new TiltVFilterFavorite();
        }

        filterFavorite.setName("objekt");
        filterFavorite.setNameObject("NameObjekt");
    }
}
