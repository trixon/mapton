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
package org.mapton.mapollage.ui;

import java.time.LocalDate;
import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.DateRangeSlider;

/**
 *
 * @author Patrik Karlström
 */
public class DateSelectionPane extends GridPane {

    private DatePicker mFromDatePicker;
    private DatePicker mToDatePicker;
    private DateRangeSlider mDateRangeSlider;

    public DateSelectionPane() {
        createUI();
    }

    private void createUI() {
        mDateRangeSlider = new DateRangeSlider();
        mDateRangeSlider.prefWidthProperty().bind(widthProperty());
        GridPane.setColumnSpan(mDateRangeSlider, GridPane.REMAINING);

        mFromDatePicker = new DatePicker();
        mFromDatePicker.setValue(LocalDate.now().minusDays(7));
        mFromDatePicker.setEditable(false);
        GridPane.setFillWidth(mFromDatePicker, true);
        GridPane.setHgrow(mFromDatePicker, Priority.ALWAYS);

        mToDatePicker = new DatePicker();
        mToDatePicker.setValue(LocalDate.now());
        mToDatePicker.setEditable(false);
        GridPane.setFillWidth(mToDatePicker, true);
        GridPane.setHgrow(mToDatePicker, Priority.ALWAYS);

        Label fromLabel = new Label(Dict.FROM.toString());
        Label toLabel = new Label(Dict.TO.toString());
        FxHelper.setPadding(new Insets(8, 0, 0, 0), fromLabel, toLabel);
        FxHelper.setMargin(new Insets(0, 0, 0, 8), toLabel, mToDatePicker);

        setPadding(new Insets(8));
        addRow(0, mDateRangeSlider);
        addRow(1, fromLabel, toLabel);
        addRow(2, mFromDatePicker, mToDatePicker);
    }
}
