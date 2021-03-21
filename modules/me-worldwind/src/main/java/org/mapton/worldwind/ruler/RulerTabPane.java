/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.worldwind.ruler;

import gov.nasa.worldwind.WorldWindow;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTabPane extends TabPane {

    private int mTabCounter = 0;
    private WorldWindow mWorldWindow;

    public static RulerTabPane getInstance() {
        return Holder.INSTANCE;
    }

    private RulerTabPane() {
        createUI();
        initListeners();
    }

    public void refresh(WorldWindow worldWindow) {
        mWorldWindow = worldWindow;
        addTab();
    }

    private void addTab() {
        Platform.runLater(() -> {
            RulerTab rulerTab = new RulerTab(Integer.toString(++mTabCounter), mWorldWindow);

            getTabs().add(rulerTab);
            getSelectionModel().select(rulerTab);
        });
    }

    private void createUI() {
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        getTabs().add(plusTab);
        setPrefWidth(FxHelper.getUIScaled(300));
    }

    private void initListeners() {
        getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Tab> ov, Tab oldTab, Tab newTab) -> {
            if (oldTab instanceof RulerTab) {
                ((RulerTab) oldTab).getMeasureTool().setArmed(false);
            }

            if (getSelectionModel().getSelectedIndex() == 0) {
                addTab();
            }
        });
    }

    private static class Holder {

        private static final RulerTabPane INSTANCE = new RulerTabPane();
    }

}
