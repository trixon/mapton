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
package org.mapton.addon.watermark;

import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.addon.watermark.ModuleOptions.*;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class WatermarkOptionsView extends MOptionsView {

    public static final double MAX_BORDER_SIZE = 10;
    public static final double MAX_FONT_SIZE = FxHelper.getUIScaled(48);
    public static final double MIN_FONT_SIZE = FxHelper.getUIScaled(10);
    private final ColorPicker mBackgroundColorPicker = new ColorPicker();
    private final ColorPicker mBorderColorPicker = new ColorPicker();
    private final Slider mBorderSizeSlider = new Slider(0, MAX_BORDER_SIZE, DEFAULT_BORDER_SIZE);
    private final MPresetPopOver mFilterPresetPopOver;
    private final ColorPicker mFontColorPicker = new ColorPicker();
    private final Slider mFontSizeSlider = new Slider(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE);
    private final Slider mOpacitySlider = new Slider(0, 1, DEFAULT_OPACITY);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final TextField mPatternTextField = new TextField();

    public WatermarkOptionsView() {
        mFilterPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "watermark");
        createUI();
        initSession();
    }

    private void createUI() {
        var restoreDefaultsRunnable = (Runnable) () -> {
            if (mFilterPresetPopOver.restoreDefaultIfExists()) {
                //
            } else {
                mOptions.reset();
            }
        };
        var actions = List.of(
                getRestoreDefaultsAction(restoreDefaultsRunnable),
                ActionUtils.ACTION_SPAN,
                mFilterPresetPopOver.getAction()
        );
        createToolbar(actions);

        var patternLabel = new Label(Dict.PATTERN.toString());
        var opacityLabel = new Label(Dict.OPACITY.toString());
        var fontSizeLabel = new Label(Dict.SIZE.toString());
        var backgroundColorLabel = new Label(Dict.BACKGROUND.toString());
        var borderColorLabel = new Label(Dict.BORDER.toString());
        var borderSizeLabel = new Label(Dict.BORDER_SIZE.toString());
        var fontColorLabel = new Label(Dict.TEXT.toString());
        var gp = createGridPane();

        int row = 0;
        gp.addRow(row++, patternLabel);
        gp.addRow(row++, mPatternTextField);
        gp.addRow(row++, opacityLabel);
        gp.addRow(row++, mOpacitySlider);
        gp.addRow(row++, fontSizeLabel);
        gp.addRow(row++, mFontSizeSlider);
        gp.addRow(row++, fontColorLabel);
        gp.addRow(row++, mFontColorPicker);
        gp.addRow(row++, backgroundColorLabel);
        gp.addRow(row++, mBackgroundColorPicker);
        gp.addRow(row++, borderColorLabel);
        gp.addRow(row++, mBorderColorPicker);
        gp.addRow(row++, borderSizeLabel);
        gp.addRow(row++, mBorderSizeSlider);

        FxHelper.autoSizeRegionHorizontal(
                mPatternTextField,
                mOpacitySlider,
                mFontSizeSlider,
                mBackgroundColorPicker,
                mFontColorPicker,
                mBorderColorPicker,
                mBorderSizeSlider
        );

        FxHelper.setPadding(FxHelper.getUIScaledInsets(6, 0, 0, 0),
                opacityLabel,
                fontSizeLabel,
                fontColorLabel,
                backgroundColorLabel,
                borderColorLabel,
                borderSizeLabel
        );

        setCenter(gp);
    }

    private void initSession() {
        loadAndBind(mBorderColorPicker, mOptions.borderColorProperty());
        loadAndBind(mFontColorPicker, mOptions.fontColorProperty());
        loadAndBind(mBackgroundColorPicker, mOptions.backgroundColorProperty());

        mPatternTextField.textProperty().bindBidirectional(mOptions.patternProperty());
        mOpacitySlider.valueProperty().bindBidirectional(mOptions.opacityProperty());
        mFontSizeSlider.valueProperty().bindBidirectional(mOptions.fontSizeProperty());
        mBorderSizeSlider.valueProperty().bindBidirectional(mOptions.borderSizeProperty());
    }

    private void loadAndBind(ColorPicker colorPicker, StringProperty colorStringProperty) {
        colorPicker.setValue(Color.web(colorStringProperty.get()));
        var valueAsStringProperty = new SimpleStringProperty(FxHelper.colorToHexRGBA(colorPicker.getValue()));
        valueAsStringProperty.bindBidirectional(colorStringProperty);
        colorPicker.setOnAction(event -> {
            colorStringProperty.set(FxHelper.colorToHexRGBA(colorPicker.getValue()));
        });
        colorStringProperty.addListener((p, o, n) -> {
            try {
                colorPicker.setValue(Color.web(n));
            } catch (IllegalArgumentException e) {
                System.err.println(e);
            }
        });
    }

}
