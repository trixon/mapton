/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.addon.photos;

import java.util.List;
import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.addon.photos.api.Mapo;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.MOptionsView;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.Dict.Time;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class PhotosOptionsView extends MOptionsView {

    private final ResourceBundle mBundle = NbBundle.getBundle(PhotosOptionsView.class);
    private final CheckBox mDrawGapCheckBox = new CheckBox(mBundle.getString("drawGapCheckBox"));
    private final CheckBox mDrawTrackCheckBox = new CheckBox(mBundle.getString("drawTrackCheckBox"));
    private final ColorPicker mGapColorPicker = new ColorPicker();
    private final Mapo mMapo = Mapo.getInstance();
    private final PhotosOptions mOptions = PhotosOptions.getInstance();
    private final MPresetPopOver mPresetPopOver;
    private final ComboBox<String> mSplitByComboBox = new ComboBox();
    private final ColorPicker mTrackColorPicker = new ColorPicker();
    private final Spinner<Double> mWidthSpinner = new Spinner<>(1.0, 10.0, 1.0, 0.1);

    public PhotosOptionsView() {
        mPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "photos");
        createUI();

        initSession();
    }

    private void createUI() {
        var restoreDefaultsRunnable = (Runnable) () -> {
            if (mPresetPopOver.restoreDefaultIfExists()) {
                //
            } else {
                mOptions.reset();
            }
        };
        var actions = List.of(
                getRestoreDefaultsAction(restoreDefaultsRunnable),
                ActionUtils.ACTION_SPAN,
                mPresetPopOver.getAction()
        );
        createToolbar(actions);

        mSplitByComboBox.getItems().setAll(
                Dict.DO_NOT_SPLIT.toString(),
                Time.DAY.toString(),
                Time.HOUR.toString(),
                Time.MONTH.toString(),
                Time.WEEK.toString(),
                Time.YEAR.toString()
        );
        mSplitByComboBox.getSelectionModel().selectFirst();

        var gp = createGridPane();
        var widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        var splitByLabel = new Label(Dict.SPLIT_BY.toString());
        var gapColorLabel = new Label(mBundle.getString("colorGap"));
        var trackColorLabel = new Label(mBundle.getString("colorTrack"));

        int row = 0;
        gp.addRow(row++, mDrawTrackCheckBox, mDrawGapCheckBox);

        var gp2 = createGridPane();
        gp2.setPadding(Insets.EMPTY);
        gp.add(gp2, 0, row++, GridPane.REMAINING, 1);

        row = 0;
        gp2.addRow(row++, trackColorLabel, gapColorLabel);
        gp2.addRow(row++, mTrackColorPicker, mGapColorPicker);
        gp2.addRow(row++, widthLabel, splitByLabel);
        gp2.addRow(row++, mWidthSpinner, mSplitByComboBox);

        mWidthSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mWidthSpinner);

        FxHelper.bindCheckBoxEnablement(mDrawTrackCheckBox, trackColorLabel, mTrackColorPicker);
        FxHelper.bindCheckBoxEnablement(mDrawGapCheckBox, gapColorLabel, mGapColorPicker);
        gp2.disableProperty().bind(mDrawTrackCheckBox.selectedProperty().or(mDrawGapCheckBox.selectedProperty()).not());
        setLabelPadding(
                //                mDrawTrackCheckBox,
                //                mDrawGapCheckBox,
                trackColorLabel,
                gapColorLabel,
                widthLabel,
                splitByLabel
        );

        FxHelper.autoSizeRegionHorizontal(
                mTrackColorPicker,
                mGapColorPicker,
                mSplitByComboBox
        );

        FxHelper.autoSizeColumn(gp, 2);
        FxHelper.autoSizeColumn(gp2, 2);

        setCenter(gp);
    }

    private void initSession() {
    }

//    private void load() {
//        var settings = mMapo.getSettings();
//
//        mDrawTrackCheckBox.setSelected(settings.isPlotTracks());
//        mDrawGapCheckBox.setSelected(settings.isPlotGaps());
//        mWidthSpinner.getValueFactory().setValue(settings.getWidth());
//
//        var colorTrack = Color.RED;
//        try {
//            colorTrack = FxHelper.colorFromHexRGBA(settings.getColorTrack());
//        } catch (Exception e) {
//        }
//        mTrackColorPicker.setValue(colorTrack);
//
//        var colorGap = Color.BLACK;
//        try {
//            colorGap = FxHelper.colorFromHexRGBA(settings.getColorGap());
//        } catch (Exception e) {
//        }
//        mGapColorPicker.setValue(colorGap);
//
//        RadioButton splitByRadioButton;
//
//        switch (settings.getSplitBy()) {
//            case HOUR ->
//                splitByRadioButton = mSplitByHourRadioButton;
//
//            case DAY ->
//                splitByRadioButton = mSplitByDayRadioButton;
//
//            case WEEK ->
//                splitByRadioButton = mSplitByWeekRadioButton;
//
//            case MONTH ->
//                splitByRadioButton = mSplitByMonthRadioButton;
//
//            case YEAR ->
//                splitByRadioButton = mSplitByYearRadioButton;
//
//            case NONE ->
//                splitByRadioButton = mSplitByNoneRadioButton;
//
//            default ->
//                throw new AssertionError();
//        }
//
//        splitByRadioButton.setSelected(true);
//    }
//
//    private void save() {
//        var settings = mMapo.getSettings();
//        settings.setPlotTracks(mDrawTrackCheckBox.isSelected());
//        settings.setPlotGaps(mDrawGapCheckBox.isSelected());
//        settings.setWidth(mWidthSpinner.getValue());
//        settings.setColorGap(FxHelper.colorToHexRGB(mGapColorPicker.getValue()));
//        settings.setColorTrack(FxHelper.colorToHexRGB(mTrackColorPicker.getValue()));
//
//        SplitBy splitBy = null;
//        var toggle = mToggleGroup.getSelectedToggle();
//
//        if (toggle == mSplitByHourRadioButton) {
//            splitBy = SplitBy.HOUR;
//        } else if (toggle == mSplitByDayRadioButton) {
//            splitBy = SplitBy.DAY;
//        } else if (toggle == mSplitByWeekRadioButton) {
//            splitBy = SplitBy.WEEK;
//        } else if (toggle == mSplitByMonthRadioButton) {
//            splitBy = SplitBy.MONTH;
//        } else if (toggle == mSplitByYearRadioButton) {
//            splitBy = SplitBy.YEAR;
//        } else if (toggle == mSplitByNoneRadioButton) {
//            splitBy = SplitBy.NONE;
//        }
//
//        settings.setSplitBy(splitBy);
//
//        mOptions.put(PhotosOptions.KEY_SETTINGS, Mapo.getGson().toJson(settings));
//        Mapton.getGlobalState().put(Mapo.KEY_SETTINGS_UPDATED, settings);
//    }
}
