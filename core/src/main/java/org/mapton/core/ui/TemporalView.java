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
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;
import org.mapton.api.MTemporalManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.DateSelectionMode;
import se.trixon.almond.util.fx.control.DateRangePane;

/**
 *
 * @author Patrik Karlström
 */
public class TemporalView extends BorderPane {

    private DateRangePane mDateRangePane;
    private final MTemporalManager mManager = MTemporalManager.getInstance();
    private ToggleSwitch mToggleSwitch;

    public TemporalView() {
        createUI();
        initListeners();

        mToggleSwitch.setSelected(true);
    }

    private void createUI() {
        setPrefWidth(300);
        setPadding(new Insets(8));

        mDateRangePane = new DateRangePane();
        mToggleSwitch = new ToggleSwitch(Dict.INTERVAL.toString());

        HBox hBox = new HBox(mToggleSwitch);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        setBottom(hBox);
        setCenter(mDateRangePane);

        mDateRangePane.setMinMaxDate(LocalDate.parse("2019-06-03"), LocalDate.parse("2019-06-27"));

        mDateRangePane.getDateRangeSlider().minDateProperty().bind(mManager.minDateProperty());
        mDateRangePane.getDateRangeSlider().maxDateProperty().bind(mManager.maxDateProperty());
    }

    private void initListeners() {
        mDateRangePane.addFromDatePickerListener((ObservableValue<? extends Object> ov, Object t, Object t1) -> {
        });
        mDateRangePane.addToDatePickerListener((ObservableValue<? extends Object> ov, Object t, Object t1) -> {
        });

        mToggleSwitch.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mDateRangePane.setDateSelectionMode(t1 ? DateSelectionMode.INTERVAL : DateSelectionMode.POINT_IN_TIME);
        });
    }
}
