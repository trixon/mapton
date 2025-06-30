/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.core.ui;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.controlsfx.control.PropertySheet;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.core.api.MTopComponent;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;
import se.trixon.almond.nbp.core.SelectionLockManager;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.PropertyItem;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 * Generic Property TopComponent
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.core.ui//LayerProperties//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "LayerPropertiesTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false, position = 200)
public final class LayerPropertiesTopComponent extends MTopComponent {

    public LayerPropertiesTopComponent() {
        setName(Dict.LAYER_PROPERTIES.toString());
    }

    @Override
    protected void initFX() {
        setScene(new Scene(new LayerPropertiesView()));
        Mapton.getGlobalState().put(MKey.LAYER_PROPERTIES, Mapton.getGlobalState().get(MKey.LAYER_PROPERTIES));
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
    }

    class LayerPropertiesView extends BorderPane {

        private final Map<String, Object> mDummyMap = new LinkedHashMap<>();
        private LogPanel mLogPanel;
        private Label mPlaceholderLabel;
        private PropertySheet mPropertySheet;

        public LayerPropertiesView() {
            createUI();
            initListeners();
            refresh(null);
        }

        private void createUI() {
            mPropertySheet = new PropertySheet();
            mPropertySheet.setMode(PropertySheet.Mode.NAME);
            mLogPanel = new LogPanel();
            setCenter(mPropertySheet);

            mPlaceholderLabel = new Label(getName());
            mPlaceholderLabel.setDisable(true);
        }

        private void initListeners() {
            Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
                Platform.runLater(() -> {
                    refresh(evt.getValue());
                });
            }, MKey.LAYER_PROPERTIES);
        }

        private void load(String text) {
            mLogPanel.setText(text);
        }

        private void loadList(ObservableList<PropertySheet.Item> propertyItems) {
            mPropertySheet.getItems().setAll(propertyItems);
        }

        private void loadMap(Map<String, Object> propertiesMap) {
            ObservableList<PropertySheet.Item> propertyItems = FXCollections.observableArrayList();
            propertiesMap.keySet().forEach((key) -> {
                propertyItems.add(new PropertyItem(propertiesMap, key));
            });

            loadList(propertyItems);
        }

        @SuppressWarnings("unchecked")
        private void refresh(Object o) {
            if (SelectionLockManager.getInstance().isLocked()) {
                return;
            }

            Node centerObject = null;

            if (o == null) {
                centerObject = mPlaceholderLabel;
            } else if (o.getClass().isInstance(mPropertySheet.getItems())) {
                centerObject = mPropertySheet;
                loadList((ObservableList<PropertySheet.Item>) o);
            } else if (o.getClass().isInstance(mDummyMap)) {
                centerObject = mPropertySheet;
                loadMap((Map<String, Object>) o);
            } else if (o instanceof Node node) {
                centerObject = node;
            } else if (o instanceof String s) {
                centerObject = mLogPanel;
                load(s);
            } else {
                centerObject = mLogPanel;
                load(ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE));
            }

            setCenter(centerObject);
        }
    }
}
