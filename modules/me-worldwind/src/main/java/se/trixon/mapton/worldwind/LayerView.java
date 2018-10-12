/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.worldwind;

import gov.nasa.worldwind.layers.Layer;
import javafx.collections.ListChangeListener;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.BorderPane;
import org.controlsfx.control.CheckListView;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private final CheckListView<Layer> mListView = new CheckListView<>();
    private WorldWindowPanel mMap;

    public LayerView() {
        createUI();
        initListeners();
    }

    void refresh(WorldWindowPanel map) {
        mMap = map;
        mListView.setItems(mMap.getCustomLayers());
        mListView.setCellFactory(lv -> new CheckBoxListCell<Layer>(mListView::getItemBooleanProperty) {
            @Override
            public void updateItem(Layer layer, boolean empty) {
                super.updateItem(layer, empty);
                setText(layer == null ? "" : layer.getName());
            }
        });

        mListView.getItems().addListener((ListChangeListener.Change<? extends Layer> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((layer) -> {
                        if (layer.isEnabled()) {
                            mListView.getCheckModel().check(layer);
                        } else {
                            mListView.getCheckModel().clearCheck(layer);
                        }
                    });
                }
            }
        });

        mListView.getCheckModel().getCheckedItems().addListener((ListChangeListener.Change<? extends Layer> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((layer) -> {
                        layer.setEnabled(true);
                    });

                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach((layer) -> {
                        layer.setEnabled(false);
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
}
