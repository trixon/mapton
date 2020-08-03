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

import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class MSimpleObjectStorage<T> {

    private T mDefaultValue = null;
    private String mGroup = Dict.MISCELLANEOUS.toString();
    private String mName;
    private String mPromptText;
    private String mTooltipText;
    private T mValue;

    public MSimpleObjectStorage() {
    }

    public T getDefaultValue() {
        return mDefaultValue;
    }

    public String getGroup() {
        return mGroup;
    }

    public String getName() {
        return mName;
    }

    public String getPromptText() {
        return mPromptText;
    }

    public String getTooltipText() {
        return mTooltipText;
    }

    public T getValue() {
        return mValue;
    }

    public void setDefaultValue(T defaultValue) {
        mDefaultValue = defaultValue;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setPromptText(String promptText) {
        mPromptText = promptText;
    }

    public void setTooltipText(String tooltipText) {
        mTooltipText = tooltipText;
    }

    public void setValue(T value) {
        mValue = value;
    }
}
