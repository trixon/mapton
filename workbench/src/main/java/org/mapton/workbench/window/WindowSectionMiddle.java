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
package org.mapton.workbench.window;

import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class WindowSectionMiddle extends WindowSectionBase {

    private StackPane mStackPane = new StackPane();
    private TabPane mTabPane = new TabPane();

    public WindowSectionMiddle() {
        getItems().setAll(mStackPane, mTabPane);
        mStackPane.getChildren().add(new Label("map"));
        Tab tab = new Tab(Dict.CHART.toString());
        mTabPane.getTabs().add(tab);
    }
}
