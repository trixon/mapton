/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.base.ui;

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
import org.mapton.api.MKey;
import org.mapton.api.MPropertyItem;
import org.mapton.api.MSelectionLockManager;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBar;
import org.openide.util.NbBundle;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.control.LogPanel;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class ObjectPropertyView extends BorderPane {

    private final Map<String, Object> mDummyMap = new LinkedHashMap<>();
    private LogPanel mLogPanel;
    private Label mPlaceholderLabel;
    private PropertySheet mPropertySheet;

    public ObjectPropertyView() {
        createUI();
        initListeners();
        refresh(null);
    }

    private void createUI() {
        mPropertySheet = new PropertySheet();
        mPropertySheet.setMode(PropertySheet.Mode.NAME);
        mLogPanel = new LogPanel();
        setCenter(mPropertySheet);

        mPlaceholderLabel = new Label(NbBundle.getMessage(getClass(), "object_properties_placeholder"), MaterialIcon._Action.ASSIGNMENT.getImageView(getIconSizeToolBar()));
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

    private void loadList(ObservableList<PropertySheet.Item> propertyItems) {
        mPropertySheet.getItems().setAll(propertyItems);
    }

    private void loadMap(Map<String, Object> propertiesMap) {
        ObservableList<PropertySheet.Item> propertyItems = FXCollections.observableArrayList();
        propertiesMap.keySet().forEach((key) -> {
            propertyItems.add(new MPropertyItem(propertiesMap, key));
        });

        loadList(propertyItems);
    }

    @SuppressWarnings("unchecked")
    private void refresh(Object o) {
        if (MSelectionLockManager.getInstance().isLocked()) {
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
        } else if (o instanceof Node) {
            centerObject = (Node) o;
        } else if (o instanceof String) {
            centerObject = mLogPanel;
            load(o.toString());
        } else {
            centerObject = mLogPanel;
            load(ToStringBuilder.reflectionToString(o, ToStringStyle.MULTI_LINE_STYLE));
        }

        setCenter(centerObject);
    }
}
