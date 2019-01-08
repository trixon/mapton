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

import gov.nasa.worldwind.layers.Layer;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckListView;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private final CheckListView<Layer> mListView = new CheckListView<>();
    private WorldWindowPanel mMap;
    private final Preferences mPreferences = NbPreferences.forModule(LayerView.class).node("layer_visibility");

    public static LayerView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerView() {
        createUI();
        initListeners();
    }

    void refresh(WorldWindowPanel map) {
        mMap = map;
        mListView.setItems(mMap.getCustomLayers());
        mListView.setCellFactory(lv -> new CheckBoxListCell<Layer>(mListView::getItemBooleanProperty) {
            @Override
            public void updateItem(Layer layer, boolean empty) {
                Platform.runLater(() -> {
                    super.updateItem(layer, empty);
                    setText(layer == null ? "" : layer.getName());
                });
            }
        });

        mListView.getItems().addListener((ListChangeListener.Change<? extends Layer> c) -> {
            for (Layer layer : mListView.getItems()) {
                if (mPreferences.getBoolean(layer.getName(), layer.isEnabled())) {
                    mListView.getCheckModel().check(layer);
                } else {
                    mListView.getCheckModel().clearCheck(layer);
                }
            }
        });

        mListView.getCheckModel().getCheckedItems().addListener((ListChangeListener.Change<? extends Layer> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((layer) -> {
                        layer.setEnabled(true);
                        mPreferences.putBoolean(layer.getName(), true);
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach((layer) -> {
                        layer.setEnabled(false);
                        mPreferences.putBoolean(layer.getName(), false);
                    });
                }
            }
        });
    }

    private void createUI() {
        setCenter(mListView);
    }

    private void initListeners() {
    }

    private static class Holder {

        private static final LayerView INSTANCE = new LayerView();
    }
}
