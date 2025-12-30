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
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.core.api.ui.MPresetPopOver;
import org.mapton.worldwind.api.MOptionsView;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AnnotationsOptionsView extends MOptionsView {

    private final Spinner<Integer> mLimitSpinner = new Spinner<>(1, 10, AnnotationsOptions.DEFAULT_LIMIT);
    private final ComboBox<AnnotationLimitMode> mModeComboBox = new ComboBox<>();
    private final AnnotationsOptions mOptions = AnnotationsOptions.getInstance();
    private final MPresetPopOver mPresetPopOver;
    private final ComboBox<AnnotationTimeout> mTimeOutComboBox = new ComboBox<>();

    public AnnotationsOptionsView() {
        mPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "annotations");
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

        mModeComboBox.getItems().setAll(AnnotationLimitMode.values());
        mTimeOutComboBox.getItems().setAll(AnnotationTimeout.values());

        var gp = createGridPane();
        var limitLabel = new Label("Max");
        var modeLabel = new Label(Dict.MODE.toString());
        var timeoutLabel = new Label("Timeout");
        int row = 0;
        gp.addRow(row++, limitLabel, modeLabel);
        gp.addRow(row++, mLimitSpinner, mModeComboBox);
        gp.addRow(row++, new Label(), timeoutLabel);
        gp.addRow(row++, new Label(), mTimeOutComboBox);

        mLimitSpinner.setEditable(true);
        FxHelper.autoCommitSpinners(mLimitSpinner);
        FxHelper.autoSizeRegionHorizontal(
                mLimitSpinner,
                mModeComboBox,
                mTimeOutComboBox
        );

        FxHelper.autoSizeColumn(gp, 2);

        setCenter(gp);
    }

    private void initSession() {
        BindingHelper.bindBidirectional(mLimitSpinner.getValueFactory().valueProperty(), mOptions.limitProperty());
        mModeComboBox.valueProperty().bindBidirectional(mOptions.limitModeProperty());
        mTimeOutComboBox.valueProperty().bindBidirectional(mOptions.timeoutProperty());
    }
}
