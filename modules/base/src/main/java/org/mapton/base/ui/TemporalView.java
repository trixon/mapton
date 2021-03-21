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
package org.mapton.base.ui;

import java.time.LocalDate;
import java.util.Locale;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.controlsfx.control.ToggleSwitch;
import org.mapton.api.MTemporalManager;
import se.trixon.almond.util.DateHelper;
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
    private SplitMenuButton mPresetSplitMenuButton;
    private final StringProperty mTitleProperty = new SimpleStringProperty();
    private ToggleSwitch mToggleSwitch;

    public TemporalView() {
        createUI();
        populatePresets();
        initListeners();

        mToggleSwitch.setSelected(true);
        setDisable(true);
        mDatePane.setMinMaxDate(mManager.getMinDate(), mManager.getMaxDate());

        mManager.refresh();
    }

    public StringProperty titleProperty() {
        return mTitleProperty;
    }

    private void createUI() {
        setPrefWidth(FxHelper.getUIScaled(300));
        setPadding(FxHelper.getUIScaledInsets(8));

        mDatePane = new DatePane();
        mPresetSplitMenuButton = new SplitMenuButton();
        mPresetSplitMenuButton.setText(Dict.RESET.toString());

        mToggleSwitch = new ToggleSwitch(Dict.INTERVAL.toString());
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        HBox hBox = new HBox(
                mPresetSplitMenuButton,
                spacer,
                mToggleSwitch
        );

        hBox.setAlignment(Pos.CENTER);
        setBottom(hBox);
        setCenter(mDatePane);

        mPresetSplitMenuButton.disableProperty().bind(mToggleSwitch.selectedProperty().not());
    }

    private LocalDate getProperMax(LocalDate localDate) {
        return DateHelper.getMin(localDate, mManager.getMaxDate());
    }

    private LocalDate getProperMin(LocalDate localDate) {
        return DateHelper.getMax(localDate, mManager.getMinDate());
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

        mPresetSplitMenuButton.setOnAction(ae -> {
            mToggleSwitch.setSelected(true);
            mManager.lowDateProperty().set(mManager.getMinDate());
            mManager.highDateProperty().set(mManager.getMaxDate());
        });
    }

    private void populatePreset(TemporalPreset temporalPreset) {
        MenuItem menuItem = new MenuItem(temporalPreset.name);
        menuItem.setOnAction(ae -> {
            mManager.setLowDate(getProperMin(temporalPreset.lowDate));
            mManager.setHighDate(getProperMax(temporalPreset.highDate));
        });
        mPresetSplitMenuButton.getItems().add(menuItem);
    }

    private void populatePresets() {
        final LocalDate now = LocalDate.now();

        populatePreset(new TemporalPreset(String.format("%s + 1 %s", Dict.Time.TODAY.toString(), Dict.Time.YEAR.toString().toLowerCase()), now, now.plusYears(1)));
        populatePreset(new TemporalPreset(String.format("%s + 1 %s", Dict.Time.TODAY.toString(), Dict.Time.MONTH.toString().toLowerCase()), now, now.plusMonths(1)));
        populatePreset(new TemporalPreset(String.format("%s + 1 %s", Dict.Time.TODAY.toString(), Dict.Time.WEEK.toString().toLowerCase()), now, now.plusWeeks(1)));
        populatePreset(new TemporalPreset(String.format("%s + 1 %s", Dict.Time.TODAY.toString(), Dict.Time.DAY.toString().toLowerCase()), now, now.plusDays(1)));
        mPresetSplitMenuButton.getItems().add(new SeparatorMenuItem());
        populatePreset(new TemporalPreset(Dict.Time.TODAY.toString(), now, now));
        mPresetSplitMenuButton.getItems().add(new SeparatorMenuItem());
        populatePreset(new TemporalPreset(String.format("%s - 1 %s", Dict.Time.TODAY.toString(), Dict.Time.DAY.toString().toLowerCase()), now.minusDays(1), now));
        populatePreset(new TemporalPreset(String.format("%s - 1 %s", Dict.Time.TODAY.toString(), Dict.Time.WEEK.toString().toLowerCase()), now.minusWeeks(1), now));
        populatePreset(new TemporalPreset(String.format("%s - 1 %s", Dict.Time.TODAY.toString(), Dict.Time.MONTH.toString().toLowerCase()), now.minusMonths(1), now));
        populatePreset(new TemporalPreset(String.format("%s - 1 %s", Dict.Time.TODAY.toString(), Dict.Time.YEAR.toString().toLowerCase()), now.minusYears(1), now));
//        mPresetSplitMenuButton.getItems().add(new SeparatorMenuItem());
//        int year = now.getYear();
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.NEXT.toString(), Dict.Time.YEAR.toString().toLowerCase()),
//                LocalDate.of(year + 1, 1, 1),
//                LocalDate.of(year + 1, 12, 31)
//        ));
//        YearMonth nextYM;
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.NEXT.toString(), Dict.Time.MONTH.toString().toLowerCase()), now, now));
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.NEXT.toString(), Dict.Time.WEEK.toString().toLowerCase()), now, now));
//        mPresetSplitMenuButton.getItems().add(new SeparatorMenuItem());
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.Time.CURRENT.toString(), Dict.Time.WEEK.toString().toLowerCase()), now, now));
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.Time.CURRENT.toString(), Dict.Time.MONTH.toString().toLowerCase()), now, now));
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.Time.CURRENT.toString(), Dict.Time.YEAR.toString().toLowerCase()),
//                LocalDate.of(year, 1, 1),
//                LocalDate.of(year, 12, 31)
//        ));
//        mPresetSplitMenuButton.getItems().add(new SeparatorMenuItem());
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.PREVIOUS.toString(), Dict.Time.WEEK.toString().toLowerCase()), now, now));
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.PREVIOUS.toString(), Dict.Time.MONTH.toString().toLowerCase()), now, now));
//        populatePreset(new TemporalPreset(String.format("%s %s", Dict.PREVIOUS.toString(), Dict.Time.YEAR.toString().toLowerCase()),
//                LocalDate.of(year - 1, 1, 1),
//                LocalDate.of(year - 1, 12, 31)
//        ));
    }

    private void refreshTitle() {
        FxHelper.runLater(() -> {
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
        });
    }

    class TemporalPreset {

        private final LocalDate highDate;
        private final LocalDate lowDate;
        private final String name;

        public TemporalPreset(String name, LocalDate lowDate, LocalDate highDate) {
            this.name = name;
            this.lowDate = lowDate;
            this.highDate = highDate;
        }

        @Override
        public String toString() {
            return this.name;
        }
    }
}
