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
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class WindowSectionRight extends WindowSectionBase {

    private final TabPane mTabPane0 = new TabPane();
    private final TabPane mTabPane1 = new TabPane();

    public WindowSectionRight() {
        getItems().setAll(mTabPane0, mTabPane1);

        Tab tool1Tab = new Tab("Tool #1");
        Tab tool2Tab = new Tab("Tool #2");
        Tab tool3Tab = new Tab("Tool #3");
        mTabPane0.getTabs().addAll(tool1Tab, tool2Tab, tool3Tab);

        Tab bookmarkTab = new Tab(Dict.OBJECT_PROPERTIES.toString());
        mTabPane1.getTabs().add(bookmarkTab);
    }

}
