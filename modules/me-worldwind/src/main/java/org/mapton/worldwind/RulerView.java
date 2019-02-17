/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.worldwind;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Patrik Karlström
 */
public class RulerView extends TabPane {

    private int mTabCounter = 0;

    public static RulerView getInstance() {
        return Holder.INSTANCE;
    }

    private RulerView() {
        createUI();
        initListeners();
    }

    private void addTab() {
        RulerTab rulerTab = new RulerTab(Integer.toString(++mTabCounter));
        getTabs().add(rulerTab);
        getSelectionModel().select(rulerTab);
    }

    private void createUI() {
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        getTabs().add(plusTab);
        addTab();
    }

    private void initListeners() {
        getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab t, Tab t1) -> {
            if (getSelectionModel().getSelectedIndex() == 0) {
                addTab();
            }
        });
    }

    private static class Holder {

        private static final RulerView INSTANCE = new RulerView();
    }

}
