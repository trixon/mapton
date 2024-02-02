/*
 * Copyright 2024 Patrik Karlström.
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

import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Tab;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public class CheckedTab extends Tab {

    private final CheckBox mTabCheckBox;
    private final String mKey;

    public CheckedTab(String title, Node node, String key) {
        super(title, node);
        mKey = key;
        mTabCheckBox = new CheckBox();
        setGraphic(mTabCheckBox);
        disableProperty().bind(mTabCheckBox.selectedProperty().not());

        mTabCheckBox.selectedProperty().addListener((p, o, n) -> {
            if (getTabPane() != null) {
                if (n) {
                    getTabPane().getSelectionModel().select(this);
                } else {
                    for (var tab : getTabPane().getTabs()) {
                        if (!tab.isDisabled()) {
                            getTabPane().getSelectionModel().select(tab);
                        }
                    }
                }
            }
            Mapton.getGlobalState().put(key, n);
        });
    }

    public String getKey() {
        return mKey;
    }

    public CheckBox getTabCheckBox() {
        return mTabCheckBox;
    }

}
