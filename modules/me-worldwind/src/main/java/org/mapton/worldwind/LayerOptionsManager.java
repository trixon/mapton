/*
 * Copyright 2025 Patrik Karlström <patrik@trixon.se>.
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
import java.util.Comparator;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.BorderPane;
import javafx.util.StringConverter;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundle;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class LayerOptionsManager {

    private final WorldWindOptionsView mOptionsView = new WorldWindOptionsView();

    public static LayerOptionsManager getInstance() {
        return Holder.INSTANCE;
    }

    private LayerOptionsManager() {
        initListeners();
    }

    public void refresh() {
//        if (mOptionsView.mComboBox.getValue() != null) {
        Mapton.getGlobalState().put(MKey.LAYER_PROPERTIES, mOptionsView);
//        }
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener(gsce -> {
            Platform.runLater(() -> {
                mOptionsView.mComboBox.getSelectionModel().select(gsce.getValue());
                refresh();
            });
        }, MKey.LAYER_PROPERTIES_WORLD_WIND);
    }

    private static class Holder {

        private static final LayerOptionsManager INSTANCE = new LayerOptionsManager();
    }

    public class WorldWindOptionsView extends BorderPane {

        private final ComboBox<LayerBundle> mComboBox = new ComboBox<>();

        public WorldWindOptionsView() {
            createUI();
            populate();
            initListeners();
        }

        private void createUI() {
            setTop(mComboBox);
            mComboBox.setVisibleRowCount(99);
            mComboBox.prefWidthProperty().bind(widthProperty());
            mComboBox.setConverter(new StringConverter<LayerBundle>() {
                @Override
                public LayerBundle fromString(String string) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public String toString(LayerBundle layerBundle) {
                    return layerBundle == null ? null : layerBundle.getPath();
                }

            });
        }

        private void initListeners() {
            Lookup.getDefault().lookupResult(LayerBundle.class).addLookupListener(lookupEvent -> {
                populate();
            });

            mComboBox.setOnAction(actionEvent -> {
                var layerBundle = mComboBox.getValue();
                setCenter(layerBundle.getOptionsView());
                Mapton.getGlobalState().put(MKey.LAYER_PROPERTIES_WORLD_WIND, layerBundle);
            });
        }

        private void populate() {
            var layerBundles = new ArrayList<>(Lookup.getDefault().lookupAll(LayerBundle.class)).stream()
                    .filter(lb -> lb.getOptionsView() != null)
                    .sorted(Comparator.comparing(LayerBundle::getPath))
                    .toList();
            mComboBox.getItems().setAll(layerBundles);
        }
    }
}
