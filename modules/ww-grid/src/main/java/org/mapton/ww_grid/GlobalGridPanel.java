/* 
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.ww_grid;

import java.util.ResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import static org.mapton.ww_grid.Options.*;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxDialogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class GlobalGridPanel extends FxDialogPanel {

    private CheckBox mClampToGroundCheckBox;
    private CheckBox mEquatorCheckBox;
    private CheckBox mLatitudesCheckBox;
    private CheckBox mLongitudesCheckBox;
    private final Options mOptions = Options.getInstance();
    private CheckBox mPolarAntarticCheckBox;
    private CheckBox mPolarArticCheckBox;
    private CheckBox mTropicCancerCheckBox;
    private CheckBox mTropicCapricornCheckBox;

    @Override
    protected void fxConstructor() {
        setScene(createScene());
        initStates();
        initListeners();
    }

    private Scene createScene() {
        ResourceBundle bundle = NbBundle.getBundle(GridTopComponent.class);
        mClampToGroundCheckBox = new CheckBox("CLAMP TO GROUND");

        mLongitudesCheckBox = new CheckBox(bundle.getString("longitudes"));
        mLatitudesCheckBox = new CheckBox(bundle.getString("latitudes"));

        mPolarArticCheckBox = new CheckBox(bundle.getString("arctic_circle"));
        mTropicCancerCheckBox = new CheckBox(bundle.getString("tropic_cancer"));
        mEquatorCheckBox = new CheckBox(bundle.getString("equator"));
        mTropicCapricornCheckBox = new CheckBox(bundle.getString("tropic_capricorn"));
        mPolarAntarticCheckBox = new CheckBox(bundle.getString("antarctic_circle"));

        Font defaultFont = Font.getDefault();

        Label presentationLabel = new Label(bundle.getString("major_latitudes"));
        presentationLabel.setFont(new Font(defaultFont.getSize() * 1.2));

        VBox vbox = new VBox(8,
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

        vbox.setPadding(new Insets(28));

        return new Scene(vbox);
    }

    private void initListeners() {
        mClampToGroundCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_CLAMP_TO_GROUND, mClampToGroundCheckBox.isSelected());
        });

        mLongitudesCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_LONGITUDES, mLongitudesCheckBox.isSelected());
        });

        mLatitudesCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_LATITUDES, mLatitudesCheckBox.isSelected());
        });

        mPolarArticCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_POLAR_ARCTIC, mPolarArticCheckBox.isSelected());
        });

        mTropicCancerCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_TROPIC_CANCER, mTropicCancerCheckBox.isSelected());
        });

        mEquatorCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_EQUATOR, mEquatorCheckBox.isSelected());
        });

        mTropicCapricornCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_TROPIC_CAPRICORN, mTropicCapricornCheckBox.isSelected());
        });

        mPolarAntarticCheckBox.setOnAction((event) -> {
            mOptions.put(KEY_GLOBAL_POLAR_ANTARCTIC, mPolarAntarticCheckBox.isSelected());
        });
    }

    private void initStates() {
        mClampToGroundCheckBox.setSelected(mOptions.is(KEY_GLOBAL_CLAMP_TO_GROUND));

        mLongitudesCheckBox.setSelected(mOptions.is(KEY_GLOBAL_LONGITUDES));
        mLatitudesCheckBox.setSelected(mOptions.is(KEY_GLOBAL_LATITUDES));

        mPolarArticCheckBox.setSelected(mOptions.is(KEY_GLOBAL_POLAR_ARCTIC));
        mTropicCancerCheckBox.setSelected(mOptions.is(KEY_GLOBAL_TROPIC_CANCER));
        mEquatorCheckBox.setSelected(mOptions.is(KEY_GLOBAL_EQUATOR));
        mTropicCapricornCheckBox.setSelected(mOptions.is(KEY_GLOBAL_TROPIC_CAPRICORN));
        mPolarAntarticCheckBox.setSelected(mOptions.is(KEY_GLOBAL_POLAR_ANTARCTIC));
    }

}
