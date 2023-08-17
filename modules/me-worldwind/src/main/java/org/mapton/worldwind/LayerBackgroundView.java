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
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MKey;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_DISPLAY_MASK;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MAP_OPACITY;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MASK_COLOR;
import static org.mapton.worldwind.ModuleOptions.DEFAULT_MASK_OPACITY;
import static org.mapton.worldwind.ModuleOptions.KEY_DISPLAY_MASK;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_OPACITY;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE_PREV;
import static org.mapton.worldwind.ModuleOptions.KEY_MASK_COLOR;
import static org.mapton.worldwind.ModuleOptions.KEY_MASK_OPACITY;
import org.mapton.worldwind.api.MapStyle;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LayerBackgroundView extends BorderPane {

    private VBox mMapOpacityBox;
    private final Slider mMapOpacitySlider = new Slider(0, 1, 1);
    private CheckBox mMaskCheckBox;
    private ColorPicker mMaskColorPicker;
    private VBox mMaskOpacityBox;
    private final Slider mMaskOpacitySlider = new Slider(0, 1, 1);
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final VBox mStyleBox = new VBox(16);

    public static LayerBackgroundView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerBackgroundView() {
        createUI();
        initListeners();
        load();

        Lookup.getDefault().lookupResult(MapStyle.class).addLookupListener(lookupEvent -> {
            initStyle();
        });

        initStyle();
    }

    private void createUI() {
        mMaskCheckBox = new CheckBox(Dict.MASK.toString());
        mMaskColorPicker = new ColorPicker();
        mMaskColorPicker.prefWidthProperty().bind(widthProperty());
        mMaskColorPicker.disableProperty().bind(mMaskCheckBox.selectedProperty().not());
        Color color = FxHelper.colorFromHexRGBA(mOptions.get(KEY_MASK_COLOR, DEFAULT_MASK_COLOR));
        mMaskColorPicker.setValue(color);

        mMapOpacityBox = new VBox(new Label(Dict.OPACITY.toString()), mMapOpacitySlider);
        mMaskOpacityBox = new VBox(FxHelper.getUIScaled(8), mMaskCheckBox, mMaskOpacitySlider, mMaskColorPicker);
        mMaskCheckBox.setOnAction(event -> {
            mOptions.put(KEY_DISPLAY_MASK, mMaskCheckBox.isSelected());
        });
        mMaskOpacitySlider.disableProperty().bind(mMaskCheckBox.selectedProperty().not());

        setCenter(mStyleBox);

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
                        mOptions.put(KEY_MAP_STYLE_PREV, mOptions.get(KEY_MAP_STYLE));
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
                            mOptions.put(KEY_MAP_STYLE_PREV, mOptions.get(KEY_MAP_STYLE));
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
        mMapOpacitySlider.setValue(mOptions.getDouble(KEY_MAP_OPACITY, DEFAULT_MAP_OPACITY));
        mMaskOpacitySlider.setValue(mOptions.getFloat(KEY_MASK_OPACITY, DEFAULT_MASK_OPACITY));
        mMaskCheckBox.setSelected(mOptions.is(KEY_DISPLAY_MASK, DEFAULT_DISPLAY_MASK));
    }

    private static class Holder {

        private static final LayerBackgroundView INSTANCE = new LayerBackgroundView();
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
