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
import javafx.collections.FXCollections;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.GridPane;
import org.controlsfx.control.action.ActionUtils;
import static org.mapton.addon.watermark.WatermarkOptions.*;
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
    private final MPresetPopOver mPresetPopOver;
    private final ColorPicker mFontColorPicker = new ColorPicker();
    private final Slider mFontSizeSlider = new Slider(MIN_FONT_SIZE, MAX_FONT_SIZE, DEFAULT_FONT_SIZE);
    private final Slider mOpacitySlider = new Slider(0, 1, DEFAULT_OPACITY);
    private final WatermarkOptions mOptions = WatermarkOptions.getInstance();
    private final ComboBox<String> mPatternComboBox = new ComboBox<>();

    public WatermarkOptionsView() {
        mPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "watermark");
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

        mPatternComboBox.setEditable(true);
        mPatternComboBox.setItems(FXCollections.observableArrayList(
                WatermarkOptions.DEFAULT_PATTERN,
                "yyyy-MM-dd",
                "yyyy-MM-dd HH.mm",
                "HH:mm",
                "HH:mm:ss",
                "'prefix' HH:mm:ss 'suffix'",
                "'Plain text: Hello, World!'"
        ));
        var patternLabel = new Label(Dict.PATTERN.toString());
        var opacityLabel = new Label(Dict.OPACITY.toString());
        var fontSizeLabel = new Label(Dict.SIZE.toString());
        var backgroundColorLabel = new Label(Dict.BACKGROUND.toString());
        var borderColorLabel = new Label(Dict.BORDER.toString());
        var borderSizeLabel = new Label(Dict.BORDER_SIZE.toString());
        var fontColorLabel = new Label(Dict.TEXT.toString());
        var gp = createGridPane();

        int row = 0;
        gp.add(patternLabel, 0, row++, GridPane.REMAINING, 1);
        gp.add(mPatternComboBox, 0, row++, GridPane.REMAINING, 1);
        gp.addRow(row++, fontColorLabel, backgroundColorLabel, borderColorLabel);
        gp.addRow(row++, mFontColorPicker, mBackgroundColorPicker, mBorderColorPicker);
        gp.addRow(row++, fontSizeLabel, opacityLabel, borderSizeLabel);
        gp.addRow(row++, mFontSizeSlider, mOpacitySlider, mBorderSizeSlider);

        FxHelper.autoSizeRegionHorizontal(
                mPatternComboBox,
                mOpacitySlider,
                mFontSizeSlider,
                mBackgroundColorPicker,
                mFontColorPicker,
                mBorderColorPicker,
                mBorderSizeSlider
        );

        setLabelPadding(
                opacityLabel,
                fontSizeLabel,
                fontColorLabel,
                backgroundColorLabel,
                borderColorLabel,
                borderSizeLabel
        );

        FxHelper.autoSizeColumn(gp, 3);

        setCenter(gp);
    }

    private void initSession() {
        mBorderColorPicker.valueProperty().bindBidirectional(mOptions.borderColorProperty());
        mBackgroundColorPicker.valueProperty().bindBidirectional(mOptions.backgroundColorProperty());
        mFontColorPicker.valueProperty().bindBidirectional(mOptions.fontColorProperty());
        mPatternComboBox.getEditor().textProperty().bindBidirectional(mOptions.patternProperty());
        mOpacitySlider.valueProperty().bindBidirectional(mOptions.opacityProperty());
        mFontSizeSlider.valueProperty().bindBidirectional(mOptions.fontSizeProperty());
        mBorderSizeSlider.valueProperty().bindBidirectional(mOptions.borderSizeProperty());
    }
}
