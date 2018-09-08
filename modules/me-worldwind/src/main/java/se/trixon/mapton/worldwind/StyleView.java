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
package se.trixon.mapton.worldwind;

import gov.nasa.worldwind.layers.Layer;
import java.util.ArrayList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.controlsfx.control.CheckListView;
import se.trixon.almond.util.Dict;
import se.trixon.mapton.core.api.DictMT;

/**
 *
 * @author Patrik Karlström
 */
public class StyleView extends HBox {

    private CheckBox mCompassCheckBox;
    private CheckBox mControlsCheckBox;
    private final CheckListView<Layer> mLayerListView = new CheckListView<>();
    private final GridPane mLeftPane = new GridPane();
    private RadioButton mModeFlatRadioButton;
    private RadioButton mModeGlobeRadioButton;
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private ComboBox<String> mProjComboBox;
    private final ArrayList<String> mProjections = new ArrayList<>();
    private CheckBox mScaleBarCheckBox;
    private CheckBox mWorldMapCheckBox;

    public StyleView() {
        mProjections.add(DictMT.PROJ_LAT_LON.toString());
        mProjections.add(DictMT.PROJ_MERCATOR.toString());
        mProjections.add(DictMT.PROJ_POLAR_NORTH.toString());
        mProjections.add(DictMT.PROJ_POLAR_SOUTH.toString());
        mProjections.add(DictMT.PROJ_SINUSOIDAL.toString());
        mProjections.add(DictMT.PROJ_SINUSOIDAL_MODIFIED.toString());
        mProjections.add(DictMT.PROJ_TRANSVERSE_MERCATOR.toString());
        mProjections.add(DictMT.PROJ_UPS_NORTH.toString());
        mProjections.add(DictMT.PROJ_UPS_SOUTH.toString());

        createUI();
        load();
    }

    private void createUI() {
        setSpacing(16);
        setPadding(new Insets(8, 16, 16, 16));
        mLeftPane.setPrefWidth(200);

        Insets topInsets = new Insets(12, 0, 0, 0);
        Label modeLabel = new Label(Dict.MODE.toString());
        Label projLabel = new Label(DictMT.PROJECTION.toString());
        GridPane.setMargin(projLabel, topInsets);

        ToggleGroup modeToggleGroup = new ToggleGroup();
        mModeGlobeRadioButton = new RadioButton(DictMT.GLOBE.toString());
        mModeGlobeRadioButton.setToggleGroup(modeToggleGroup);
        GridPane.setMargin(mModeGlobeRadioButton, topInsets);
        mModeFlatRadioButton = new RadioButton(DictMT.FLAT.toString());
        mModeFlatRadioButton.setToggleGroup(modeToggleGroup);
        GridPane.setMargin(mModeFlatRadioButton, topInsets);

        mProjComboBox = new ComboBox();

        mWorldMapCheckBox = new CheckBox(DictMT.WORLD_MAP.toString());
        GridPane.setMargin(mWorldMapCheckBox, topInsets);

        mScaleBarCheckBox = new CheckBox(DictMT.SCALE_BAR.toString());
        GridPane.setMargin(mScaleBarCheckBox, topInsets);

        mControlsCheckBox = new CheckBox(DictMT.VIEW_CONTROLS.toString());
        GridPane.setMargin(mControlsCheckBox, topInsets);

        mCompassCheckBox = new CheckBox(DictMT.COMPASS.toString());
        GridPane.setMargin(mCompassCheckBox, topInsets);

        mProjComboBox.getItems().addAll(mProjections);

        mLeftPane.addColumn(
                0,
                modeLabel,
                mModeGlobeRadioButton,
                mModeFlatRadioButton,
                projLabel,
                mProjComboBox,
                mWorldMapCheckBox,
                mCompassCheckBox,
                mControlsCheckBox,
                mScaleBarCheckBox
        );

        mModeFlatRadioButton.setMaxWidth(Double.MAX_VALUE);
        mModeGlobeRadioButton.setMaxWidth(Double.MAX_VALUE);
        mProjComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(mProjComboBox, Priority.ALWAYS);
        GridPane.setFillWidth(mProjComboBox, true);
        mProjComboBox.disableProperty().bind(mModeGlobeRadioButton.selectedProperty());
        mModeGlobeRadioButton.setOnAction((event -> {
            mOptions.setMapGlobe(true);
        }));
        mModeFlatRadioButton.setOnAction((event -> {
            mOptions.setMapGlobe(false);
        }));

        mProjComboBox.setOnAction((event -> {
            mOptions.setMapProjection(mProjComboBox.getSelectionModel().getSelectedIndex());
        }));

        mWorldMapCheckBox.setOnAction((event) -> {
            mOptions.setDisplayWorldMap(mWorldMapCheckBox.isSelected());
        });

        mScaleBarCheckBox.setOnAction((event) -> {
            mOptions.setDisplayScaleBar(mScaleBarCheckBox.isSelected());
        });

        mControlsCheckBox.setOnAction((event) -> {
            mOptions.setDisplayControls(mControlsCheckBox.isSelected());
        });

        mCompassCheckBox.setOnAction((event) -> {
            mOptions.setDisplayCompass(mCompassCheckBox.isSelected());
        });

        getChildren().addAll(
                mLeftPane,
                new Separator(Orientation.VERTICAL),
                mLayerListView
        );
    }

    private void load() {
        if (mOptions.isMapGlobe()) {
            mModeGlobeRadioButton.setSelected(true);
        } else {
            mModeFlatRadioButton.setSelected(true);
        }
        mProjComboBox.getSelectionModel().select(mOptions.getMapProjection());

        mWorldMapCheckBox.setSelected(mOptions.isDisplayWorldMap());
        mScaleBarCheckBox.setSelected(mOptions.isDisplayScaleBar());
        mControlsCheckBox.setSelected(mOptions.isDisplayControls());
        mCompassCheckBox.setSelected(mOptions.isDisplayCompass());
    }
}
