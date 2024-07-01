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
import java.util.TreeSet;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Orientation;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.ListActionView;
import org.mapton.api.MDict;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.SwappedListSelectionView;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LayerOverlayView extends BorderPane {

    private final SwappedListSelectionView<String> mListSelectionView = new SwappedListSelectionView<>();
    private final ModuleOptions mOptions = ModuleOptions.getInstance();
    private final OverlayManager mOverlayManager = OverlayManager.getInstance();
    private boolean mPopulated;

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

    private ListActionView.ListAction[] createTargetActions() {
        int imageSize = FxHelper.getUIScaled(16);

        return new ListActionView.ListAction[]{
            new ListActionView.ListAction<String>(MaterialIcon._Navigation.EXPAND_LESS.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<String> listView) {
                    setEventHandler(event -> moveSelectedUp(listView));
                }
            },
            new ListActionView.ListAction<String>(MaterialIcon._Navigation.EXPAND_MORE.getImageView(imageSize)) {
                @Override
                public void initialize(ListView<String> listView) {
                    setEventHandler(event -> moveSelectedDown(listView));
                }
            }
        };
    }

    private void createUI() {
        mListSelectionView.setSwappedSourceHeader(new Label("%s %s".formatted(Dict.AVAILABLE.toString(), MDict.OVERLAYS.toLower())));
        mListSelectionView.setSwappedTargetHeader(new Label("%s %s".formatted(Dict.SELECTED.toString(), MDict.OVERLAYS.toLower())));
        mListSelectionView.getSwappedTargetActions().addAll(createTargetActions());
        mListSelectionView.setOrientation(Orientation.VERTICAL);
        setCenter(mListSelectionView);
    }

    private void initListeners() {
        mOverlayManager.getAvailableOverlays().addListener((ListChangeListener.Change<? extends String> c) -> {
            FxHelper.runLater(() -> populate());
        });

        mListSelectionView.getSwappedTargetItems().addListener((ListChangeListener.Change<? extends String> c) -> {
            if (mPopulated) {
                var overlays = mListSelectionView.getSwappedTargetItems().stream().map(s -> s).toList();
                mOptions.put(ModuleOptions.KEY_MAP_OVERLAYS, String.join(",", overlays));
            }
        });
    }

    private void moveSelectedDown(ListView<String> listView) {
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

    private void moveSelectedUp(ListView<String> listView) {
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
        var availableOverlays = new TreeSet<>(mOverlayManager.getAvailableOverlays());
        mListSelectionView.getSwappedSourceItems().setAll(availableOverlays);
        var storedOverlaysItems = StringUtils.split(mOptions.get(ModuleOptions.KEY_MAP_OVERLAYS, ""), ",");
        var storedOverlays = new ArrayList<>(Arrays.asList(storedOverlaysItems));
        var selectedButNotAvailableOverlays = new ArrayList<String>();

        for (var storedOverlay : storedOverlays) {
            if (!availableOverlays.contains(storedOverlay)) {
                selectedButNotAvailableOverlays.add(storedOverlay);
            }
        }

        storedOverlays.removeAll(selectedButNotAvailableOverlays);
        mListSelectionView.getSwappedTargetItems().setAll(storedOverlays);
        mListSelectionView.getSwappedSourceItems().removeAll(storedOverlays);
        mPopulated = true;
    }

    private static class Holder {

        private static final LayerOverlayView INSTANCE = new LayerOverlayView();
    }
}
