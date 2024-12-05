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
import javafx.scene.control.Separator;
import javafx.scene.control.TabPane;
import org.controlsfx.control.action.ActionUtils;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTabbedFilterPopOver extends BaseFilterPopOver {

    private final TabPane mTabPane = new TabPane();

    public BaseTabbedFilterPopOver() {
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        setContentNode(mTabPane);
    }

    public TabPane getTabPane() {
        return mTabPane;
    }

    public void populateToolBar(Node... nodes) {
        getToolBar().getItems().add(new Separator());
        addToToolBar("mc", ActionUtils.ActionTextBehavior.SHOW);
        addToToolBar("mr", ActionUtils.ActionTextBehavior.SHOW);
        addToToolBar("mm", ActionUtils.ActionTextBehavior.SHOW);
        addToToolBar("mp", ActionUtils.ActionTextBehavior.SHOW);
        getToolBar().getItems().add(new Separator());
        addToToolBar("copyNames", ActionUtils.ActionTextBehavior.HIDE);
        addToToolBar("paste", ActionUtils.ActionTextBehavior.HIDE);

        if (nodes != null) {
            getToolBar().getItems().addAll(nodes);
        }
    }

}
