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
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import static org.mapton.worldwind.ModuleOptions.*;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BackgroundImageOptionsView extends BorderPane {

    private final Slider mOpacitySlider = new Slider(0, 1, 1);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();

    public BackgroundImageOptionsView() {
        createUI();
        initStates();
        initListeners();
        load();
    }

    private void createUI() {
        var opacityBox = new VBox(new Label(Dict.OPACITY.toString()), mOpacitySlider);
        opacityBox.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(opacityBox);
    }

    private void initListeners() {
        mOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.put(KEY_BACKGROUND_IMAGE_OPACITY, mOpacitySlider.getValue());
        });
    }

    private void initStates() {
    }

    private void load() {
        mOpacitySlider.setValue(mOptions.getDouble(KEY_BACKGROUND_IMAGE_OPACITY, DEFAULT_BACKGROUND_IMAGE_OPACITY));
    }
}
