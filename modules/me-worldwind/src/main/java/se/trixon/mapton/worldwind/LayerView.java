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
import java.util.HashSet;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckListView;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane {

    private final CheckListView<Layer> mListView = new CheckListView<>();
    private WorldWindowPanel mMap;
    private final Slider mSlider = new Slider(0, 1, 1);

    public LayerView() {
        createUI();
        initListeners();
    }

    void refresh(WorldWindowPanel map) {
        mMap = map;
        HashSet<String> blacklist = new HashSet<>();
        blacklist.add("Compass");
        blacklist.add("World Map");
        blacklist.add("Scale bar");
        blacklist.add("View Controls");
        blacklist.add("Stars");
        blacklist.add("Atmosphere");

        mListView.getItems().clear();
        map.getLayers().stream()
                .filter((layer) -> (!blacklist.contains(layer.getName())))
                .forEachOrdered((layer) -> {
                    mListView.getItems().add(0, layer);
                });

        mListView.getItems().stream()
                .filter((layer) -> (layer.isEnabled()))
                .forEachOrdered((layer) -> {
                    mListView.getCheckModel().check(layer);
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
        VBox opacityBox = new VBox(new Label(Dict.OPACITY.toString()), mSlider);
        setCenter(mListView);
        setBottom(opacityBox);

        mSlider.setDisable(true);
    }

    private void initListeners() {
        mListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Layer> observable, Layer oldValue, Layer layer) -> {
            mSlider.setDisable(layer == null);
            if (layer != null) {
                mSlider.setValue(layer.getOpacity());
            }
        });

        mSlider.valueProperty().addListener((ObservableValue<? extends Number> observable, Number oldValue, Number newValue) -> {
            for (Layer layer : mListView.getSelectionModel().getSelectedItems()) {
                layer.setOpacity(newValue.doubleValue());
            }

            mMap.redraw();
        });
    }
}
