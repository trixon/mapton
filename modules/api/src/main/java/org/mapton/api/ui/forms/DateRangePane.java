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

    public void setMinMaxDate(LocalDate minDate, LocalDate maxDate) {
        mDatePane.setMinMaxDate(minDate, maxDate);
    }

    private MenuItem createPresetMenuItem(String name, String code) {
        var menuItem = new MenuItem(name);
        var dateFormula = new MDateFormula(code);
        menuItem.setOnAction(actionEvent -> {
            mDatePane.getDateRangeSlider().setLowHighDate(dateFormula.getStartDate(), dateFormula.getEndDate());
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
    }

    private void populatePresets() {
        mPresetSplitMenuButton.getItems().clear();
        var latestMenu = new Menu("Senaste");
        var presentMenu = new Menu("Innevarande");
        var previousMenu = new Menu("Föregående");

        mPresetSplitMenuButton.getItems().setAll(
                createPresetMenuItem(Dict.Time.TODAY.toString(), "L,0,D"),
                new SeparatorMenuItem(),
                latestMenu,
                presentMenu,
                previousMenu
        );

        latestMenu.getItems().addAll(
                createPresetMenuItem("dygnet", "L,1,D"),
                createPresetMenuItem("veckan", "L,1,W"),
                createPresetMenuItem("två veckorna", "L,2,W"),
                createPresetMenuItem("månaden", "L,1,M"),
                createPresetMenuItem("tre månaderna", "L,3,M"),
                createPresetMenuItem("sex månaderna", "L,6,M"),
                createPresetMenuItem("året", "L,1,Y"),
                createPresetMenuItem("två åren", "L,2,Y"),
                createPresetMenuItem("tre åren", "L,3,Y"),
                createPresetMenuItem("fyra åren", "L,4,Y"),
                createPresetMenuItem("fem åren", "L,5,Y")
        );

        presentMenu.getItems().addAll(
                createPresetMenuItem("månad", "C,M"),
                createPresetMenuItem("år", "C,Y")
        );

        previousMenu.getItems().addAll(
                createPresetMenuItem("månad", "P,M"),
                createPresetMenuItem("år", "P,Y")
        );
    }

}
