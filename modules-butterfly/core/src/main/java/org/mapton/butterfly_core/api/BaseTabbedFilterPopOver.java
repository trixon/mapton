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
package org.mapton.butterfly_core.api;

import javafx.scene.Node;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import org.mapton.api.ui.forms.CheckedTab;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTabbedFilterPopOver extends BaseFilterPopOver {

    private final CheckedTab mBasicTab = new CheckedTab("Grunddata", "basic");
    private final CheckedTab mDisruptorTab = new CheckedTab("Störningskällor", "disruptor");
    private final CheckedTab mDateTab = new CheckedTab(Dict.DATE.toString(), "date");
    private final CheckedTab mMeasTab = new CheckedTab(SDict.MEASUREMENTS.toString(), "measurements");
    private final TabPane mTabPane = new TabPane();

    public BaseTabbedFilterPopOver() {
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setContentNode(mTabPane);
    }

    public void addBasicTab(Node node) {
        mBasicTab.setContent(node);
        mBasicTab.getTabCheckBox().setSelected(true);
        addPadding(node);
        mTabPane.getTabs().add(mBasicTab);
    }

    public void addBasicTab(Node node, String title) {
        mBasicTab.setText(title);
        addBasicTab(node);
    }

    public void addDisruptorTab(Node node) {
        mDisruptorTab.setContent(node);
        mDisruptorTab.getTabCheckBox().setSelected(true);
        addPadding(node);
        mTabPane.getTabs().add(mDisruptorTab);
    }

    public void addDisruptorTab(Node node, String title) {
        mDisruptorTab.setText(title);
        addDisruptorTab(node);
    }

    public void addDateTab(Node node) {
        mDateTab.setContent(node);
        mDateTab.getTabCheckBox().setSelected(true);
        addPadding(node);
        mTabPane.getTabs().add(mDateTab);
    }

    public void addDateTab(Node node, String title) {
        mDateTab.setText(title);
        addDateTab(node);
    }

    public void addMeasTab(Node node) {
        mMeasTab.setContent(node);
        mMeasTab.getTabCheckBox().setSelected(true);
        addPadding(node);
        mTabPane.getTabs().add(mMeasTab);
    }

    public void addMeasTab(Node node, String title) {
        mMeasTab.setText(title);
        addMeasTab(node);
    }

    public TabPane getTabPane() {
        return mTabPane;
    }

    private void addPadding(Node node) {
        if (node instanceof Region r) {
            r.setPadding(FxHelper.getUIScaledInsets(16));

        }
    }
}
