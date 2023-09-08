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
package org.mapton.api.ui.forms;

import javafx.scene.control.ListView;
import org.mapton.api.MBaseDataManager;

/**
 *
 * @author Patrik Karlström
 * @param <ManagerType>
 * @param <ItemType>
 */
public class SingleListForm<ManagerType extends MBaseDataManager, ItemType> extends ListForm {

    private final ManagedList<ManagerType, ItemType> mManagedList;

    public SingleListForm(MBaseDataManager manager, String title) {
        super(title);
        mManagedList = new ManagedList<>(manager);
        setContent(mManagedList.getView());
    }

    public ListView<ItemType> getListView() {
        return mManagedList.getListView();
    }

    public ManagedList<ManagerType, ItemType> getManagedList() {
        return mManagedList;
    }
}
