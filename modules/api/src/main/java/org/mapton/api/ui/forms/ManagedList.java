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

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.Mapton;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.ListItemCountLabel;

/**
 *
 * @author Patrik Karlström
 * @param <ManagerType>
 * @param <ItemType>
 */
public class ManagedList<ManagerType extends MBaseDataManager, ItemType> {

    private static Object sLastOfAnyObject;
    private long mLastSelection;
    private final ListView<ItemType> mListView = new ListView<>();
    private final MBaseDataManager mManager;
    private final BorderPane mRoot = new BorderPane();
    private final ListItemCountLabel mListItemCountLabel = new ListItemCountLabel();

    public ManagedList(MBaseDataManager manager) {
        mManager = manager;

        createUI();
        initListeners();
    }

    public ListView<ItemType> getListView() {
        return mListView;
    }

    public Pane getView() {
        return mRoot;
    }

    private void createUI() {
        mRoot.setCenter(mListView);
        mRoot.setBottom(mListItemCountLabel);

        mListView.itemsProperty().bind(mManager.timeFilteredItemsProperty());
        mListItemCountLabel.prefWidthProperty().bind(mRoot.widthProperty());
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
