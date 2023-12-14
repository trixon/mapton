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

import se.trixon.almond.util.fx.control.editable_list.EditableListItem;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseFilterFavorite implements EditableListItem {

    private String mCategory;
    private String mFreeText;
    private String mGroup;
    private String mName;
    private String mNameObject;

    public BaseFilterFavorite() {
    }

    public String getCategory() {
        return mCategory;
    }

    public String getFreeText() {
        return mFreeText;
    }

    public String getGroup() {
        return mGroup;
    }

    @Override
    public String getName() {
        return mName;
    }

    public String getNameObject() {
        return mNameObject;
    }

    public void setCategory(String category) {
        mCategory = category;
    }

    public void setFreeText(String freeText) {
        mFreeText = freeText;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNameObject(String nameObject) {
        mNameObject = nameObject;
    }

}
