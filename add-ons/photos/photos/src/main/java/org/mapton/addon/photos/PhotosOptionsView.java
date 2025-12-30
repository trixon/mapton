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
import org.mapton.addon.photos.api.SplitBy;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.MOptionsView;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class PhotosOptionsView extends MOptionsView {

    private final ResourceBundle mBundle = NbBundle.getBundle(PhotosOptionsView.class);
    private final ColorPicker mGapColorPicker = new ColorPicker();
    private final PhotosOptions mOptions = PhotosOptions.getInstance();
    private final CheckBox mPlotGapCheckBox = new CheckBox(mBundle.getString("drawGapCheckBox"));
    private final CheckBox mPlotTrackCheckBox = new CheckBox(mBundle.getString("drawTrackCheckBox"));
    private final MPresetPopOver mPresetPopOver;
    private final ComboBox<SplitBy> mSplitByComboBox = new ComboBox<>();
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

        mSplitByComboBox.getItems().setAll(SplitBy.values());

        var gp = createGridPane();
        var widthLabel = new Label(Dict.Geometry.WIDTH.toString());
        var splitByLabel = new Label(Dict.SPLIT_BY.toString());
        var gapColorLabel = new Label(mBundle.getString("colorGap"));
        var trackColorLabel = new Label(mBundle.getString("colorTrack"));

        int row = 0;
        gp.addRow(row++, mPlotTrackCheckBox, mPlotGapCheckBox);

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

        BindingHelper.bindCheckBoxEnablement(mPlotTrackCheckBox, trackColorLabel, mTrackColorPicker);
        BindingHelper.bindCheckBoxEnablement(mPlotGapCheckBox, gapColorLabel, mGapColorPicker);
        gp2.disableProperty().bind(mPlotTrackCheckBox.selectedProperty().or(mPlotGapCheckBox.selectedProperty()).not());
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
                mSplitByComboBox,
                mWidthSpinner
        );

        FxHelper.autoSizeColumn(gp, 2);
        FxHelper.autoSizeColumn(gp2, 2);

        setCenter(gp);
    }

    private void initSession() {
        mPlotGapCheckBox.selectedProperty().bindBidirectional(mOptions.plotGapProperty());
        mPlotTrackCheckBox.selectedProperty().bindBidirectional(mOptions.plotTrackProperty());
        mGapColorPicker.valueProperty().bindBidirectional(mOptions.gapColorProperty());
        mTrackColorPicker.valueProperty().bindBidirectional(mOptions.trackColorProperty());
        mSplitByComboBox.valueProperty().bindBidirectional(mOptions.splitByProperty());
        BindingHelper.bindBidirectional(mWidthSpinner.getValueFactory().valueProperty(), mOptions.widthProperty());
    }

}
