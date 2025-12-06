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
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SegmentedButton;
import org.mapton.api.MDateFormula;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.control.DatePane;

/**
 *
 * @author Patrik Karlström
 */
public class DateRangePane {

    private final StringProperty mDateFormulaProperty = new SimpleStringProperty();
    private final DatePane mDatePane = new DatePane();
    private final SplitMenuButton mPresetSplitMenuButton = new SplitMenuButton();
    private VBox mRoot;
    private final BooleanProperty mSelectedFromStartProperty = new SimpleBooleanProperty();
    private final BooleanProperty mSelectedToEndProperty = new SimpleBooleanProperty();

    public DateRangePane() {
        createUI();
        initListeners();
    }

    public StringProperty dateFormulaProperty() {
        return mDateFormulaProperty;
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
        mDateFormulaProperty.setValue("");
    }

    public BooleanProperty selectedFromStartProperty() {
        return mSelectedFromStartProperty;
    }

    public BooleanProperty selectedToEndProperty() {
        return mSelectedToEndProperty;
    }

    public void setMinMaxDate(LocalDate minDate, LocalDate maxDate) {
        mDatePane.setMinMaxDate(minDate, maxDate);
    }

    private MenuItem createPresetMenuItem(String name, String code, boolean before) {
        var menuItem = new MenuItem(name);
        var dateFormula = new MDateFormula(code, before);
        menuItem.setOnAction(actionEvent -> {
            var startDate = DateHelper.getMax(dateFormula.getStartDate(), mDatePane.getDateRangeSlider().getMinDate());
            var endDate = DateHelper.getMin(dateFormula.getEndDate(), mDatePane.getDateRangeSlider().getMaxDate());
            mDatePane.getDateRangeSlider().setLowHighDate(startDate, endDate);
            mDateFormulaProperty.set(code);
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
            dateRangeSlider.setLowHighDate(dateRangeSlider.getMinDate(), dateRangeSlider.getLowDate().minusDays(1));
            beforeToggleButton.setSelected(false);
        });

        afterToggleButton.setOnAction(actionEvent -> {
            dateRangeSlider.setLowHighDate(dateRangeSlider.getHighDate().plusDays(1), dateRangeSlider.getMaxDate());
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
            mDateFormulaProperty.set("");
            mDatePane.reset();
        });

        //TODO Reset formula on slider change?
//        ChangeListener<LocalDate> dateChengeListener = (p, o, n) -> {
//            mDateFormulaProperty.set("");
//        };
//        mDatePane.getDateRangeSlider().highDateProperty().addListener(dateChengeListener);
//        mDatePane.getDateRangeSlider().lowDateProperty().addListener(dateChengeListener);
        mDatePane.getDateRangeSlider().lowDateProperty().addListener((it, o, n) -> {
            mSelectedFromStartProperty.setValue(mDatePane.getDateRangeSlider().isSelectedFromStart());
            mSelectedToEndProperty.setValue(mDatePane.getDateRangeSlider().isSelectedToEnd());
        });
        mDatePane.getDateRangeSlider().highDateProperty().addListener((it, o, n) -> {
            mSelectedFromStartProperty.setValue(mDatePane.getDateRangeSlider().isSelectedFromStart());
            mSelectedToEndProperty.setValue(mDatePane.getDateRangeSlider().isSelectedToEnd());
        });
    }

    private void populatePresets() {
        mPresetSplitMenuButton.getItems().clear();
        var latestMenu = new Menu("Senaste");
        var presentMenu = new Menu("Innevarande");
        var previousMenu = new Menu("Föregående");
        var beforeLatestMenu = new Menu("Före senaste");
        var beforePresentMenu = new Menu("Före innevarande");
        var beforePreviousMenu = new Menu("Före föregående");

        mPresetSplitMenuButton.getItems().setAll(
                createPresetMenuItem(Dict.Time.TODAY.toString(), "L,0,D", false),
                new SeparatorMenuItem(),
                latestMenu,
                presentMenu,
                previousMenu,
                new SeparatorMenuItem(),
                beforeLatestMenu,
                beforePresentMenu,
                beforePreviousMenu
        );

        latestMenu.getItems().addAll(
                createPresetMenuItem("dygnet", "L,1,D", false),
                createPresetMenuItem("veckan", "L,1,W", false),
                createPresetMenuItem("två veckorna", "L,2,W", false),
                createPresetMenuItem("månaden", "L,1,M", false),
                createPresetMenuItem("tre månaderna", "L,3,M", false),
                createPresetMenuItem("sex månaderna", "L,6,M", false),
                createPresetMenuItem("året", "L,1,Y", false),
                createPresetMenuItem("två åren", "L,2,Y", false),
                createPresetMenuItem("tre åren", "L,3,Y", false),
                createPresetMenuItem("fyra åren", "L,4,Y", false),
                createPresetMenuItem("fem åren", "L,5,Y", false)
        );

        presentMenu.getItems().addAll(
                createPresetMenuItem("vecka", "C,W", false),
                createPresetMenuItem("månad", "C,M", false),
                createPresetMenuItem("år", "C,Y", false)
        );

        previousMenu.getItems().addAll(
                createPresetMenuItem("vecka", "P,W", false),
                createPresetMenuItem("månad", "P,M", false),
                createPresetMenuItem("år", "P,Y", false)
        );

        beforeLatestMenu.getItems().addAll(
                createPresetMenuItem("dygnet", "L,1,D", true),
                createPresetMenuItem("veckan", "L,1,W", true),
                createPresetMenuItem("två veckorna", "L,2,W", true),
                createPresetMenuItem("månaden", "L,1,M", true),
                createPresetMenuItem("tre månaderna", "L,3,M", true),
                createPresetMenuItem("sex månaderna", "L,6,M", true),
                createPresetMenuItem("året", "L,1,Y", true),
                createPresetMenuItem("två åren", "L,2,Y", true),
                createPresetMenuItem("tre åren", "L,3,Y", true),
                createPresetMenuItem("fyra åren", "L,4,Y", true),
                createPresetMenuItem("fem åren", "L,5,Y", true)
        );

        beforePresentMenu.getItems().addAll(
                createPresetMenuItem("vecka", "C,W", true),
                createPresetMenuItem("månad", "C,M", true),
                createPresetMenuItem("år", "C,Y", true)
        );

        beforePreviousMenu.getItems().addAll(
                createPresetMenuItem("vecka", "P,W", true),
                createPresetMenuItem("månad", "P,M", true),
                createPresetMenuItem("år", "P,Y", true)
        );
    }

}
