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
package org.mapton.core_nb.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MKey;
import org.mapton.api.MToolMap;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MapToolboxView extends BorderPane {

    private final Map<Action, String> mActionParents = new HashMap<>();
    private TextField mFilterTextField;
    private final Preferences mPreferences = NbPreferences.forModule(MapToolboxView.class).node("maptools_expanded_state");
    private final Map<String, TreeItem<Action>> mToolParents = new TreeMap<>();
    private final ArrayList<MToolMap> mTools = new ArrayList<>();
    private final TreeView<Action> mTreeView = new TreeView<>();

    public MapToolboxView() {
        createUI();
        addListeners();

        initTools();
        populate();
    }

    private void addListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            populate();
        });

        mTreeView.setOnMouseClicked((event) -> {
            final TreeItem<Action> selectedItem = mTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                if (selectedItem.getChildren().isEmpty()) {
                    Mapton.getGlobalState().send(MKey.MAP_TOOL_STARTED, event);
                }
                selectedItem.getValue().handle(null);
            }
        });

        mTreeView.setOnKeyPressed((event) -> {
            final TreeItem<Action> selectedItem = mTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getCode() == KeyCode.ENTER) {
                if (selectedItem.getChildren().isEmpty()) {
                    Mapton.getGlobalState().send(MKey.MAP_TOOL_STARTED, event);
                }
                mTreeView.getSelectionModel().getSelectedItem().getValue().handle(null);
            }
        });

        Lookup.getDefault().lookupResult(MToolMap.class).addLookupListener((LookupEvent ev) -> {
            initTools();
        });

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Almond.openAndActivateTopComponent(evt.getValue());
        }, MKey.LAYER_FAST_OPEN_TOOL);
    }

    private void createUI() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.TOOLS_SEARCH.toString());

        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory((TreeView<Action> param) -> new ActionTreeCell());

        setTop(mFilterTextField);
        setCenter(mTreeView);
    }

    private TreeItem<Action> getParent(TreeItem<Action> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mToolParents.containsKey(path)) {
                parent = mToolParents.get(path);
            } else {
                Action action = new Action(segment, (event) -> {
                });

                parent.getChildren().add(parent = mToolParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(action)));
            }

            sb.append("/");
        }

        return parent;
    }

    private void initTools() {
        mTools.clear();
        Lookup.getDefault().lookupAll(MToolMap.class).forEach((tool) -> {
            mTools.add(tool);
        });
    }

    private void populate() {
        mToolParents.clear();
        Action rootMark = new Action("");
        TreeItem<Action> root = new TreeItem<>(rootMark);

        for (MToolMap tool : mTools) {
            mActionParents.put(tool.getAction(), tool.getParent());
            final String filter = mFilterTextField.getText();
            final boolean validFilter
                    = StringHelper.matchesSimpleGlob(tool.getParent(), filter, true, true)
                    || StringHelper.matchesSimpleGlob(tool.getAction().getText(), filter, true, true);

            if (validFilter) {
                TreeItem<Action> actionTreeItem = new TreeItem<>(tool.getAction());
                String category = StringUtils.defaultString(tool.getParent());

                TreeItem<Action> parent = mToolParents.computeIfAbsent(category, k -> getParent(root, category));
                parent.getChildren().add(actionTreeItem);
            }
        }

        postPopulate(root, "");
        mTreeView.setRoot(root);
    }

    private void postPopulate(TreeItem<Action> treeItem, String level) {
        final Action value = treeItem.getValue();
        final String path = String.format("%s/%s", mActionParents.get(value), value.getText());
        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<Action>> c1 = (TreeItem<Action> o1, TreeItem<Action> o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<Action>> c2 = (TreeItem<Action> o1, TreeItem<Action> o2) -> o1.getValue().getText().compareTo(o2.getValue().getText());

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (TreeItem<Action> childTreeItem : treeItem.getChildren()) {
            postPopulate(childTreeItem, level + "-");
        }
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
