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
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE_PREV;
import org.mapton.worldwind.api.MapStyle;
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
        FxHelper.runLaterDelayed(1000, () -> mManager.refresh());
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
                    mOptions.put(KEY_MAP_STYLE_PREV, mOptions.get(KEY_MAP_STYLE));
                    mOptions.put(KEY_MAP_STYLE, selectionModel.getSelectedItem().getId());
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
            var button = new Button(Integer.toString(i + 1));
            button.prefWidthProperty().bind(widthProperty());
            button.setPadding(FxHelper.getUIScaledInsets(3));

            mSpeedDialDynamic.getChildren().add(button);
        }
    }

    private void populateSpeedDialFixed() {
        FxHelper.runLater(() -> {
            mSpeedDialFixed.getChildren().clear();
            new ArrayList<>(mManager.getAllItems()).stream()
                    .filter(mapStyle -> StringUtils.isBlank(mapStyle.getCategory()))
                    .forEachOrdered(mapStyle -> {
                        var button = new Button(mapStyle.getName());
                        button.prefWidthProperty().bind(widthProperty());
                        button.setPadding(FxHelper.getUIScaledInsets(3));
                        button.setOnAction(actionEvent -> {
                            mListView.getSelectionModel().clearSelection();
                            mOptions.put(KEY_MAP_STYLE_PREV, mOptions.get(KEY_MAP_STYLE));
                            mOptions.put(KEY_MAP_STYLE, mapStyle.getId());
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
}
