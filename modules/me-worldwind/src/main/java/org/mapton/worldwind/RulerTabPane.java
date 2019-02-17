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

import gov.nasa.worldwind.WorldWindowGLDrawable;
import gov.nasa.worldwind.util.measure.MeasureTool;
import gov.nasa.worldwind.util.measure.MeasureToolController;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

/**
 *
 * @author Patrik Karlström
 */
public class RulerTabPane extends TabPane {

    private WorldWindowPanel mMap;
    private int mTabCounter = 0;

    public static RulerTabPane getInstance() {
        return Holder.INSTANCE;
    }

    private RulerTabPane() {
        createUI();
        initListeners();
    }

    void refresh(WorldWindowPanel map) {
        mMap = map;
        addTab();

    }

    private void addTab() {
        WorldWindowGLDrawable wwd = mMap.getWwd();
        MeasureTool measureTool = new MeasureTool(wwd);
        measureTool.setController(new MeasureToolController());

        RulerTab rulerTab = new RulerTab(Integer.toString(++mTabCounter), wwd, measureTool);
        Platform.runLater(() -> {
            getTabs().add(rulerTab);
            getSelectionModel().select(rulerTab);
        });
    }

    private void createUI() {
        Tab plusTab = new Tab("+");
        plusTab.setClosable(false);
        getTabs().add(plusTab);
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
