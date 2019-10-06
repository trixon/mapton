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

import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mapton.workbench.modules.map.MapWindow;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class WindowSectionMiddle extends WindowSectionBase {

    private TabPane mTabPane = new TabPane();
    private MapWindow mMapWindow = MapWindow.getInstance();

    public WindowSectionMiddle() {
        getItems().setAll(mMapWindow, mTabPane);
        Tab tab = new Tab(Dict.CHART.toString());
        mTabPane.getTabs().add(tab);
    }
}
