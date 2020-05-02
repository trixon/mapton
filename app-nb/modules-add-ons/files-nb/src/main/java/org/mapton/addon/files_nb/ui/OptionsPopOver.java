/* 
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.addon.files_nb.ui;

import java.util.ResourceBundle;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.controlsfx.control.PopOver;
import org.mapton.addon.files_nb.Options;
import org.mapton.addon.files_nb.api.Mapo;
import org.mapton.addon.files_nb.api.MapoSettings;
import org.mapton.addon.files_nb.api.MapoSettings.SplitBy;
import org.mapton.api.Mapton;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class OptionsPopOver extends PopOver {

    private final ResourceBundle mBundle = SystemHelper.getBundle(OptionsPopOver.class, "Bundle");
    private final CheckBox mDrawGapCheckBox = new CheckBox(mBundle.getString("TabPath.drawGapCheckBox"));
    private final CheckBox mDrawTrackCheckBox = new CheckBox(mBundle.getString("TabPath.drawTrackCheckBox"));
    private final ColorPicker mGapColorPicker = new ColorPicker();
    private final Mapo mMapo = Mapo.getInstance();
    private final Options mOptions = Options.getInstance();
    private VBox mRoot;
    private final RadioButton mSplitByDayRadioButton = new RadioButton(Dict.Time.DAY.toString());
    private final RadioButton mSplitByHourRadioButton = new RadioButton(Dict.Time.HOUR.toString());
    private final RadioButton mSplitByMonthRadioButton = new RadioButton(Dict.Time.MONTH.toString());
    private final RadioButton mSplitByNoneRadioButton = new RadioButton(Dict.DO_NOT_SPLIT.toString());
    private final RadioButton mSplitByWeekRadioButton = new RadioButton(Dict.Time.WEEK.toString());
    private final RadioButton mSplitByYearRadioButton = new RadioButton(Dict.Time.YEAR.toString());
    private final ToggleGroup mToggleGroup = new ToggleGroup();
    private final ColorPicker mTrackColorPicker = new ColorPicker();
    private final Spinner<Double> mWidthSpinner = new Spinner<>(1.0, 10.0, 1.0, 0.1);

    public OptionsPopOver() {
        setTitle(Dict.OPTIONS.toString());
        setArrowLocation(PopOver.ArrowLocation.TOP_RIGHT);
        setHeaderAlwaysVisible(true);
        setCloseButtonEnabled(false);
        setDetachable(false);
        setAnimated(true);
        setContentNode(createUI());

        load();
        initListeners();
    }

    private Node createUI() {
        mRoot = new VBox();
        VBox trackBox = new VBox();

        Label widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        Label splitByLabel = new Label(Dict.SPLIT_BY.toString());
        Label gapColorLabel = new Label(mBundle.getString("TabPath.colorGap"));
        Label trackColorLabel = new Label(mBundle.getString("TabPath.colorTrack"));

        mWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mWidthSpinner);

        mSplitByHourRadioButton.setToggleGroup(mToggleGroup);
        mSplitByDayRadioButton.setToggleGroup(mToggleGroup);
        mSplitByWeekRadioButton.setToggleGroup(mToggleGroup);
        mSplitByMonthRadioButton.setToggleGroup(mToggleGroup);
        mSplitByYearRadioButton.setToggleGroup(mToggleGroup);
        mSplitByNoneRadioButton.setToggleGroup(mToggleGroup);

        trackBox.getChildren().addAll(
                widthLabel,
                mWidthSpinner,
                trackColorLabel,
                mTrackColorPicker,
                gapColorLabel,
                mGapColorPicker,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );

        trackBox.disableProperty().bind(mDrawTrackCheckBox.selectedProperty().or(mDrawGapCheckBox.selectedProperty()).not());

        mRoot.getChildren().addAll(
                mDrawTrackCheckBox,
                mDrawGapCheckBox,
                trackBox
        );

        FxHelper.setPadding(
                new Insets(8, 0, 0, 0),
                mDrawTrackCheckBox,
                mDrawGapCheckBox,
                widthLabel,
                trackColorLabel,
                gapColorLabel,
                splitByLabel,
                mSplitByHourRadioButton,
                mSplitByDayRadioButton,
                mSplitByWeekRadioButton,
                mSplitByMonthRadioButton,
                mSplitByYearRadioButton,
                mSplitByNoneRadioButton
        );

        mRoot.setPadding(new Insets(8));

        return mRoot;
    }

    private void initListeners() {
        EventHandler<ActionEvent> event = (evt) -> {
            save();
        };

        initListeners(mRoot, event);
        mWidthSpinner.valueProperty().addListener((ObservableValue<? extends Double> ov, Double t, Double t1) -> {
            event.handle(null);
        });
    }

    @SuppressWarnings("unchecked")
    private void initListeners(Pane pane, EventHandler<ActionEvent> event) {
        for (Node node : pane.getChildren()) {
            if (node instanceof Pane) {
                initListeners((Pane) node, event);
            } else if (node instanceof ButtonBase) {
                ((ButtonBase) node).setOnAction(event);
            } else if (node instanceof ComboBoxBase) {
                ((ComboBoxBase) node).setOnAction(event);
            }
        }
    }

    private void load() {
        MapoSettings settings = mMapo.getSettings();

        mDrawTrackCheckBox.setSelected(settings.isPlotTracks());
        mDrawGapCheckBox.setSelected(settings.isPlotGaps());
        mWidthSpinner.getValueFactory().setValue(settings.getWidth());

        Color colorTrack = Color.RED;
        try {
            colorTrack = FxHelper.colorFromHexRGBA(settings.getColorTrack());
        } catch (Exception e) {
        }
        mTrackColorPicker.setValue(colorTrack);

        Color colorGap = Color.BLACK;
        try {
            colorGap = FxHelper.colorFromHexRGBA(settings.getColorGap());
        } catch (Exception e) {
        }
        mGapColorPicker.setValue(colorGap);

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

    private void save() {
        MapoSettings settings = mMapo.getSettings();
        settings.setPlotTracks(mDrawTrackCheckBox.isSelected());
        settings.setPlotGaps(mDrawGapCheckBox.isSelected());
        settings.setWidth(mWidthSpinner.getValue());
        settings.setColorGap(FxHelper.colorToHexRGB(mGapColorPicker.getValue()));
        settings.setColorTrack(FxHelper.colorToHexRGB(mTrackColorPicker.getValue()));

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
    }
}
