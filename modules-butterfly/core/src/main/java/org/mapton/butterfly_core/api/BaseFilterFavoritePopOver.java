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

import org.mapton.api.MDict;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MFilterPopOver;
import org.mapton.api.ui.MPopOver;
import se.trixon.almond.nbp.fx.NbEditableList;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.editable_list.EditableList;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class BaseFilterFavoritePopOver<T extends BaseFilterFavorite> extends MPopOver {

    protected EditableList<T> mEditableList;
    private final MFilterPopOver mFilterPopOver;
    protected final FilterFavoriteManager mManager = FilterFavoriteManager.getInstance();
    private final Class<T> mTypeParameterClass;

    public BaseFilterFavoritePopOver(Class<T> typeParameterClass, MFilterPopOver filterPopOver) {
        mTypeParameterClass = typeParameterClass;
        mFilterPopOver = filterPopOver;

        String title = MDict.FILTER_FAVORITES.toString();
        setTitle(title);
        getAction().setText(title);
        getAction().setGraphic(MaterialIcon._Action.BOOKMARK_BORDER.getImageView(getIconSizeToolBarInt()));

        init();

        setContentNode(mEditableList);
    }

    private void init() {

        mEditableList = new NbEditableList.Builder<T>()
                .setItemSingular(Dict.PROFILE.toString())
                .setItemPlural(Dict.PROFILES.toString())
                .setItemsProperty(mManager.createItemsProperty(mTypeParameterClass))
                .setOnEdit((title, filterFavorite) -> {
                    onEdit(title, filterFavorite);
                    mEditableList.getListView().getItems().add(filterFavorite);
                })
                .setOnRemoveAll(() -> {
//                    mTaskManager.getIdToItem().clear();
//                    StorageManager.save();
                })
                .setOnRemove(t -> {
//                    mTaskManager.getIdToItem().remove(t.getId());
//                    StorageManager.save();
                })
                //                .setOnClone(t -> {
                //                    var original = t;
                //                    var json = GSON.toJson(original);
                //                    var clone = GSON.fromJson(json, original.getClass());
                //                    var uuid = UUID.randomUUID().toString();
                //                    clone.setId(uuid);
                //                    clone.setLastRun(0);
                //                    clone.setName("%s %s".formatted(clone.getName(), LocalDate.now().toString()));
                //                    mTaskManager.getIdToItem().put(clone.getId(), clone);
                //
                //                    StorageManager.save();
                //
                //                    return mTaskManager.getById(uuid);
                //                    return null;
                //                })
                //                .setOnSave(t -> {
                ////                    mExecutorManager.requestStart(task);
                //                    return null;
                //                })
                .build();

//        mEditableList.getListView().setCellFactory(listView -> new TaskListCell());
    }

    public abstract void onEdit(String title, T filterFavorite);

}
