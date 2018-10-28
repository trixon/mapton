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
package org.mapton.core.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeMap;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import org.controlsfx.control.action.Action;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.icons.material.MaterialIcon;
import org.mapton.api.MTool;
import static org.mapton.api.Mapton.getIconSizeToolBar;

/**
 *
 * @author Patrik Karlström
 */
public class ToolboxView extends TreeView<Action> {

    public ToolboxView() {
        createUI();
    }

    private void createUI() {
        setShowRoot(false);
        setCellFactory((TreeView<Action> param) -> new ActionTreeCell());

        setOnMouseClicked((event) -> {
            final TreeItem<Action> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                selectedItem.getValue().handle(null);
            }
        });

        setOnKeyPressed((event) -> {
            final TreeItem<Action> selectedItem = getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getCode() == KeyCode.ENTER) {
                getSelectionModel().getSelectedItem().getValue().handle(null);
            }
        });

        Lookup.getDefault().lookupResult(MTool.class).addLookupListener((LookupEvent ev) -> {
            populateToolbox();
        });

        populateToolbox();
    }

    private void populateToolbox() {
        TreeItem<Action> root = new TreeItem<>();
        root.setExpanded(true);
        ObservableList<TreeItem<Action>> treeRootChildrens = root.getChildren();
        TreeMap<String, TreeItem<Action>> actionParents = new TreeMap<>();
        ArrayList<TreeItem> tempRootItems = new ArrayList<>();

        Lookup.getDefault().lookupAll(MTool.class).forEach((toolboxAction) -> {
            TreeItem<Action> treeItem = new TreeItem(toolboxAction.getAction());
            treeItem.getValue().setGraphic(MaterialIcon._Action.BUILD.getImageView(getIconSizeToolBar() / 2));

            final String parentName = toolboxAction.getParent();
            if (parentName == null) {
                tempRootItems.add(treeItem);
            } else {
                actionParents.computeIfAbsent(parentName, k -> new TreeItem(parentName)).getChildren().add(treeItem);
            }
        });

        Comparator<TreeItem> treeItemComparator = (TreeItem o1, TreeItem o2) -> ((Action) o1.getValue()).getText().compareTo(((Action) o2.getValue()).getText());

        actionParents.keySet().stream().map((key) -> {
            TreeItem<Action> parentItem = new TreeItem<>(new Action(key));
            parentItem.getValue().setGraphic(MaterialIcon._Places.BUSINESS_CENTER.getImageView(getIconSizeToolBar() / 2));
            FXCollections.sort(actionParents.get(key).getChildren(), treeItemComparator);
            actionParents.get(key).getChildren().forEach((item) -> {
                parentItem.getChildren().add(item);
            });
            return parentItem;
        }).forEachOrdered((parentItem) -> {
            treeRootChildrens.add(parentItem);
        });

        Collections.sort(tempRootItems, treeItemComparator);
        tempRootItems.forEach((rootItem) -> {
            treeRootChildrens.add(rootItem);
        });

        root.getChildren().forEach((treeItem) -> {
            treeItem.setExpanded(true);
        });

        setRoot(root);
    }

    class ActionTreeCell extends TreeCell<Action> {

        public ActionTreeCell() {
            createUI();
        }

        @Override
        protected void updateItem(Action action, boolean empty) {
            super.updateItem(action, empty);

            if (action == null || empty) {
                clearContent();
            } else {
                addContent(action);
            }
        }

        private void addContent(Action action) {
            setText(action.getText());
            setGraphic(action.getGraphic());
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
        }

    }

}
