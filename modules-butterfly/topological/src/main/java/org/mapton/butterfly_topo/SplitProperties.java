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
package org.mapton.butterfly_topo;

import java.util.LinkedHashMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.controlsfx.control.PropertySheet;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.PropertyItem;

/**
 *
 * @author Patrik Karlström
 */
public class SplitProperties extends TabPane {

    private final Tab mDashboardTab;
    private final Tab mDetailsTab;
    private final PropertySheet mPropertySheet = new PropertySheet();

    public SplitProperties() {
        mDashboardTab = new Tab(Dict.DASHBOARD.toString());
        mDetailsTab = new Tab(Dict.DETAILS.toString(), mPropertySheet);
        getTabs().setAll(mDetailsTab, mDashboardTab);
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);
        mPropertySheet.setMode(PropertySheet.Mode.NAME);

    }

    public void load(Node node, LinkedHashMap<String, Object> propertiesMap) {
        mDashboardTab.setContent(node);
        ObservableList<PropertySheet.Item> propertyItems = FXCollections.observableArrayList();
        propertiesMap.keySet().forEach((key) -> {
            propertyItems.add(new PropertyItem(propertiesMap, key));
        });

        mPropertySheet.getItems().setAll(propertyItems);
    }
}
