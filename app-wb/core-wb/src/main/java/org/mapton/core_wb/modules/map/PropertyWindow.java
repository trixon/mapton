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
package org.mapton.core_wb.modules.map;

import java.util.LinkedHashMap;
import java.util.Map;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.windowsystemfx.Window;
import se.trixon.windowsystemfx.WindowSystemComponent;

@WindowSystemComponent.Description(
        iconBase = "",
        preferredId = "org.mapton.core_wb.modules.map.PropertyWindow",
        parentId = "property",
        position = 1
)
@ServiceProvider(service = Window.class)
public final class PropertyWindow extends Window {

    private final Map<String, Object> mDummyMap = new LinkedHashMap<>();
    private LogPanel mLogPanel;
    private BorderPane mNode;
    private Label mPlaceholderLabel;
    private PropertySheet mPropertySheet;

    public PropertyWindow() {
        setName(Dict.OBJECT_PROPERTIES.toString());
    }

    @Override
    public Node getNode() {
        if ((mNode == null)) {
            createUI();
            initListeners();
            refresh(null);
        }

        return mNode;
    }

    private void createUI() {
        mPropertySheet = new PropertySheet();
        mPropertySheet.setMode(PropertySheet.Mode.NAME);
        mLogPanel = new LogPanel();
        mNode = new BorderPane(mPropertySheet);
        mPlaceholderLabel = new Label(NbBundle.getMessage(ChartWindow.class, "object_properties_placeholder"), MaterialIcon._Action.ASSIGNMENT.getImageView(getIconSizeToolBar()));
        mPlaceholderLabel.setDisable(true);
    }

    private void initListeners() {
        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                refresh(evt.getValue());
            });
        }, MKey.OBJECT_PROPERTIES);
    }

    private void load(String text) {
        mLogPanel.setText(text);
    }

    private void loadList(ObservableList<Item> propertyItems) {
        mPropertySheet.getItems().setAll(propertyItems);
    }

    private void loadMap(Map<String, Object> propertiesMap) {
        ObservableList<Item> propertyItems = FXCollections.observableArrayList();
        propertiesMap.keySet().forEach((key) -> {
            propertyItems.add(new PropertyItem(propertiesMap, key));
        });

        loadList(propertyItems);
    }

    @SuppressWarnings("unchecked")
    private void refresh(Object o) {
        Node centerObject = null;
        mNode.setCenter(mPropertySheet);

        if (o == null) {
            centerObject = mPlaceholderLabel;
        } else if (o.getClass().isInstance(mPropertySheet.getItems())) {
            centerObject = mPropertySheet;
            loadList((ObservableList<Item>) o);
        } else if (o.getClass().isInstance(mDummyMap)) {
            centerObject = mPropertySheet;
            loadMap((Map<String, Object>) o);
        } else if (o instanceof Node) {
            centerObject = (Node) o;
        } else if (o instanceof String) {
            centerObject = mLogPanel;
            load(o.toString());
        } else {
            load(ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE));
        }

        mNode.setCenter(centerObject);
    }
}
