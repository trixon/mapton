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
import java.beans.PropertyChangeEvent;
import java.util.HashSet;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.BooleanUtils;
import org.controlsfx.control.CheckListView;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private final HashSet<Layer> mLayerEnabledListenerSet = new HashSet<>();
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
        if (mMap == null) {
            mMap = map;
            mMap.getCustomLayers().addListener((ListChangeListener.Change<? extends Layer> change) -> {
                refresh(map);
            });
        }

        SortedList<Layer> sortedLayers = mMap.getCustomLayers().sorted((Layer o1, Layer o2) -> o1.getName().compareTo(o2.getName()));
        ObservableList<Layer> layers = FXCollections.observableArrayList();

        for (Layer layer : sortedLayers) {
            Object hiddenValue = layer.getValue(WWHelper.KEY_HIDE_FROM_LAYER_MANAGER);
            boolean hidden = hiddenValue != null;
            if (hidden) {
                hidden = BooleanUtils.toBoolean(layer.getValue(WWHelper.KEY_HIDE_FROM_LAYER_MANAGER).toString());
            }

            if (!hidden) {
                layers.add(layer);
            }
        }

        //Don't use setAll...
        mListView.getItems().clear();
        mListView.getItems().addAll(layers);

        Platform.runLater(() -> {
            mListView.requestLayout();
        });
    }

    private void createUI() {
        setCenter(mListView);
    }

    private void initListeners() {
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
                if (!mLayerEnabledListenerSet.contains(layer)) {
                    mLayerEnabledListenerSet.add(layer);
                    layer.addPropertyChangeListener("Enabled", (PropertyChangeEvent evt) -> {
                        if ((boolean) evt.getNewValue()) {
                            mListView.getCheckModel().check(layer);
                        } else {
                            mListView.getCheckModel().clearCheck(layer);
                        }
                    });
                }

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

        mListView.setOnMouseClicked((event) -> {
            Layer layer = mListView.getSelectionModel().getSelectedItem();
            if (layer != null && layer.hasKey(WWHelper.KEY_FAST_OPEN) && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Almond.openAndActivateTopComponent((String) layer.getValue(WWHelper.KEY_FAST_OPEN));
            }
        });
    }

    private static class Holder {

        private static final LayerView INSTANCE = new LayerView();
    }
}
