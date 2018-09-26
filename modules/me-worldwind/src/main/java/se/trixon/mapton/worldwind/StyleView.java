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

import java.util.ArrayList;
import java.util.Collections;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.Dict;
import se.trixon.mapton.api.MDict;
import se.trixon.mapton.worldwind.api.MapStyle;

/**
 *
 * @author Patrik Karlström
 */
public class StyleView extends HBox {

    private CheckBox mElevationCheckBox;
    private CheckBox mAtmosphereCheckBox;
    private CheckBox mCompassCheckBox;
    private CheckBox mControlsCheckBox;
    private final GridPane mLeftPane = new GridPane();
    private RadioButton mModeFlatRadioButton;
    private RadioButton mModeGlobeRadioButton;
    private VBox mOpacityBox;
    private final Slider mOpacitySlider = new Slider(0, 1, 1);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private ComboBox<String> mProjComboBox;
    private final ArrayList<String> mProjections = new ArrayList<>();
    private CheckBox mScaleBarCheckBox;
    private CheckBox mStarsCheckBox;
    private final VBox mStyleBox = new VBox(16);
    private CheckBox mWorldMapCheckBox;

    public StyleView() {
        mProjections.add(MDict.PROJ_LAT_LON.toString());
        mProjections.add(MDict.PROJ_MERCATOR.toString());
        mProjections.add(MDict.PROJ_POLAR_NORTH.toString());
        mProjections.add(MDict.PROJ_POLAR_SOUTH.toString());
        mProjections.add(MDict.PROJ_SINUSOIDAL.toString());
        mProjections.add(MDict.PROJ_SINUSOIDAL_MODIFIED.toString());
        mProjections.add(MDict.PROJ_TRANSVERSE_MERCATOR.toString());
        mProjections.add(MDict.PROJ_UPS_NORTH.toString());
        mProjections.add(MDict.PROJ_UPS_SOUTH.toString());

        createUI();
        initListeners();
        load();
        Lookup.getDefault().lookupResult(MapStyle.class).addLookupListener((LookupEvent ev) -> {
            initStyle();
        });

        initStyle();
    }

    private void createUI() {
        setSpacing(16);
        setPadding(new Insets(8, 16, 16, 16));
        mLeftPane.setPrefWidth(200);
        mStyleBox.setPrefWidth(200);

        Insets topInsets = new Insets(12, 0, 0, 0);
        Label modeLabel = new Label(Dict.MODE.toString());
        Label projLabel = new Label(MDict.PROJECTION.toString());
        GridPane.setMargin(projLabel, topInsets);

        ToggleGroup modeToggleGroup = new ToggleGroup();
        mModeGlobeRadioButton = new RadioButton(MDict.GLOBE.toString());
        mModeGlobeRadioButton.setToggleGroup(modeToggleGroup);
        GridPane.setMargin(mModeGlobeRadioButton, topInsets);
        mModeFlatRadioButton = new RadioButton(MDict.FLAT.toString());
        mModeFlatRadioButton.setToggleGroup(modeToggleGroup);
        GridPane.setMargin(mModeFlatRadioButton, topInsets);

        mProjComboBox = new ComboBox();

        mWorldMapCheckBox = new CheckBox(MDict.WORLD_MAP.toString());
        GridPane.setMargin(mWorldMapCheckBox, topInsets);

        mScaleBarCheckBox = new CheckBox(MDict.SCALE_BAR.toString());
        GridPane.setMargin(mScaleBarCheckBox, topInsets);

        mControlsCheckBox = new CheckBox(MDict.VIEW_CONTROLS.toString());
        GridPane.setMargin(mControlsCheckBox, topInsets);

        mCompassCheckBox = new CheckBox(MDict.COMPASS.toString());
        GridPane.setMargin(mCompassCheckBox, topInsets);

        mStarsCheckBox = new CheckBox(MDict.STARS.toString());
        GridPane.setMargin(mStarsCheckBox, topInsets);

        mAtmosphereCheckBox = new CheckBox(MDict.ATMOSPHERE.toString());
        GridPane.setMargin(mAtmosphereCheckBox, topInsets);

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
                mScaleBarCheckBox,
                mStarsCheckBox,
                mAtmosphereCheckBox
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

        mAtmosphereCheckBox.setOnAction((event) -> {
            mOptions.setDisplayAtmosphere(mAtmosphereCheckBox.isSelected());
        });

        mStarsCheckBox.setOnAction((event) -> {
            mOptions.setDisplayStars(mStarsCheckBox.isSelected());
        });

        mElevationCheckBox = new CheckBox(MDict.ELEVATION.toString());
        mElevationCheckBox.setOnAction((event) -> {
            mOptions.setMapElevation(mElevationCheckBox.isSelected());
        });

        mOpacityBox = new VBox(new Label(Dict.OPACITY.toString()), mOpacitySlider, mElevationCheckBox);

        getChildren().addAll(
                mLeftPane,
                new Separator(Orientation.VERTICAL),
                mStyleBox
        );
    }

    private void initListeners() {
        mOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.setMapOpacity(mOpacitySlider.getValue());
        });
    }

    private void initStyle() {
        Platform.runLater(() -> {
            ToggleGroup group = new ToggleGroup();

            mStyleBox.getChildren().clear();
            ArrayList< MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
            Collections.sort(styles, (MapStyle o1, MapStyle o2) -> o1.getName().compareTo(o2.getName()));

            for (MapStyle mapStyle : styles) {
                ToggleButton button = new ToggleButton(mapStyle.getName());
                button.prefWidthProperty().bind(widthProperty());
                button.setToggleGroup(group);
                button.setOnAction((ActionEvent event) -> {
                    mOptions.setMapStyle(mapStyle.getName());
                });

                if (mapStyle.getSuppliers() != null) {
                    button.setTooltip(new Tooltip(String.format("%s %s", Dict.FROM.toString().toLowerCase(), mapStyle.getSuppliers())));
                }

                mStyleBox.getChildren().add(button);
            }

            mStyleBox.getChildren().add(mOpacityBox);
        });
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
        mAtmosphereCheckBox.setSelected(mOptions.isDisplayAtmosphere());
        mStarsCheckBox.setSelected(mOptions.isDisplayStar());

        mOpacitySlider.setValue(mOptions.getMapOpacity());
        mElevationCheckBox.setSelected(mOptions.isMapElevation());
    }
}
