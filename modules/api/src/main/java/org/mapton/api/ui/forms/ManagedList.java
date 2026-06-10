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
package org.mapton.api.ui.forms;

import com.dlsc.gemsfx.Spacer;
import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import org.controlsfx.control.PopOver;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarIntDouble;
import org.mapton.api.ui.MPopOver;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.ListItemCountLabel;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 * @param <ManagerType>
 * @param <ItemType>
 */
public class ManagedList<ManagerType extends MBaseDataManager, ItemType> {

    private static Object sLastOfAnyObject;
    private final Label mFooterLabel = new Label();
    private long mLastSelection;
    private final ListItemCountLabel mListItemCountLabel = new ListItemCountLabel();
    private final ListView<ItemType> mListView = new ListView<>();
    private final MBaseDataManager mManager;
    private final BorderPane mRoot = new BorderPane();

    public ManagedList(MBaseDataManager manager) {
        mManager = manager;

        createUI();
        initListeners();
    }

    public Label getFooterLabel() {
        return mFooterLabel;
    }

    public ListView<ItemType> getListView() {
        return mListView;
    }

    public Pane getView() {
        return mRoot;
    }

    private void createUI() {
        var popOver = new MPopOver();
        popOver.setTitle(Dict.OPTIONS.toString());
        popOver.setArrowLocation(PopOver.ArrowLocation.BOTTOM_CENTER);
        popOver.setContentNode(mManager.getOptionsView());
        int iconSize = (int) (0.5 * getIconSizeToolBarIntDouble());
        var settingsButton = new Button("", MaterialIcon._Action.SETTINGS.getImageView(iconSize));
        settingsButton.setOnAction(actionEvent -> {
            popOver.show(settingsButton);
        });
        settingsButton.setDisable(mManager.getOptionsView() == null);
        settingsButton.setTooltip(new Tooltip(Dict.OPTIONS.toString()));

        mFooterLabel.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, FxHelper.getUIScaled(4d)));
        var hbox = new HBox(settingsButton, mFooterLabel, new Spacer(), mListItemCountLabel);

        hbox.setAlignment(Pos.CENTER_LEFT);
        mRoot.setCenter(mListView);
        mRoot.setBottom(hbox);

        mListView.itemsProperty().bind(mManager.timeFilteredItemsProperty());
        hbox.prefWidthProperty().bind(mRoot.widthProperty());
        mListItemCountLabel.init(mListView, mManager.getTimeFilteredItems(), mManager.getAllItems());
    }

    private void initListeners() {
        mManager.getTimeFilteredItems().addListener((ListChangeListener.Change c) -> {
            Platform.runLater(() -> {
                mManager.restoreSelection();
            });
        });

        mListView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
            if (needsUpdate()) {
                mManager.setSelectedItem(n);
                if (KeyboardUtils.isPressed(KeyEvent.VK_SHIFT)) {
                    try {
                        Mapton.getEngine().panTo(mManager.getLatLonForItem(n));
                    } catch (NullPointerException e) {
                        //
                    }
                }
            }
        });

        mListView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 1) {
                refreshSelection();
            } else if (mouseEvent.getButton() == MouseButton.PRIMARY && mouseEvent.getClickCount() == 2) {
                refreshSelection();
                var item = mListView.getSelectionModel().getSelectedItem();

                try {
                    Mapton.getEngine().panTo(mManager.getLatLonForItem(item));
                } catch (NullPointerException e) {
                    //
                }
            }
        });

        mManager.selectedItemProperty().addListener((p, o, n) -> {
            var selectedItem = mListView.getSelectionModel().getSelectedItem();
            if (n != null && n != selectedItem) {
                mListView.getSelectionModel().select((ItemType) n);
                mListView.getFocusModel().focus(mListView.getItems().indexOf(n));
                FxHelper.scrollToItemIfNotVisible(mListView, n);
            }
        });
    }

    private boolean needsUpdate() {
        return SystemHelper.age(mLastSelection) > 100;
    }

    private void refreshSelection() {
        var selectedItem = mListView.getSelectionModel().getSelectedItem();
        if (sLastOfAnyObject != selectedItem && needsUpdate()) {
            mManager.setSelectedItem(null);
            mManager.setSelectedItem(selectedItem);
            mLastSelection = System.currentTimeMillis();
        }
    }
}
