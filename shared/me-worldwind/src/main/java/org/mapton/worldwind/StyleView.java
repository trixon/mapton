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
package org.mapton.worldwind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import static org.mapton.worldwind.ModuleOptions.*;
import org.mapton.worldwind.api.MapStyle;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class StyleView extends HBox {

    private CheckBox mAtmosphereCheckBox;
    private CheckBox mCompassCheckBox;
    private CheckBox mControlsCheckBox;
    private CheckBox mElevationCheckBox;
    private CheckBox mMaskCheckBox;
    private final GridPane mLeftPane = new GridPane();
    private RadioButton mModeFlatRadioButton;
    private RadioButton mModeGlobeRadioButton;
    private VBox mMapOpacityBox;
    private VBox mMaskOpacityBox;
    private final Slider mMapOpacitySlider = new Slider(0, 1, 1);
    private final Slider mMaskOpacitySlider = new Slider(0, 1, 1);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private CheckBox mPlaceNameCheckBox;
    private ComboBox<String> mProjComboBox;
    private final ArrayList<String> mProjections = new ArrayList<>();
    private CheckBox mScaleBarCheckBox;
    private CheckBox mStarsCheckBox;
    private final VBox mStyleBox = new VBox(16);
    private CheckBox mWorldMapCheckBox;
    private ColorPicker mMaskColorPicker;

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
        setSpacing(FxHelper.getUIScaled(16));
        setPadding(FxHelper.getUIScaledInsets(8, 16, 16, 16));
        double width = FxHelper.getUIScaled(200);
        mLeftPane.setPrefWidth(width);
        mStyleBox.setPrefWidth(width);

        Insets topInsets = FxHelper.getUIScaledInsets(12, 0, 0, 0);
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

        mProjComboBox = new ComboBox<>();

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

        mPlaceNameCheckBox = new CheckBox(Dict.PLACE_NAMES.toString());
        GridPane.setMargin(mPlaceNameCheckBox, topInsets);

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
        //mPlaceNameCheckBox
        );

        mModeFlatRadioButton.setMaxWidth(Double.MAX_VALUE);
        mModeGlobeRadioButton.setMaxWidth(Double.MAX_VALUE);
        mProjComboBox.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(mProjComboBox, Priority.ALWAYS);
        GridPane.setFillWidth(mProjComboBox, true);
        mProjComboBox.disableProperty().bind(mModeGlobeRadioButton.selectedProperty());
        mModeGlobeRadioButton.setOnAction((event -> {
            mOptions.put(KEY_MAP_GLOBE, true);
        }));
        mModeFlatRadioButton.setOnAction((event -> {
            mOptions.put(KEY_MAP_GLOBE, false);
        }));

        mProjComboBox.setOnAction((event -> {
            mOptions.put(KEY_MAP_PROJECTION, mProjComboBox.getSelectionModel().getSelectedIndex());

        }));

        mWorldMapCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_WORLD_MAP, mWorldMapCheckBox.isSelected());
        });

        mScaleBarCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_SCALE_BAR, mScaleBarCheckBox.isSelected());
        });

        mControlsCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_CONTROLS, mControlsCheckBox.isSelected());
        });

        mCompassCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_COMPASS, mCompassCheckBox.isSelected());
        });

        mAtmosphereCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_ATMOSPHERE, mAtmosphereCheckBox.isSelected());
        });

        mStarsCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_STARS, mStarsCheckBox.isSelected());
        });

        mPlaceNameCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_PLACE_NAMES, mPlaceNameCheckBox.isSelected());
        });

        mElevationCheckBox = new CheckBox(MDict.ELEVATION.toString());
        mElevationCheckBox.setOnAction(event -> {
            mOptions.put(KEY_MAP_ELEVATION, mElevationCheckBox.isSelected());
        });
        mElevationCheckBox.setVisible(false);

        mMaskCheckBox = new CheckBox(Dict.MASK.toString());
        mMaskCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_MASK, mMaskCheckBox.isSelected());
        });
        mMaskColorPicker = new ColorPicker();
        mMaskColorPicker.prefWidthProperty().bind(widthProperty());
        mMaskColorPicker.disableProperty().bind(mMaskCheckBox.selectedProperty().not());
        mMaskOpacitySlider.disableProperty().bind(mMaskCheckBox.selectedProperty().not());

        Color color = FxHelper.colorFromHexRGBA(mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR));
        mMaskColorPicker.setValue(color);

        mMapOpacityBox = new VBox(new Label(Dict.OPACITY.toString()), mMapOpacitySlider, mElevationCheckBox);
        mMaskOpacityBox = new VBox(FxHelper.getUIScaled(8), mMaskCheckBox, mMaskOpacitySlider, mMaskColorPicker);

        getChildren().addAll(
                mLeftPane,
                new Separator(Orientation.VERTICAL),
                mStyleBox
        );
    }

    private void initListeners() {
        mMapOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.put(KEY_MAP_OPACITY, mMapOpacitySlider.getValue());
        });

        mMaskOpacitySlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            mOptions.put(KEY_MASK_OPACITY, mMaskOpacitySlider.getValue());
        });

        mMaskColorPicker.valueProperty().addListener((ObservableValue<? extends Color> ov, Color t, Color t1) -> {
            mOptions.put(KEY_MASK_COLOR, FxHelper.colorToHexRGB(t1));
        });

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            initStyle();
        }, MKey.DATA_SOURCES_WMS_STYLES);
    }

    private void initStyle() {
        Platform.runLater(() -> {
            mStyleBox.getChildren().clear();

            ArrayList<MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
            ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);

            if (wmsStyles != null) {
                for (MWmsStyle wmsStyle : wmsStyles) {
                    styles.add(MapStyle.createFromWmsStyle(wmsStyle));
                }
            }

            Collections.sort(styles, (MapStyle o1, MapStyle o2) -> o1.getName().compareTo(o2.getName()));
            TreeMap<String, ObservableList<MapStyle>> categoryStyles = new TreeMap<>();
            for (MapStyle mapStyle : styles) {
                if (StringUtils.isBlank(mapStyle.getCategory())) {
                    Button button = new Button(mapStyle.getName());
                    button.prefWidthProperty().bind(widthProperty());
                    button.setOnAction((ActionEvent event) -> {
                        mOptions.put(KEY_MAP_STYLE, mapStyle.getId());
                    });

                    if (mapStyle.getSuppliers() != null) {
                        button.setTooltip(new Tooltip(mapStyle.getDescription()));
                    }

                    mStyleBox.getChildren().add(button);
                } else {
                    categoryStyles.computeIfAbsent(mapStyle.getCategory(), k -> FXCollections.observableArrayList()).add(mapStyle);
                }
            }

            mStyleBox.getChildren().add(new Separator());

            for (String category : categoryStyles.keySet()) {
                ListView<MapStyle> listView = new ListView<>(categoryStyles.get(category));
                listView.setPrefWidth(FxHelper.getUIScaled(250));
                listView.setCellFactory((ListView<MapStyle> param) -> new MapStyleListCell());
                listView.parentProperty().addListener((ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) -> {
                    Region region = (Region) newValue;
                    region.setPadding(Insets.EMPTY);
                    region.setBorder(Border.EMPTY);
                });

                MultipleSelectionModel<MapStyle> selectionModel = listView.getSelectionModel();
                selectionModel.getSelectedItems().addListener((ListChangeListener.Change<? extends MapStyle> change) -> {
                    change.next();
                    if (change.wasAdded() || change.wasReplaced()) {
                        try {
                            mOptions.put(KEY_MAP_STYLE, selectionModel.getSelectedItem().getId());
                        } catch (Exception e) {
                        }
                    }
                });

                MenuButton menuButton = new MenuButton(category);
                menuButton.setPopupSide(Side.RIGHT);
                CustomMenuItem menuItem = new CustomMenuItem(listView);
                menuButton.setOnShowing(event -> {
                    listView.getSelectionModel().clearSelection();
                });
                menuButton.setOnShown(event -> {
                    listView.requestFocus();
                    for (MapStyle item : listView.getItems()) {
                        if (item.getId().equalsIgnoreCase(mOptions.get(KEY_MAP_STYLE))) {
                            listView.getSelectionModel().select(item);
                            break;
                        }
                    }

                });
                menuItem.setHideOnClick(false);
                menuButton.getItems().add(menuItem);
                menuButton.prefWidthProperty().bind(widthProperty());

                mStyleBox.getChildren().add(menuButton);
            }

            mStyleBox.getChildren().addAll(new Separator(), mMapOpacityBox, mMaskOpacityBox);
        });
    }

    private void load() {
        if (mOptions.is(KEY_MAP_GLOBE, DEFAULT_MAP_GLOBE)) {
            mModeGlobeRadioButton.setSelected(true);
        } else {
            mModeFlatRadioButton.setSelected(true);
        }

        mProjComboBox.getSelectionModel().select(mOptions.getInt(KEY_MAP_PROJECTION, DEFAULT_MAP_PROJECTION));
        mWorldMapCheckBox.setSelected(mOptions.is(KEY_DISPLAY_WORLD_MAP, DEFAULT_DISPLAY_WORLD_MAP));
        mScaleBarCheckBox.setSelected(mOptions.is(KEY_DISPLAY_SCALE_BAR, DEFAULT_DISPLAY_SCALE_BAR));
        mControlsCheckBox.setSelected(mOptions.is(KEY_DISPLAY_CONTROLS, DEFAULT_DISPLAY_CONTROLS));
        mCompassCheckBox.setSelected(mOptions.is(KEY_DISPLAY_COMPASS, DEFAULT_DISPLAY_COMPASS));
        mAtmosphereCheckBox.setSelected(mOptions.is(KEY_DISPLAY_ATMOSPHERE, DEFAULT_DISPLAY_ATMOSPHERE));
        mStarsCheckBox.setSelected(mOptions.is(KEY_DISPLAY_STARS, DEFAULT_DISPLAY_STARS));
        mPlaceNameCheckBox.setSelected(mOptions.is(KEY_DISPLAY_PLACE_NAMES, DEFAULT_DISPLAY_PLACE_NAMES));

        mMapOpacitySlider.setValue(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
        mMaskOpacitySlider.setValue(mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY));
        mMaskCheckBox.setSelected(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));
        mElevationCheckBox.setSelected(mOptions.is(KEY_MAP_ELEVATION, DEFAULT_MAP_ELEVATION));
    }

    class MapStyleListCell extends ListCell<MapStyle> {

        private final VBox mBox = new VBox();
        private final Label mDescLabel = new Label();
        private final Label mNameLabel = new Label();

        public MapStyleListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MapStyle mapStyle, boolean empty) {
            super.updateItem(mapStyle, empty);

            if (mapStyle == null || empty) {
                clearContent();
            } else {
                addContent(mapStyle);
            }
        }

        private void addContent(MapStyle mapStyle) {
            setText(null);

            mNameLabel.setText(mapStyle.getName());
            mDescLabel.setText(mapStyle.getDescription());

            setGraphic(mBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = Font.getDefault().getFamily();
            double fontSize = FxHelper.getScaledFontSize();
            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize));
            mDescLabel.setFont(Font.font(fontFamily, FontPosture.ITALIC, fontSize));

            mBox.setSpacing(FxHelper.getUIScaled(4));
            mBox.getChildren().setAll(mNameLabel, mDescLabel);
        }
    }
}
