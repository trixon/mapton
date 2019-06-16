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
package org.mapton.core.ui;

import java.time.LocalDate;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.ToggleSwitch;
import se.trixon.almond.util.fx.control.DateRangePane;

/**
 *
 * @author Patrik Karlström
 */
public class TemporalView extends BorderPane {

    private DateRangePane mDateRangePane;
    private ToggleSwitch mToggleSwitch;

    public TemporalView() {
        createUI();
    }

    private void createUI() {
        setPrefWidth(300);
        setPadding(new Insets(8));

        mDateRangePane = new DateRangePane();
        mToggleSwitch = new ToggleSwitch("Mode");

        mDateRangePane.addFromDatePickerListener((ObservableValue<? extends Object> ov, Object t, Object t1) -> {
        });
        mDateRangePane.addToDatePickerListener((ObservableValue<? extends Object> ov, Object t, Object t1) -> {
        });
        mDateRangePane.setMinMaxDate(LocalDate.parse("2019-06-03"), LocalDate.parse("2019-06-27"));

        setTop(mDateRangePane);
        //setBottom(mToggleSwitch);
    }
}
