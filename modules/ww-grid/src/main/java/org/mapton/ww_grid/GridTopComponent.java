/*
 * Copyright 2018 Patrik Karlström.
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
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.mapton.api.MDict;
import org.mapton.api.MTopComponent;
import org.mapton.api.Mapton;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.ww_grid//Grid//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "GridTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public final class GridTopComponent extends MTopComponent {

    private CheckBox mClampToGroundCheckBox;
    private CheckBox mEquatorCheckBox;
    private CheckBox mLatitudesCheckBox;
    private CheckBox mLongitudesCheckBox;
    private final Options mOptions = Options.getInstance();
    private CheckBox mPolarAntarticCheckBox;
    private CheckBox mPolarArticCheckBox;
    private BorderPane mRoot;
    private CheckBox mTropicCancerCheckBox;
    private CheckBox mTropicCapricornCheckBox;

    public GridTopComponent() {
        setName(MDict.GRID.toString());
    }

    @Override
    protected void initFX() {
        setScene(createScene());
        initStates();
        initListeners();
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private Scene createScene() {
        ResourceBundle bundle = NbBundle.getBundle(GridTopComponent.class);

        Label titleLabel = new Label(MDict.GRID.toString());

        mClampToGroundCheckBox = new CheckBox("CLAMP TO GROUND");

        mLongitudesCheckBox = new CheckBox(bundle.getString("longitudes"));
        mLatitudesCheckBox = new CheckBox(bundle.getString("latitudes"));

        mPolarArticCheckBox = new CheckBox(bundle.getString("arctic_circle"));
        mTropicCancerCheckBox = new CheckBox(bundle.getString("tropic_cancer"));
        mEquatorCheckBox = new CheckBox(bundle.getString("equator"));
        mTropicCapricornCheckBox = new CheckBox(bundle.getString("tropic_capricorn"));
        mPolarAntarticCheckBox = new CheckBox(bundle.getString("antarctic_circle"));

        Separator sep1 = new Separator(Orientation.HORIZONTAL);
        VBox titleBox = new VBox(8, titleLabel);
        titleBox.setAlignment(Pos.CENTER);
        Font defaultFont = Font.getDefault();

        titleLabel.prefWidthProperty().bind(titleBox.widthProperty());
        titleLabel.setBackground(Mapton.getThemeBackground());
        titleLabel.setAlignment(Pos.BASELINE_CENTER);
        titleLabel.setFont(new Font(defaultFont.getSize() * 2));

        Label presentationLabel = new Label(bundle.getString("major_latitudes"));
        presentationLabel.setFont(new Font(defaultFont.getSize() * 1.2));

        VBox vbox = new VBox(8,
                //                mClampToGroundCheckBox,
                mLongitudesCheckBox,
                mLatitudesCheckBox,
                presentationLabel,
                mPolarArticCheckBox,
                mTropicCancerCheckBox,
                mEquatorCheckBox,
                mTropicCapricornCheckBox,
                mPolarAntarticCheckBox,
                sep1
        );

        vbox.setPadding(new Insets(8));
        mRoot = new BorderPane(vbox);
        mRoot.setTop(titleBox);

        return new Scene(mRoot);
    }

    private void initListeners() {
        mClampToGroundCheckBox.setOnAction((event) -> {
            mOptions.setClampToGround(mClampToGroundCheckBox.isSelected());
        });

        mLongitudesCheckBox.setOnAction((event) -> {
            mOptions.setLongitudes(mLongitudesCheckBox.isSelected());
        });

        mLatitudesCheckBox.setOnAction((event) -> {
            mOptions.setLatitudes(mLatitudesCheckBox.isSelected());
        });

        mPolarArticCheckBox.setOnAction((event) -> {
            mOptions.setPolarArctic(mPolarArticCheckBox.isSelected());
        });

        mTropicCancerCheckBox.setOnAction((event) -> {
            mOptions.setTropicCancer(mTropicCancerCheckBox.isSelected());
        });

        mEquatorCheckBox.setOnAction((event) -> {
            mOptions.setEquator(mEquatorCheckBox.isSelected());
        });

        mTropicCapricornCheckBox.setOnAction((event) -> {
            mOptions.setTropicCapricorn(mTropicCapricornCheckBox.isSelected());
        });

        mPolarAntarticCheckBox.setOnAction((event) -> {
            mOptions.setPolarAntarctic(mPolarAntarticCheckBox.isSelected());
        });
    }

    private void initStates() {
        mClampToGroundCheckBox.setSelected(mOptions.isClampToGround());

        mLongitudesCheckBox.setSelected(mOptions.isLongitudes());
        mLatitudesCheckBox.setSelected(mOptions.isLatitudes());

        mPolarArticCheckBox.setSelected(mOptions.isPolarArctic());
        mTropicCancerCheckBox.setSelected(mOptions.isTropicCancer());
        mEquatorCheckBox.setSelected(mOptions.isEquator());
        mTropicCapricornCheckBox.setSelected(mOptions.isTropicCapricorn());
        mPolarAntarticCheckBox.setSelected(mOptions.isPolarAntarctic());
    }
}
