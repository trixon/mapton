/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade.distance;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import org.mapton.api.ui.MOptionsView;
import org.mapton.butterfly_format.types.BDimension;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ManagerOptionsView extends MOptionsView {

    private final DistanceLayerOptions mLayerOptions = DistanceLayerOptions.getInstance();
//    private final MPresetPopOver mPresetPopOver;
//    private final ResourceBundle mBundle = NbBundle.getBundle(ManagerOptionsView.class);
    private final ComboBox<BDimension> mModeComboBox = new ComboBox<>();

    public ManagerOptionsView() {
//        mPresetPopOver = new MPresetPopOver(mOptions, MPresetPopOver.PARENT_NODE_OPTIONS, "annotations");
        createUI();
        initSession();
    }

    private void createUI() {
        mModeComboBox.getItems().addAll(BDimension._1d, BDimension._3d);

        var gp = createGridPane();
        var modeLabel = new Label(Dict.MODE.toString());
        int row = 0;
        gp.addRow(row++, modeLabel);
        gp.addRow(row++, mModeComboBox);

//        mLimitSpinner.setEditable(true);
//        FxHelper.autoCommitSpinners(mLimitSpinner);
        FxHelper.autoSizeRegionHorizontal(
                //                mLimitSpinner,
                mModeComboBox
        //                mTimeOutComboBox
        );

        mModeComboBox.setPrefWidth(150);
        FxHelper.autoSizeColumn(gp, 2);
//        setLabelPadding(timeoutLabel);
        setCenter(gp);
    }

    private void initSession() {
//        BindingHelper.bindBidirectional(mLimitSpinner.getValueFactory().valueProperty(), mOptions.limitProperty());
        mModeComboBox.valueProperty().bindBidirectional(mLayerOptions.distanceModeProperty());
//        mTimeOutComboBox.valueProperty().bindBidirectional(mOptions.timeoutProperty());
    }
}
