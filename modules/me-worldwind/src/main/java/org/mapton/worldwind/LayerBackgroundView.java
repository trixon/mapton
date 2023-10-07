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
import java.util.prefs.Preferences;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioMenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.MapStyle;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LayerBackgroundView extends BorderPane {

    private final BorderPane mBorderPane = new BorderPane();
    private TextField mFilterTextField;
    private final ListView<MapStyle> mListView = new ListView<>();
    private final LayerMapStyleManager mManager = LayerMapStyleManager.getInstance();
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final HBox mSpeedDialDynamic = new HBox(FxHelper.getUIScaled(2));
    private final HBox mSpeedDialFixed = new HBox(FxHelper.getUIScaled(2));
    private final VBox mSpeedDial = new VBox(FxHelper.getUIScaled(2), mSpeedDialFixed, mSpeedDialDynamic);

    public static LayerBackgroundView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerBackgroundView() {
        createUI();
        initListeners();

        populate();

        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED, () -> {
            FxHelper.runLater(() -> {
                mManager.refresh();
            });
        });
    }

    private void activateStyle(String id) {
        mOptions.put(ModuleOptions.KEY_MAP_STYLE_PREV, mOptions.get(ModuleOptions.KEY_MAP_STYLE));
        mOptions.put(ModuleOptions.KEY_MAP_STYLE, id);
    }

    private void createUI() {
        mSpeedDial.setPadding(FxHelper.getUIScaledInsets(8));

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.SEARCH.toString());
        mFilterTextField.setMinWidth(20);

        mListView.itemsProperty().bind(mManager.timeFilteredItemsProperty());
        mListView.setCellFactory(listView -> new MapStyleListCell());

        setTop(mSpeedDial);
        setCenter(mBorderPane);
        mBorderPane.setTop(mFilterTextField);
        mBorderPane.setCenter(mListView);
    }

    private void initListeners() {
        mFilterTextField.textProperty().addListener((p, o, n) -> {
            mManager.refresh(n);
        });

        mManager.getAllItems().addListener((ListChangeListener.Change<? extends MapStyle> c) -> {
            populateSpeedDialFixed();
        });

        var selectionModel = mListView.getSelectionModel();
        selectionModel.selectedItemProperty().addListener((p, o, n) -> {
            mManager.setSelectedItem(n);
        });

        mManager.selectedItemProperty().addListener((p, o, n) -> {
            if (mListView.getSelectionModel().getSelectedItem() != n) {
                mListView.getSelectionModel().select(n);
                mListView.getFocusModel().focus(mListView.getItems().indexOf(n));
                FxHelper.scrollToItemIfNotVisible(mListView, n);
            }
        });

        selectionModel.getSelectedItems().addListener((ListChangeListener.Change<? extends MapStyle> change) -> {
            change.next();
            if (change.wasAdded() || change.wasReplaced()) {
                try {
                    activateStyle(selectionModel.getSelectedItem().getId());
                } catch (Exception e) {
                }
            }
        });
    }

    private void populate() {
        populateSpeedDialFixed();
        populateSpeedDialDynamic();
    }

    private void populateSpeedDialDynamic() {
        for (int i = 0; i < 6; i++) {
            var speedDialButton = new SpeedDialButton(i);
            var pane = speedDialButton.getRoot();
            pane.prefWidthProperty().bind(widthProperty());
            pane.setPadding(FxHelper.getUIScaledInsets(3));

            mSpeedDialDynamic.getChildren().add(pane);
        }
    }

    private void populateSpeedDialFixed() {
        FxHelper.runLater(() -> {
            mSpeedDialFixed.getChildren().clear();
            new ArrayList<>(mManager.getAllItems()).stream()
                    .filter(mapStyle -> StringUtils.isBlank(mapStyle.getCategory()))
                    .forEachOrdered(mapStyle -> {
                        var name = StringUtils.replaceEach(mapStyle.getName(),
                                new String[]{"OpenStreetMap"},
                                new String[]{"OSM"});
                        var button = new Button(name);
                        button.prefWidthProperty().bind(widthProperty());
                        button.setPadding(FxHelper.getUIScaledInsets(3));
                        button.setOnAction(actionEvent -> {
                            mListView.getSelectionModel().clearSelection();
                            activateStyle(mapStyle.getId());
                        });

                        if (mapStyle.getSuppliers() != null) {
                            button.setTooltip(new Tooltip(mapStyle.getDescription()));
                        }

                        mSpeedDialFixed.getChildren().add(button);
                    });
        });
    }

    private static class Holder {

        private static final LayerBackgroundView INSTANCE = new LayerBackgroundView();
    }

    class MapStyleListCell extends ListCell<MapStyle> {

        private final VBox mBox = new VBox();
        private final Label mCategoryLabel = new Label();
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
            mCategoryLabel.setText(mapStyle.getCategory());
            setGraphic(mBox);
            if (StringUtils.isNotBlank(mapStyle.getDescription())) {
                setTooltip(new Tooltip(mapStyle.getDescription()));
            }
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = Font.getDefault().getFamily();
            double fontSize = FxHelper.getScaledFontSize();

            mCategoryLabel.setFont(Font.font(fontFamily, FontWeight.THIN, fontSize));
            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize));

            mBox.setSpacing(FxHelper.getUIScaled(2));
            mBox.getChildren().setAll(
                    mNameLabel,
                    mCategoryLabel
            );
        }
    }

    private class SpeedDialButton {

        private final Button mButton = new Button();
        private final int mIndex;
        private MapStyle mMapStyle;
        private final Label mNameLabel = new Label();
        private final Preferences mPreferences = NbPreferences.forModule(SpeedDialButton.class).node("speedDial");
        private MenuItem mResetMenuItem;
        private final StackPane mRoot = new StackPane();

        public SpeedDialButton(int index) {
            mIndex = index;
            createUI();
            initListeners();
            initContextMenu();

            load();
        }

        public Button getButton() {
            return mButton;
        }

        public Pane getRoot() {
            return mRoot;
        }

        private void createUI() {
            var internalBox = new VBox();
            internalBox.getChildren().setAll(mNameLabel);
            internalBox.setAlignment(Pos.CENTER);
            mButton.setGraphic(internalBox);
            mRoot.getChildren().add(mButton);

            FxHelper.autoSizeRegionHorizontal(mButton);
            FxHelper.autoSizeRegionVertical(mButton);

            mNameLabel.setStyle("-fx-font-size: 1.2em;-fx-font-weight: bolder;");
        }

        private String getKey() {
            return "button_%d".formatted(mIndex);
        }

        private void initContextMenu() {
            var contextMenu = new ContextMenu();

            mResetMenuItem = new MenuItem(Dict.RESET.toString());
            mResetMenuItem.setOnAction(actionEvent -> {
                mPreferences.remove(getKey());
                load();
            });

            contextMenu.getItems().setAll(mResetMenuItem);

            if (!mManager.getAllItems().isEmpty()) {
                contextMenu.getItems().add(new SeparatorMenuItem());
                var toggleGroup = new ToggleGroup();
                mManager.getAllItems().stream()
                        .filter(s -> StringUtils.isNotBlank(s.getCategory()))
                        .forEachOrdered(s -> {
                            var menuItem = new RadioMenuItem(s.getName());
                            menuItem.setToggleGroup(toggleGroup);
                            menuItem.setSelected(StringUtils.equalsIgnoreCase(mPreferences.get(getKey(), null), s.getId()));
                            menuItem.setOnAction(actionEvent -> {
                                mPreferences.put(getKey(), s.getId());
                                load();
                            });

                            contextMenu.getItems().add(menuItem);
                        });
            }
            mButton.setContextMenu(contextMenu);
            mRoot.setOnMousePressed(mouseEvent -> {
                contextMenu.show(mRoot, mouseEvent.getScreenX(), mouseEvent.getScreenY());
            });

            mResetMenuItem.disableProperty().bind(mButton.disableProperty());
        }

        private void initListeners() {
            mManager.getAllItems().addListener((ListChangeListener.Change<? extends MapStyle> c) -> {
                initContextMenu();
                load();
            });

            mButton.setOnAction(actionEvent -> {
                mListView.getSelectionModel().clearSelection();
                activateStyle(mMapStyle.getId());
            });
        }

        private void load() {
            var styleId = mPreferences.get(getKey(), null);
            mMapStyle = mManager.getById(styleId);
            var disabled = mMapStyle == null;

            mButton.setDisable(disabled);

            if (disabled) {
                mButton.setTooltip(null);
                FxHelper.clearLabel(mNameLabel);
            } else {
                mButton.setTooltip(new Tooltip("%s\r%s".formatted(mMapStyle.getName(), mMapStyle.getDescription())));
                mNameLabel.setText(Integer.toString(mIndex + 1));
            }
        }
    }
}
