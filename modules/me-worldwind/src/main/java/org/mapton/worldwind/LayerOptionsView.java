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
package org.mapton.worldwind;

import java.util.ArrayList;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.mapton.api.MDict;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LayerOptionsView extends VBox {

    private CheckBox mAtmosphereCheckBox;
    private CheckBox mCompassCheckBox;
    private CheckBox mControlsCheckBox;
    private CheckBox mElevationCheckBox;
    private final VBox mLeftPane = new VBox();
    private VBox mMapOpacityBox;
    private final Slider mMapOpacitySlider = new Slider(0, 1, 1);
    private CheckBox mMaskCheckBox;
    private ColorPicker mMaskColorPicker;
    private VBox mMaskOpacityBox;
    private final Slider mMaskOpacitySlider = new Slider(0, 1, 1);
    private RadioButton mModeFlatRadioButton;
    private RadioButton mModeGlobeRadioButton;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private CheckBox mPlaceNameCheckBox;
    private ComboBox<String> mProjComboBox;
    private final ArrayList<String> mProjections = new ArrayList<>();
    private final VBox mRightPane = new VBox();
    private CheckBox mScaleBarCheckBox;
    private CheckBox mStarsCheckBox;
    private CheckBox mWorldMapCheckBox;

    public LayerOptionsView() {
        mProjections.add(MDict.PROJ_LAT_LON.toString());
        mProjections.add(MDict.PROJ_MERCATOR.toString());
        mProjections.add(MDict.PROJ_POLAR_NORTH.toString());
        mProjections.add(MDict.PROJ_POLAR_SOUTH.toString());
        mProjections.add(MDict.PROJ_SINUSOIDAL.toString());
        mProjections.add(MDict.PROJ_SINUSOIDAL_MODIFIED.toString());
        mProjections.add(MDict.PROJ_TRANSVERSE_MERCATOR.toString());
        mProjections.add(MDict.PROJ_UPS_NORTH.toString());
        mProjections.add(MDict.PROJ_UPS_SOUTH.toString());

        createUI();
        initListeners();
        load();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(16));
        setPadding(FxHelper.getUIScaledInsets(16));
        var subBox = new HBox();
        subBox.setSpacing(FxHelper.getUIScaled(16));
        subBox.setPadding(FxHelper.getUIScaledInsets(8, 16, 16, 16));

        mMaskCheckBox = new CheckBox(Dict.MASK.toString());
        mMaskColorPicker = new ColorPicker();
        mMaskColorPicker.prefWidthProperty().bind(widthProperty());
        mMaskColorPicker.disableProperty().bind(mMaskCheckBox.selectedProperty().not());
        var color = FxHelper.colorFromHexRGBA(mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR));
        mMaskColorPicker.setValue(color);

        mMapOpacityBox = new VBox(new Label(Dict.OPACITY.toString()), mMapOpacitySlider);
        mMaskOpacityBox = new VBox(FxHelper.getUIScaled(8), mMaskCheckBox, mMaskOpacitySlider, mMaskColorPicker);
        mMaskOpacitySlider.disableProperty().bind(mMaskCheckBox.selectedProperty().not());

        double width = FxHelper.getUIScaled(200);
        mLeftPane.setPrefWidth(width);

        var topInsets = FxHelper.getUIScaledInsets(12, 0, 0, 0);
        var modeLabel = new Label(Dict.MODE.toString());
        var projLabel = new Label(MDict.PROJECTION.toString());
        var modeToggleGroup = new ToggleGroup();
        mModeGlobeRadioButton = new RadioButton(MDict.GLOBE.toString());
        mModeGlobeRadioButton.setToggleGroup(modeToggleGroup);
        mModeFlatRadioButton = new RadioButton(MDict.FLAT.toString());
        mModeFlatRadioButton.setToggleGroup(modeToggleGroup);

        mProjComboBox = new ComboBox<>();

        mProjComboBox.getItems().addAll(mProjections);
        mElevationCheckBox = new CheckBox(MDict.ELEVATION.toString());

        mModeFlatRadioButton.setMaxWidth(Double.MAX_VALUE);
        mModeGlobeRadioButton.setMaxWidth(Double.MAX_VALUE);
        mProjComboBox.setMaxWidth(Double.MAX_VALUE);
        mProjComboBox.disableProperty().bind(mModeGlobeRadioButton.selectedProperty());

        FxHelper.setPadding(
                topInsets,
                projLabel,
                mModeGlobeRadioButton,
                mModeFlatRadioButton,
                mElevationCheckBox
        );

        mLeftPane.getChildren().setAll(
                modeLabel,
                mModeGlobeRadioButton,
                mModeFlatRadioButton,
                projLabel,
                mProjComboBox
        );

        mWorldMapCheckBox = new CheckBox(MDict.WORLD_MAP.toString());
        mScaleBarCheckBox = new CheckBox(MDict.SCALE_BAR.toString());
        mControlsCheckBox = new CheckBox(MDict.VIEW_CONTROLS.toString());
        mControlsCheckBox.setDisable(true);
        mCompassCheckBox = new CheckBox(MDict.COMPASS.toString());
        mStarsCheckBox = new CheckBox(MDict.STARS.toString());
        mAtmosphereCheckBox = new CheckBox(MDict.ATMOSPHERE.toString());
        mPlaceNameCheckBox = new CheckBox(Dict.PLACE_NAMES.toString());
        mPlaceNameCheckBox.setDisable(true);
        mRightPane.setSpacing(FxHelper.getUIScaled(12));
        mRightPane.getChildren().setAll(
                mWorldMapCheckBox,
                mCompassCheckBox,
                mControlsCheckBox,
                mScaleBarCheckBox,
                mStarsCheckBox,
                mAtmosphereCheckBox,
                mPlaceNameCheckBox,
                mElevationCheckBox
        );

        mLeftPane.getChildren().stream()
                .filter(node -> node instanceof Region)
                .map(node -> (Region) node)
                .forEachOrdered(region -> {
                    region.prefWidthProperty().bind(mLeftPane.widthProperty());
                });

        mRightPane.getChildren().stream()
                .filter(node -> node instanceof Region)
                .map(node -> (Region) node)
                .forEachOrdered(region -> {
                    region.prefWidthProperty().bind(mRightPane.widthProperty());
                });

        mLeftPane.prefWidthProperty().bind(widthProperty());
        mRightPane.prefWidthProperty().bind(widthProperty());
        subBox.getChildren().addAll(
                mLeftPane,
                new Separator(Orientation.VERTICAL),
                mRightPane
        );

        getChildren().setAll(
                mMapOpacityBox,
                mMaskOpacityBox,
                subBox
        );
    }

    private void initListeners() {
        mMaskCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_MASK, mMaskCheckBox.isSelected());
        });

        mMapOpacitySlider.valueProperty().addListener((p, o, n) -> {
            mOptions.put(KEY_MAP_OPACITY, mMapOpacitySlider.getValue());
        });

        mMaskOpacitySlider.valueProperty().addListener((p, o, n) -> {
            mOptions.put(KEY_MASK_OPACITY, mMaskOpacitySlider.getValue());
        });

        mMaskColorPicker.valueProperty().addListener((p, o, n) -> {
            mOptions.put(KEY_MASK_COLOR, FxHelper.colorToHexRGB(n));
        });

        mModeGlobeRadioButton.setOnAction((event -> {
            mOptions.put(KEY_MAP_GLOBE, true);
        }));

        mModeFlatRadioButton.setOnAction((event -> {
            mOptions.put(KEY_MAP_GLOBE, false);
        }));

        mProjComboBox.setOnAction((event -> {
            mOptions.put(KEY_MAP_PROJECTION, mProjComboBox.getSelectionModel().getSelectedIndex());
        }));

        mWorldMapCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_WORLD_MAP, mWorldMapCheckBox.isSelected());
        });

        mScaleBarCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_SCALE_BAR, mScaleBarCheckBox.isSelected());
        });

        mControlsCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_CONTROLS, mControlsCheckBox.isSelected());
        });

        mCompassCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_COMPASS, mCompassCheckBox.isSelected());
        });

        mAtmosphereCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_ATMOSPHERE, mAtmosphereCheckBox.isSelected());
        });

        mStarsCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_STARS, mStarsCheckBox.isSelected());
        });

        mPlaceNameCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_PLACE_NAMES, mPlaceNameCheckBox.isSelected());
        });

        mElevationCheckBox.setOnAction(event -> {
            mOptions.put(KEY_MAP_ELEVATION, mElevationCheckBox.isSelected());
        });
    }

    private void load() {
        mMapOpacitySlider.setValue(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
        mMaskOpacitySlider.setValue(mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY));
        mMaskCheckBox.setSelected(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));

        if (mOptions.is(KEY_MAP_GLOBE, DEFAULT_MAP_GLOBE)) {
            mModeGlobeRadioButton.setSelected(true);
        } else {
            mModeFlatRadioButton.setSelected(true);
        }

        mProjComboBox.getSelectionModel().select(mOptions.getInt(KEY_MAP_PROJECTION, DEFAULT_MAP_PROJECTION));
        mWorldMapCheckBox.setSelected(mOptions.is(KEY_DISPLAY_WORLD_MAP, DEFAULT_DISPLAY_WORLD_MAP));
        mScaleBarCheckBox.setSelected(mOptions.is(KEY_DISPLAY_SCALE_BAR, DEFAULT_DISPLAY_SCALE_BAR));
        mControlsCheckBox.setSelected(mOptions.is(KEY_DISPLAY_CONTROLS, DEFAULT_DISPLAY_CONTROLS));
        mCompassCheckBox.setSelected(mOptions.is(KEY_DISPLAY_COMPASS, DEFAULT_DISPLAY_COMPASS));
        mAtmosphereCheckBox.setSelected(mOptions.is(KEY_DISPLAY_ATMOSPHERE, DEFAULT_DISPLAY_ATMOSPHERE));
        mStarsCheckBox.setSelected(mOptions.is(KEY_DISPLAY_STARS, DEFAULT_DISPLAY_STARS));
        mPlaceNameCheckBox.setSelected(mOptions.is(KEY_DISPLAY_PLACE_NAMES, DEFAULT_DISPLAY_PLACE_NAMES));
        mElevationCheckBox.setSelected(mOptions.is(KEY_MAP_ELEVATION, DEFAULT_MAP_ELEVATION));
    }

}
