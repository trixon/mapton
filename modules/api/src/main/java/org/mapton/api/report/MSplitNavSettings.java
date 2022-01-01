/*
 * Copyright 2022 Patrik Karlström.
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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class MSplitNavSettings {

    private String mTitle = null;
    private Color mTitleColor = Mapton.getThemeColor();
    private TitleMode mTitleMode = TitleMode.FULL_PATH;
    private ObservableList<Node> mToolBarItems = FXCollections.observableArrayList();

    public MSplitNavSettings() {
    }

    public String getTitle() {
        return mTitle;
    }

    public Color getTitleColor() {
        return mTitleColor;
    }

    public TitleMode getTitleMode() {
        return mTitleMode;
    }

    public ObservableList<Node> getToolBarItems() {
        return mToolBarItems;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setTitleColor(Color titleColor) {
        mTitleColor = titleColor;
    }

    public void setTitleMode(TitleMode titleMode) {
        mTitleMode = titleMode;
    }

    public void setToolBarItems(ObservableList<Node> items) {
        mToolBarItems = items;
    }

    public enum TitleMode {
        NONE,
        NAME,
        FULL_PATH,
        NAME_WITH_PARENT;
    }
}
