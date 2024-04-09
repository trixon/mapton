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
package org.mapton.api.ui.forms;

import com.dlsc.gemsfx.Spacer;
import java.time.LocalDate;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.DatePane;
import se.trixon.almond.util.fx.control.TemporalPreset;

/**
 *
 * @author Patrik Karlström
 */
public class DateRangePane {

    private final DatePane mDatePane = new DatePane();
    private final SplitMenuButton mPresetSplitMenuButton = new SplitMenuButton();

    private VBox mRoot;

    public DateRangePane() {
        createUI();
        initListeners();
    }

    public DatePane getDatePane() {
        return mDatePane;
    }

    public VBox getRoot() {
        return mRoot;
    }

    public SimpleObjectProperty<LocalDate> highDateProperty() {
        return mDatePane.getDateRangeSlider().highDateProperty();
    }

    public SimpleStringProperty highStringProperty() {
        return mDatePane.getDateRangeSlider().highStringProperty();
    }

    public SimpleObjectProperty<LocalDate> lowDateProperty() {
        return mDatePane.getDateRangeSlider().lowDateProperty();
    }

    public SimpleStringProperty lowStringProperty() {
        return mDatePane.getDateRangeSlider().lowStringProperty();
    }

    public void reset() {
        mDatePane.reset();

    }

    public void setMinMaxDate(LocalDate minDate, LocalDate maxDate) {
        mDatePane.setMinMaxDate(minDate, maxDate);
    }

    private MenuItem createPresetMenuItem(String name, LocalDate lowDate, LocalDate highDate) {
        var preset = new TemporalPreset(name, lowDate, highDate);
        var menuItem = new MenuItem(preset.name());
        menuItem.setOnAction(actionEvent -> {
            mDatePane.getDateRangeSlider().setLowHighDate(preset.lowDate(), preset.highDate());
        });
        return menuItem;
    }

    private void createUI() {
        var beforeToggleButton = new ToggleButton("⇷");
        var afterToggleButton = new ToggleButton("⇸");
        var startToggleButton = new ToggleButton("⇤");
        var endToggleButton = new ToggleButton("⇥");
        startToggleButton.setTooltip(new Tooltip("Från början"));
        beforeToggleButton.setTooltip(new Tooltip("Perioden före"));
        afterToggleButton.setTooltip(new Tooltip("Perioden efter"));
        endToggleButton.setTooltip(new Tooltip("Till slutet"));

        var dateRangeSlider = mDatePane.getDateRangeSlider();

        beforeToggleButton.setOnAction(actionEvent -> {
            dateRangeSlider.setLowHighDate(dateRangeSlider.getMinDate(), dateRangeSlider.getLowDate());
            beforeToggleButton.setSelected(false);
        });

        afterToggleButton.setOnAction(actionEvent -> {
            dateRangeSlider.setLowHighDate(dateRangeSlider.getHighDate(), dateRangeSlider.getMaxDate());
            afterToggleButton.setSelected(false);
        });

        startToggleButton.setOnAction(actionEvent -> {
            dateRangeSlider.setLowDate(dateRangeSlider.getMinDate());
            startToggleButton.setSelected(false);
        });

        endToggleButton.setOnAction(actionEvent -> {
            dateRangeSlider.setHighDate(dateRangeSlider.getMaxDate());
            endToggleButton.setSelected(false);
        });

        var segmentedButton = new SegmentedButton(
                startToggleButton,
                beforeToggleButton,
                afterToggleButton,
                endToggleButton
        );

        mPresetSplitMenuButton.setText(Dict.RESET.toString());

        mRoot = new VBox(mDatePane, new HBox(segmentedButton, new Spacer(), mPresetSplitMenuButton));
    }

    private void initListeners() {
        mPresetSplitMenuButton.setOnShowing(event -> {
            populatePresets();
        });

        mPresetSplitMenuButton.setOnAction(ae -> {
            mDatePane.reset();
        });
    }

    private void populatePresets() {
        mPresetSplitMenuButton.getItems().clear();
        var now = LocalDate.now();
        var latestMenu = new Menu("Senaste");
        var presentMenu = new Menu("Innevarande");
        var previousMenu = new Menu("Föregående");

        mPresetSplitMenuButton.getItems().setAll(
                createPresetMenuItem(Dict.Time.TODAY.toString(), now, now),
                new SeparatorMenuItem(),
                latestMenu,
                presentMenu,
                previousMenu
        );

        latestMenu.getItems().addAll(
                createPresetMenuItem("dygnet", now.minusDays(1), now),
                createPresetMenuItem("veckan", now.minusWeeks(1), now),
                createPresetMenuItem("två veckorna", now.minusWeeks(2), now),
                createPresetMenuItem("månaden", now.minusMonths(1), now),
                createPresetMenuItem("tre månaderna", now.minusMonths(3), now),
                createPresetMenuItem("sex månaderna", now.minusMonths(6), now),
                createPresetMenuItem("året", now.minusYears(1), now),
                createPresetMenuItem("två åren", now.minusYears(2), now),
                createPresetMenuItem("tre åren", now.minusYears(3), now),
                createPresetMenuItem("fyra åren", now.minusYears(4), now),
                createPresetMenuItem("fem åren", now.minusYears(5), now)
        );

        presentMenu.getItems().addAll(
                createPresetMenuItem("månad", now.withDayOfMonth(1), now),
                createPresetMenuItem("år", now.withDayOfYear(1), now)
        );

        var prevMonthStart = now.minusMonths(1).withDayOfMonth(1);
        var prevMonthEnd = prevMonthStart.withDayOfMonth(prevMonthStart.lengthOfMonth());
        var prevYearStart = now.minusYears(1).withDayOfYear(1);
        var prevYearEnd = prevYearStart.withDayOfYear(prevYearStart.lengthOfYear());
        previousMenu.getItems().addAll(
                createPresetMenuItem("månad", prevMonthStart, prevMonthEnd),
                createPresetMenuItem("år", prevYearStart, prevYearEnd)
        );
    }

}
