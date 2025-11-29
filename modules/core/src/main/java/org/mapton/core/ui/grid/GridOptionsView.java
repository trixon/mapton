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
package org.mapton.core.ui.grid;

import java.util.ResourceBundle;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.*;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GridOptionsView extends VBox {

    private CheckBox mClampToGroundCheckBox;
    private CheckBox mEquatorCheckBox;
    private CheckBox mLatitudesCheckBox;
    private CheckBox mLongitudesCheckBox;
    private final MOptions mOptions = MOptions.getInstance();
    private CheckBox mPlotGlobalsCheckBox;
    private CheckBox mPolarAntarticCheckBox;
    private CheckBox mPolarArticCheckBox;
    private CheckBox mTropicCancerCheckBox;
    private CheckBox mTropicCapricornCheckBox;
    private CheckBox mPlotLocalsCheckBox;

    public GridOptionsView() {
        createUI();
        initStates();
        initListeners();
    }

    private void createUI() {
        ResourceBundle bundle = NbBundle.getBundle(GridTopComponent.class);
        mPlotGlobalsCheckBox = new CheckBox(Dict.GLOBAL.toString());
        mPlotGlobalsCheckBox.setStyle("-fx-font-weight: bold; -fx-font-size: 1.3em");

        mPlotLocalsCheckBox = new CheckBox(Dict.LOCAL.toString());
        mPlotLocalsCheckBox.setStyle("-fx-font-weight: bold; -fx-font-size: 1.3em");
//        mPlotLocalsCheckBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8));

        mClampToGroundCheckBox = new CheckBox("CLAMP TO GROUND");

        mLongitudesCheckBox = new CheckBox(bundle.getString("longitudes"));
        mLatitudesCheckBox = new CheckBox(bundle.getString("latitudes"));

        mPolarArticCheckBox = new CheckBox(bundle.getString("arctic_circle"));
        mTropicCancerCheckBox = new CheckBox(bundle.getString("tropic_cancer"));
        mEquatorCheckBox = new CheckBox(bundle.getString("equator"));
        mTropicCapricornCheckBox = new CheckBox(bundle.getString("tropic_capricorn"));
        mPolarAntarticCheckBox = new CheckBox(bundle.getString("antarctic_circle"));

        var presentationLabel = new Label(bundle.getString("major_latitudes"));
        presentationLabel.setFont(new Font(FxHelper.getScaledFontSize() * 1.2));

        var vbox = new VBox(8,
                //mClampToGroundCheckBox,
                mLongitudesCheckBox,
                mLatitudesCheckBox,
                presentationLabel,
                mPolarArticCheckBox,
                mTropicCancerCheckBox,
                mEquatorCheckBox,
                mTropicCapricornCheckBox,
                mPolarAntarticCheckBox
        );

        vbox.disableProperty().bind(mPlotGlobalsCheckBox.selectedProperty().not());

        setSpacing(8);
        getChildren().setAll(mPlotGlobalsCheckBox, vbox, mPlotLocalsCheckBox);
        vbox.setPadding(FxHelper.getUIScaledInsets(4, 0, 0, 16));
    }

    private void initListeners() {
        mPlotGlobalsCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_PLOT, mPlotGlobalsCheckBox.isSelected());
        });

        mPlotLocalsCheckBox.setOnAction(event -> {
            mOptions.put(KEY_GRID_LOCAL_PLOT, mPlotLocalsCheckBox.isSelected());
        });

        mClampToGroundCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_CLAMP_TO_GROUND, mClampToGroundCheckBox.isSelected());
        });

        mLongitudesCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_LONGITUDES, mLongitudesCheckBox.isSelected());
        });

        mLatitudesCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_LATITUDES, mLatitudesCheckBox.isSelected());
        });

        mPolarArticCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_POLAR_ARCTIC, mPolarArticCheckBox.isSelected());
        });

        mTropicCancerCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_TROPIC_CANCER, mTropicCancerCheckBox.isSelected());
        });

        mEquatorCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_EQUATOR, mEquatorCheckBox.isSelected());
        });

        mTropicCapricornCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_TROPIC_CAPRICORN, mTropicCapricornCheckBox.isSelected());
        });

        mPolarAntarticCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GRID_GLOBAL_POLAR_ANTARCTIC, mPolarAntarticCheckBox.isSelected());
        });

    }

    private void initStates() {
        mPlotGlobalsCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_PLOT));
        mPlotLocalsCheckBox.setSelected(mOptions.is(KEY_GRID_LOCAL_PLOT));

        mClampToGroundCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_CLAMP_TO_GROUND));

        mLongitudesCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_LONGITUDES));
        mLatitudesCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_LATITUDES));

        mPolarArticCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_POLAR_ARCTIC));
        mTropicCancerCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_TROPIC_CANCER));
        mEquatorCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_EQUATOR));
        mTropicCapricornCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_TROPIC_CAPRICORN));
        mPolarAntarticCheckBox.setSelected(mOptions.is(KEY_GRID_GLOBAL_POLAR_ANTARCTIC));
    }

}
