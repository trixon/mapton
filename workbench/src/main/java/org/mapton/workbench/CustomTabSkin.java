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
package org.mapton.workbench;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import com.dlsc.workbenchfx.view.controls.module.Tab;
import com.dlsc.workbenchfx.view.controls.module.TabSkin;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SkinBase;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.mapton.workbench.modules.MapModule;

/**
 *
 * @author Patrik Karlström
 */
public class CustomTabSkin extends SkinBase<Tab> {

    private Button closeBtn;
    private StackPane closeIconShape;
    private HBox controlBox;
    private final ReadOnlyObjectProperty<Node> icon;
    private final Tab mTab;
    private final ReadOnlyStringProperty name;
    private Label nameLbl;

    /**
     * Creates a new {@link TabSkin} object for a corresponding {@link Tab}.
     *
     * @param tab the {@link Tab} for which this Skin is created
     */
    public CustomTabSkin(Tab tab) {
        super(tab);
        mTab = tab;
        name = tab.nameProperty();
        icon = tab.iconProperty();

        initializeParts();
        layoutParts();
        setupBindings();
        setupEventHandlers();
        setupValueChangedListeners();

        updateIcon();
        getChildren().add(controlBox);
    }

    private WorkbenchModule getModule() {
        return mTab.moduleProperty().get();
    }

    private void initializeParts() {
        closeIconShape = new StackPane();
        closeIconShape.getStyleClass().add("shape");
        closeBtn = new Button("", closeIconShape);
        closeBtn.getStyleClass().addAll("icon", "close-icon");

        nameLbl = new Label();
        nameLbl.getStyleClass().add("tab-name-lbl");

        controlBox = new HBox();
        controlBox.getStyleClass().add("tab-box");
    }

    private void layoutParts() {
        Label iconPlaceholder = new Label(); // Will be replaced in the listener
        ObservableList<Node> children = controlBox.getChildren();
        WorkbenchModule module = getModule();

        if (module instanceof MapModule) {
            children.addAll(iconPlaceholder, nameLbl);
        } else {
            children.addAll(iconPlaceholder, nameLbl, closeBtn);
        }
    }

    private void setupBindings() {
        nameLbl.textProperty().bind(name);
    }

    private void setupEventHandlers() {
        closeBtn.setOnAction(e -> getSkinnable().close());
    }

    private void setupValueChangedListeners() {
        // handle icon changes
        icon.addListener((observable, oldIcon, newIcon) -> {
            if (oldIcon != newIcon) {
                updateIcon();
            }
        });
    }

    /**
     * Replaces the Icon when calling setModule().
     */
    private void updateIcon() {
        Node iconNode = icon.get();
        ObservableList<Node> children = controlBox.getChildren();
        children.remove(0);
        children.add(0, iconNode);
        iconNode.getStyleClass().add("tab-icon");
    }
}
