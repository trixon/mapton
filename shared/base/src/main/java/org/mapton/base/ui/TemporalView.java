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
package org.mapton.base.ui;

import java.time.LocalDate;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import org.controlsfx.control.ToggleSwitch;
import org.mapton.api.MTemporalManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.DatePane;
import se.trixon.almond.util.fx.control.DateSelectionMode;

/**
 *
 * @author Patrik Karlström
 */
public class TemporalView extends BorderPane {

    private DatePane mDatePane;
    private final MTemporalManager mManager = MTemporalManager.getInstance();
    private final StringProperty mTitleProperty = new SimpleStringProperty();
    private ToggleSwitch mToggleSwitch;

    public TemporalView() {
        createUI();
        initListeners();

        mToggleSwitch.setSelected(true);
        mManager.refresh();
    }

    public StringProperty titleProperty() {
        return mTitleProperty;
    }

    private void createUI() {
        setPrefWidth(FxHelper.getUIScaled(300));
        setPadding(FxHelper.getUIScaledInsets(8));

        mDatePane = new DatePane();
        mToggleSwitch = new ToggleSwitch(Dict.INTERVAL.toString());

        HBox hBox = new HBox(mToggleSwitch);
        hBox.setAlignment(Pos.CENTER_RIGHT);

        setBottom(hBox);
        setCenter(mDatePane);
    }

    private void initListeners() {
        ChangeListener<LocalDate> minMaxChangeListener = (ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) -> {
            mDatePane.setMinMaxDate(mManager.getMinDate(), mManager.getMaxDate());
            try {
                setDisable(mManager.getMinDate().equals(LocalDate.of(1900, 1, 1)) && mManager.getMaxDate().equals(LocalDate.of(2099, 12, 31)));
            } catch (Exception e) {
                setDisable(true);
            }
            refreshTitle();
        };

        mManager.minDateProperty().addListener(minMaxChangeListener);
        mManager.maxDateProperty().addListener(minMaxChangeListener);

        ChangeListener<LocalDate> rangeChangeListener = (ObservableValue<? extends LocalDate> ov, LocalDate t, LocalDate t1) -> {
            refreshTitle();
        };

        mManager.lowDateProperty().addListener(rangeChangeListener);
        mManager.highDateProperty().addListener(rangeChangeListener);

        mManager.lowDateProperty().bindBidirectional(mDatePane.getFromDatePicker().valueProperty());
        mManager.highDateProperty().bindBidirectional(mDatePane.getToDatePicker().valueProperty());

        mToggleSwitch.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
            mDatePane.setDateSelectionMode(t1 ? DateSelectionMode.INTERVAL : DateSelectionMode.POINT_IN_TIME);
            refreshTitle();
        });
    }

    private void refreshTitle() {
        if (isDisabled()) {
            mTitleProperty.set(Dict.DATE.toString());
        } else {
            String text = null;
            switch (mDatePane.getDateSelectionMode()) {
                case INTERVAL:
                    text = String.format("%s %s %s",
                            mManager.getLowDate(),
                            Dict.TO.toString().toLowerCase(Locale.getDefault()),
                            mManager.getHighDate()
                    );
                    break;

                case POINT_IN_TIME:
                    text = mManager.getHighDate().toString();
                    break;
            }

            mTitleProperty.set(text);
        }
    }
}
