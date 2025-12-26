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

import java.util.List;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.VBox;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BackgroundImageOptionsView extends MOptionsView {

    private final Slider mOpacitySlider = new Slider(0, 1, BackgroundImageOptions.DEFAULT_OPACITY);
    private final BackgroundImageOptions mOptions = BackgroundImageOptions.getInstance();
    private final MPresetPopOver mPresetPopOver;

    public BackgroundImageOptionsView() {
        mPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "backgroundImage");
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

        var opacityBox = new VBox(new Label(Dict.OPACITY.toString()), mOpacitySlider);
        opacityBox.setPadding(FxHelper.getUIScaledInsets(8));

        setCenter(opacityBox);
    }

    private void initSession() {
        mOpacitySlider.valueProperty().bindBidirectional(mOptions.opacityProperty());
    }
}
