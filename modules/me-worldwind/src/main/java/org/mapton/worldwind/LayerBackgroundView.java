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
import java.util.Arrays;
import java.util.Collections;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MKey;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MOptionsPopOver;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE;
import static org.mapton.worldwind.ModuleOptions.KEY_MAP_STYLE_PREV;
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

    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final VBox mSpeedDial = new VBox(16);
    private TextField mFilterTextField;
    private Action mOptionsAction;
    private MOptionsPopOver mOptionsPopOver;
    private ToolBar mToolBar;
    private ListView<MapStyle> listView = new ListView<>();
    private BorderPane borderPane = new BorderPane();

    public static LayerBackgroundView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerBackgroundView() {
        createUI();
        initListeners();

        Lookup.getDefault().lookupResult(MapStyle.class).addLookupListener(lookupEvent -> {
            initStyle();
        });

        initStyle();
    }

    private void createUI() {
        mOptionsPopOver = new MOptionsPopOver();
        mOptionsPopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        //mOptionsPopOver.setContentNode(new LayerOptionsView());

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.LAYER_SEARCH.toString());
        mFilterTextField.setMinWidth(20);
        final int iconSize = (int) (getIconSizeToolBarInt() * 0.8);

        mOptionsAction = mOptionsPopOver.getAction();
        var actions = Arrays.asList(
                //                selectActionGroup,
                mOptionsAction
        );
        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), iconSize);
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);
        var topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(mToolBar);
        mToolBar.setMinWidth(iconSize * 2.5);
        setTop(topBorderPane);
        setCenter(borderPane);
        borderPane.setTop(mSpeedDial);
        borderPane.setCenter(listView);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            initStyle();
        }, MKey.DATA_SOURCES_WMS_STYLES);
    }

    private void initStyle() {
        Platform.runLater(() -> {
            mSpeedDial.getChildren().clear();

            ArrayList<MapStyle> styles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
            ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);
            ArrayList<MapStyle> stylesList = new ArrayList<>();

            if (wmsStyles != null) {
                for (var wmsStyle : wmsStyles) {
                    styles.add(MapStyle.createFromWmsStyle(wmsStyle));
                }
            }

            Collections.sort(styles, (o1, o2) -> o1.getName().compareTo(o2.getName()));
//            var categoryStyles = new TreeMap<String, ObservableList<MapStyle>>();
            for (var mapStyle : styles) {
                if (StringUtils.isBlank(mapStyle.getCategory())) {
                    var button = new Button(mapStyle.getName());
                    button.prefWidthProperty().bind(widthProperty());
                    button.setOnAction(actionEvent -> {
                        mOptions.put(KEY_MAP_STYLE_PREV, mOptions.get(KEY_MAP_STYLE));
                        mOptions.put(KEY_MAP_STYLE, mapStyle.getId());
                    });

                    if (mapStyle.getSuppliers() != null) {
                        button.setTooltip(new Tooltip(mapStyle.getDescription()));
                    }

                    mSpeedDial.getChildren().add(button);
                } else {
//                    categoryStyles.computeIfAbsent(mapStyle.getCategory(), k -> FXCollections.observableArrayList()).add(mapStyle);
                    stylesList.add(mapStyle);
                }
            }

            mSpeedDial.getChildren().add(new Separator());

            listView.getItems().setAll(stylesList);
//            for (var category : categoryStyles.keySet()) {
//                listView.setPrefWidth(FxHelper.getUIScaled(250));
            listView.setCellFactory((ListView<MapStyle> param) -> new MapStyleListCell());
//                listView.parentProperty().addListener((ObservableValue<? extends Parent> observable, Parent oldValue, Parent newValue) -> {
//                    Region region = (Region) newValue;
//                    region.setPadding(Insets.EMPTY);
//                    region.setBorder(Border.EMPTY);
//                });

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

        }
        );
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
