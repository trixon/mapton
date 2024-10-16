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
package org.mapton.core.ui.poi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Separator;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MFilterPopOver;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class PoisView extends BorderPane {

    private MenuItem mBrowseMenuItem;
    private Menu mContextCopyMenu;
    private ContextMenu mContextMenu;
    private EventHandler<MouseEvent> mContextMenuMouseEvent;
    private Menu mContextOpenMenu;
    private FilterPopOver mFilterPopOver;
    private TextField mFilterTextField;
    private Label mItemCountLabel;
    private ListView<MPoi> mListView;
    private final MPoiManager mManager = MPoiManager.getInstance();

    public PoisView() {
        createUI();
        initListeners();
        populateContextProviders();

        FxHelper.runLaterDelayed(1000, () -> {
            mManager.refresh();
        });
    }

    private void createUI() {
        mFilterPopOver = new FilterPopOver();

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText("%s %s".formatted(Dict.SEARCH.toString(), MDict.POI.toString()));
        mFilterTextField.setMinWidth(20);

        mListView = new ListView<>();
        mListView.itemsProperty().bind(mManager.timeFilteredItemsProperty());
        mListView.setCellFactory(param -> new PoiListCell());

        var refreshAction = new Action(Dict.REFRESH.toString(), event -> {
            mManager.refresh();
        });
        refreshAction.setGraphic(MaterialIcon._Navigation.REFRESH.getImageView(getIconSizeToolBarInt()));

        ArrayList<Action> actions = new ArrayList<>();
        actions.add(refreshAction);
        actions.add(mFilterPopOver.getAction());

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        var topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(toolBar);
        toolBar.setMinWidth(getIconSizeToolBarInt() * 2 * 1.6);

        FxHelper.slimToolBar(toolBar);

        mItemCountLabel = new Label();
        mItemCountLabel.setAlignment(Pos.BASELINE_RIGHT);
        setTop(topBorderPane);
        setCenter(mListView);
        setBottom(mItemCountLabel);

        mItemCountLabel.prefWidthProperty().bind(widthProperty());
        mBrowseMenuItem = new MenuItem(Dict.OPEN_IN_WEB_BROWSER.toString());
        mContextMenu = new ContextMenu(
                mBrowseMenuItem,
                new SeparatorMenuItem(),
                mContextCopyMenu = new Menu(MDict.COPY_LOCATION.toString()),
                mContextOpenMenu = new Menu(MDict.OPEN_LOCATION.toString())
        );
    }

    private void initListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            mManager.refresh(newValue);
        });

        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends MPoi> c) -> {
            Platform.runLater(() -> {
                mItemCountLabel.setText("%d/%d".formatted(
                        mManager.getTimeFilteredItems().size(),
                        mManager.getAllItems().size()
                ));
                mManager.restoreSelection();
            });
        });

        Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener(lookupEvent -> {
            populateContextProviders();
        });

        mContextMenuMouseEvent = mouseEvent -> {
            getScene().getWindow().requestFocus();
            mListView.requestFocus();
            MPoi poi = mListView.getSelectionModel().getSelectedItem();

            if (poi != null) {
                Mapton.getEngine().setLockedLatitude(poi.getLatitude());
                Mapton.getEngine().setLockedLongitude(poi.getLongitude());
                if (mouseEvent.isSecondaryButtonDown()) {
                    Mapton.getEngine().setLatitude(poi.getLatitude());
                    Mapton.getEngine().setLongitude(poi.getLongitude());
                    mBrowseMenuItem.setDisable(StringUtils.isBlank(poi.getUrl()));
                    mContextMenu.show(this, mouseEvent.getScreenX(), mouseEvent.getScreenY());
                } else if (mouseEvent.isPrimaryButtonDown()) {
                    mContextMenu.hide();
                }
            }
        };

        mListView.setOnMousePressed(mContextMenuMouseEvent);
        mListView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                var poi = mListView.getSelectionModel().getSelectedItem();
                if (poi != null) {
                    Mapton.getEngine().panTo(poi.getLatLon(), poi.getZoom());
                }
            }
        });

        mListView.getSelectionModel().selectedItemProperty().addListener((ov, oldPoi, newPoi) -> {
            mManager.setSelectedItem(newPoi);
        });

        mManager.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (mListView.getSelectionModel().getSelectedItem() != newValue) {
                mListView.getSelectionModel().select(newValue);
                mListView.getFocusModel().focus(mListView.getItems().indexOf(newValue));
                FxHelper.scrollToItemIfNotVisible(mListView, newValue);
            }
        });

        mBrowseMenuItem.setOnAction(actionEvent -> {
            SystemHelper.desktopBrowse(mListView.getSelectionModel().getSelectedItem().getUrl());
        });
    }

    private synchronized void populateContextProviders() {
        mContextCopyMenu.getItems().clear();
        mContextOpenMenu.getItems().clear();

        for (var provider : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
            var menuItem = new MenuItem(provider.getName());
            switch (provider.getType()) {
                case COPY -> {
                    mContextCopyMenu.getItems().add(menuItem);
                    menuItem.setOnAction(actionEvent -> {
                        String s = provider.getUrl();
                        Mapton.getLog().v("Open location", s);
                        SystemHelper.copyToClipboard(s);
                    });
                }

                case OPEN -> {
                    mContextOpenMenu.getItems().add(menuItem);
                    menuItem.setOnAction(actionEvent -> {
                        String s = provider.getUrl();
                        Mapton.getLog().v("Copy location", s);
                        SystemHelper.desktopBrowse(s);
                    });
                }
            }
        }

        mContextCopyMenu.getItems().sorted((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
        mContextCopyMenu.setVisible(!mContextCopyMenu.getItems().isEmpty());

        mContextOpenMenu.getItems().sorted((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
        mContextOpenMenu.setVisible(!mContextOpenMenu.getItems().isEmpty());
    }

    public class FilterPopOver extends MFilterPopOver {

        private PoiCategoryCheckTreeView mCategoryCheckTreeView;

        public FilterPopOver() {
            createUI();
        }

        @Override
        public void clear() {
            mCategoryCheckTreeView.getCheckModel().clearChecks();
            setUsePolygonFilter(false);
        }

        @Override
        public void onPolygonFilterChange() {
            mManager.refresh();
        }

        @Override
        public void reset() {
            mCategoryCheckTreeView.getCheckModel().checkAll();
            setUsePolygonFilter(false);
        }

        private void createUI() {
            mCategoryCheckTreeView = new PoiCategoryCheckTreeView();
            var vBox = new VBox(GAP,
                    getButtonBox(),
                    new Separator(),
                    mCategoryCheckTreeView
            );

            autoSize(vBox);
            setContentNode(vBox);

            mManager.polygonFilterProperty().bind(usePolygonFilterProperty());
        }
    }

    class PoiListCell extends ListCell<MPoi> {

        private final Label mDesc1Label = new Label();
        private final Label mNameLabel = new Label();
        private final String mStyleBold = "-fx-font-weight: bold;";
        private VBox mVBox;

        public PoiListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MPoi poi, boolean empty) {
            super.updateItem(poi, empty);
            if (poi == null || empty) {
                clearContent();
            } else {
                addContent(poi);
            }
        }

        private void addContent(MPoi poi) {
            setText(null);

            mNameLabel.setText(poi.getName());
            mDesc1Label.setText("%s: %s".formatted(poi.getProvider(), poi.getCategory()));

            var rows = new LinkedHashMap<String, String>();
            rows.put(Dict.NAME.toString(), StringUtils.defaultString(poi.getName()));
            rows.put(Dict.CATEGORY.toString(), StringUtils.defaultString(poi.getCategory()));
            rows.put(Dict.SOURCE.toString(), StringUtils.defaultString(poi.getProvider()));
            rows.put(Dict.DESCRIPTION.toString(), StringUtils.defaultString(poi.getDescription()));
            rows.put(Dict.TAGS.toString(), StringUtils.defaultString(poi.getTags()));
            rows.put("URL", StringUtils.defaultString(poi.getUrl()));

            int length = 0;
            for (var string : rows.keySet()) {
                length = Math.max(length, string.length());
            }

            var sb = new StringBuilder();
            for (var entry : rows.entrySet()) {
                sb.append(StringUtils.rightPad(entry.getKey(), length, " ")).append(" : ").append(entry.getValue()).append("\n");
            }

            var tooltip = new Tooltip(sb.toString());
            tooltip.setFont(Font.font("monospaced"));
            setTooltip(tooltip);

            setGraphic(mVBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            mNameLabel.setStyle(mStyleBold);

            mVBox = new VBox(
                    mNameLabel,
                    mDesc1Label
            );
        }
    }
}
