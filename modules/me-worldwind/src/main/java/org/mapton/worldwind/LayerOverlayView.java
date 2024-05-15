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

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.ListActionView;
import org.controlsfx.control.ListSelectionView;
import org.mapton.worldwind.api.MapStyle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LayerOverlayView extends BorderPane {

    private final ListSelectionView<String> mListSelectionView = new ListSelectionView<>();
    private final LayerMapStyleManager mManager = LayerMapStyleManager.getInstance();

    public static LayerOverlayView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerOverlayView() {
        Platform.runLater(() -> {
            createUI();
            initListeners();
            populate();
        });
    }

    private ListActionView.ListAction[] createTaskTargetActions() {
        int imageSize = FxHelper.getUIScaled(16);

        return new ListActionView.ListAction[]{
            new ListActionView.ListAction<String>(MaterialIcon._Navigation.EXPAND_LESS.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<String> listView) {
                    setEventHandler(event -> moveSelectedTasksUp(listView));
                }
            },
            new ListActionView.ListAction<String>(MaterialIcon._Navigation.EXPAND_MORE.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<String> listView) {
                    setEventHandler(event -> moveSelectedTasksDown(listView));
                }
            }
        };
    }

    private void createUI() {
        mListSelectionView.getTargetActions().addAll(createTaskTargetActions());
//        mListSelectionView.setOrientation(Orientation.VERTICAL);
        setCenter(mListSelectionView);
    }

    private void initListeners() {
        mManager.allItemsProperty().addListener((ObservableValue<? extends ObservableList<MapStyle>> observable, ObservableList<MapStyle> oldValue, ObservableList<MapStyle> newValue) -> {
        });

        mListSelectionView.getTargetItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            save();
        });
    }

    private void moveSelectedTasksDown(ListView<String> listView) {
        var items = listView.getItems();
        var selectionModel = listView.getSelectionModel();
        var selectedIndices = selectionModel.getSelectedIndices();
        int lastIndex = items.size() - 1;

        for (int index = selectedIndices.size() - 1; index >= 0; index--) {
            var selectedIndex = selectedIndices.get(index);
            if (selectedIndex < lastIndex) {
                if (selectedIndices.contains(selectedIndex + 1)) {
                    continue;
                }
                var item = items.get(selectedIndex);
                var itemToBeReplaced = items.get(selectedIndex + 1);
                items.set(selectedIndex + 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex + 1);
            }
        }
    }

    private void moveSelectedTasksUp(ListView<String> listView) {
        var items = listView.getItems();
        var selectionModel = listView.getSelectionModel();
        var selectedIndices = selectionModel.getSelectedIndices();

        for (var selectedIndex : selectedIndices) {
            if (selectedIndex > 0) {
                if (selectedIndices.contains(selectedIndex - 1)) {
                    continue;
                }
                var item = items.get(selectedIndex);
                var itemToBeReplaced = items.get(selectedIndex - 1);
                items.set(selectedIndex - 1, item);
                items.set(selectedIndex, itemToBeReplaced);
                selectionModel.clearSelection(selectedIndex);
                selectionModel.select(selectedIndex - 1);
            }
        }
    }

    private void populate() {
        mListSelectionView.getSourceItems().clear();
        mListSelectionView.getTargetItems().clear();

        mListSelectionView.getSourceItems().addAll("A", "B", "C");
        mListSelectionView.getTargetItems().addAll("D");
    }

    private void save() {
        var orderedItems = mListSelectionView.getTargetItems().stream().map(s -> s).toList();
        System.out.println(String.join(", ", orderedItems));
    }

    private static class Holder {

        private static final LayerOverlayView INSTANCE = new LayerOverlayView();
    }
}
