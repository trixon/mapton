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
package org.mapton.api.report;

import org.mapton.api.MMaskerPaneBase;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MSplitNavMaskerPane extends MMaskerPaneBase implements MSplitNavType {

    protected MSplitNavSettings mSplitNavSetting = new MSplitNavSettings();
    private String mName = "";
    private String mParent = "";

    public MSplitNavMaskerPane() {
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public String getParent() {
        return mParent;
    }

    @Override
    public MSplitNavSettings getSplitNavSettings() {
        return mSplitNavSetting;
    }

    @Override
    public void setName(String name) {
        mName = name;
    }

    @Override
    public void setParent(String parent) {
        mParent = parent;
    }

    @Override
    public String toString() {
        return getName();
    }

}
