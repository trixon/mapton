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

import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import org.mapton.api.Mapton;
import org.mapton.mapollage.Options;
import org.mapton.mapollage.api.Mapo;
import org.mapton.mapollage.api.MapoSettings;
import org.mapton.mapollage.api.MapoSettings.SplitBy;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TabPath extends TabBase {

    private final CheckBox mDrawPathCheckBox = new CheckBox(mBundle.getString("TabPath.drawPathCheckBox"));
    private final Options mOptions = Options.getInstance();
    private VBox mRoot;
    private final RadioButton mSplitByDayRadioButton = new RadioButton(Dict.Time.DAY.toString());
    private final RadioButton mSplitByHourRadioButton = new RadioButton(Dict.Time.HOUR.toString());
    private final RadioButton mSplitByMonthRadioButton = new RadioButton(Dict.Time.MONTH.toString());
    private final RadioButton mSplitByNoneRadioButton = new RadioButton(Dict.DO_NOT_SPLIT.toString());
    private final RadioButton mSplitByWeekRadioButton = new RadioButton(Dict.Time.WEEK.toString());
    private final RadioButton mSplitByYearRadioButton = new RadioButton(Dict.Time.YEAR.toString());
    private final ToggleGroup mToggleGroup = new ToggleGroup();
    private final Spinner<Double> mWidthSpinner = new Spinner(1.0, 10.0, 1.0, 0.1);

    public TabPath(Mapo mapo) {
        setText(Dict.Geometry.PATH.toString());
        mMapo = mapo;
        createUI();
        load();
        initListeners();
    }

    private void createUI() {
        mRoot = new VBox();
        VBox pathBox = new VBox();

        setScrollPaneContent(mRoot);
        Label widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        Label splitByLabel = new Label(Dict.SPLIT_BY.toString());

        mWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mWidthSpinner);

        mSplitByHourRadioButton.setToggleGroup(mToggleGroup);
        mSplitByDayRadioButton.setToggleGroup(mToggleGroup);
        mSplitByWeekRadioButton.setToggleGroup(mToggleGroup);
        mSplitByMonthRadioButton.setToggleGroup(mToggleGroup);
        mSplitByYearRadioButton.setToggleGroup(mToggleGroup);
        mSplitByNoneRadioButton.setToggleGroup(mToggleGroup);

        pathBox.getChildren().addAll(
                widthLabel,
                mWidthSpinner,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );
        pathBox.disableProperty().bind(mDrawPathCheckBox.selectedProperty().not());

        mRoot.getChildren().addAll(
                mDrawPathCheckBox,
                pathBox
        );

        FxHelper.setPadding(
                new Insets(8, 0, 0, 0),
                mDrawPathCheckBox,
                widthLabel,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );
    }

    private void initListeners() {
        EventHandler<ActionEvent> event = (evt) -> {
            MapoSettings settings = mMapo.getSettings();
            settings.setPlotPaths(mDrawPathCheckBox.isSelected());
            settings.setWidth(mWidthSpinner.getValue());

            SplitBy splitBy = null;
            Toggle t = mToggleGroup.getSelectedToggle();

            if (t == mSplitByHourRadioButton) {
                splitBy = SplitBy.HOUR;
            } else if (t == mSplitByDayRadioButton) {
                splitBy = SplitBy.DAY;
            } else if (t == mSplitByWeekRadioButton) {
                splitBy = SplitBy.WEEK;
            } else if (t == mSplitByMonthRadioButton) {
                splitBy = SplitBy.MONTH;
            } else if (t == mSplitByYearRadioButton) {
                splitBy = SplitBy.YEAR;
            } else if (t == mSplitByNoneRadioButton) {
                splitBy = SplitBy.NONE;
            }

            settings.setSplitBy(splitBy);

            mOptions.put(Options.KEY_SETTINGS, Mapo.getGson().toJson(settings));
            Mapton.getGlobalState().put(Mapo.KEY_SETTINGS_UPDATED, settings);
        };

        initListeners(mRoot, event);
        mWidthSpinner.valueProperty().addListener((ObservableValue<? extends Double> ov, Double t, Double t1) -> {
            event.handle(null);
        });
    }

    private void initListeners(Pane pane, EventHandler<ActionEvent> event) {
        for (Node node : pane.getChildren()) {
            if (node instanceof Pane) {
                initListeners((Pane) node, event);
            } else if (node instanceof ButtonBase) {
                ((ButtonBase) node).setOnAction(event);
            }
        }
    }

    private void load() {
        MapoSettings settings = mMapo.getSettings();

        mDrawPathCheckBox.setSelected(settings.isPlotPaths());
        mWidthSpinner.getValueFactory().setValue(settings.getWidth());

        RadioButton splitByRadioButton;

        switch (settings.getSplitBy()) {
            case HOUR:
                splitByRadioButton = mSplitByHourRadioButton;
                break;

            case DAY:
                splitByRadioButton = mSplitByDayRadioButton;
                break;

            case WEEK:
                splitByRadioButton = mSplitByWeekRadioButton;
                break;

            case MONTH:
                splitByRadioButton = mSplitByMonthRadioButton;
                break;

            case YEAR:
                splitByRadioButton = mSplitByYearRadioButton;
                break;

            case NONE:
                splitByRadioButton = mSplitByNoneRadioButton;
                break;

            default:
                throw new AssertionError();
        }

        splitByRadioButton.setSelected(true);
    }
}
