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

import javafx.beans.value.ObservableValue;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_DISPLAY_MASK;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MAP_OPACITY;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MASK_COLOR;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MASK_OPACITY;
import static org.mapton.worldwind.ModuleOptions.KEY_DISPLAY_MASK;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_OPACITY;
import static org.mapton.worldwind.ModuleOptions.KEY_MASK_COLOR;
import static org.mapton.worldwind.ModuleOptions.KEY_MASK_OPACITY;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LayerBackgroundOptionsView extends VBox {

    private VBox mMapOpacityBox;
    private final Slider mMapOpacitySlider = new Slider(0, 1, 1);
    private CheckBox mMaskCheckBox;
    private ColorPicker mMaskColorPicker;
    private VBox mMaskOpacityBox;
    private final Slider mMaskOpacitySlider = new Slider(0, 1, 1);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();

    public LayerBackgroundOptionsView() {
        createUI();
        initListeners();
        load();
    }

    private void createUI() {
        setSpacing(FxHelper.getUIScaled(16));
        setPadding(FxHelper.getUIScaledInsets(16));
        setPrefSize(FxHelper.getUIScaled(400), FxHelper.getUIScaled(200));

        mMaskCheckBox = new CheckBox(Dict.MASK.toString());
        mMaskColorPicker = new ColorPicker();
        mMaskColorPicker.prefWidthProperty().bind(widthProperty());
        mMaskColorPicker.disableProperty().bind(mMaskCheckBox.selectedProperty().not());
        var color = FxHelper.colorFromHexRGBA(mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR));
        mMaskColorPicker.setValue(color);

        mMapOpacityBox = new VBox(new Label(Dict.OPACITY.toString()), mMapOpacitySlider);
        mMaskOpacityBox = new VBox(FxHelper.getUIScaled(8), mMaskCheckBox, mMaskOpacitySlider, mMaskColorPicker);
        mMaskCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_MASK, mMaskCheckBox.isSelected());
        });
        mMaskOpacitySlider.disableProperty().bind(mMaskCheckBox.selectedProperty().not());

        getChildren().setAll(mMapOpacityBox, mMaskOpacityBox);
    }

    private void initListeners() {
        mMapOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.put(KEY_MAP_OPACITY, mMapOpacitySlider.getValue());
        });

        mMaskOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.put(KEY_MASK_OPACITY, mMaskOpacitySlider.getValue());
        });

        mMaskColorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            mOptions.put(KEY_MASK_COLOR, FxHelper.colorToHexRGB(t1));
        });
    }

    private void load() {
        mMapOpacitySlider.setValue(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
        mMaskOpacitySlider.setValue(mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY));
        mMaskCheckBox.setSelected(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));
    }

}
