/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_core.ui;

import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.mapton.butterfly_core.api.BMeasurementReport;

/**
 *
 * @author Patrik Karlström
 */
public class MeasurementsPane extends BorderPane {

    private final TabPane mTabPane = new TabPane();
    private final Label mTitleLabel = new Label();

    public MeasurementsPane() {
        createUI();
    }

    void load(BMeasurementReport measurementReport) {
        mTabPane.getTabs().clear();
        var p = measurementReport.getPoint();
        if (measurementReport.getNumOfMeasurements() == 0) {
            mTitleLabel.setText(p.getName());
        } else {
            var title = "%s\t\t%s - %s\t%d/%d".formatted(p.getName(),
                    measurementReport.getFirstDate(),
                    measurementReport.getLastDate(),
                    measurementReport.getNumOfReplacements(),
                    measurementReport.getNumOfMeasurements()
            );
            mTitleLabel.setText(title);
            if (measurementReport.getTabs().size() == 1) {
                setCenter(measurementReport.getTabs().getFirst().getContent());
            } else {
                for (var tab : measurementReport.getTabs()) {
                    mTabPane.getTabs().add(tab);
                }
                setCenter(mTabPane);
            }
        }
    }

    private void createUI() {
        mTabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        var topBox = new HBox(mTitleLabel);
        setTop(topBox);
    }

}
