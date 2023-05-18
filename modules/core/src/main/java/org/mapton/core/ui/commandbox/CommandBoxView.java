/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.core.ui.commandbox;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MCommandBoxItem;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class CommandBoxView extends BorderPane {

    private final Map<Action, String> mActionParents = new HashMap<>();
    private TextField mFilterTextField;
    private long mLatestFocus;
    private final Preferences mPreferences = NbPreferences.forModule(CommandBoxView.class).node("runbox_expanded_state");
    private final Map<String, TreeItem<Action>> mItemParents = new ConcurrentSkipListMap<>();
    private final ArrayList<MCommandBoxItem> mItems = new ArrayList<>();
    private final TreeView<Action> mTreeView = new TreeView<>();

    public CommandBoxView() {
        createUI();
        initListeners();

        initTools();
        populate();
    }

    private void createUI() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.SEARCH.toString());

        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory(param -> new ActionTreeCell());

        setTop(mFilterTextField);
        setCenter(mTreeView);
    }

    private TreeItem<Action> getParent(TreeItem<Action> parent, String category) {
        var categorySegments = StringUtils.split(category, "/");
        var sb = new StringBuilder();

        for (var segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mItemParents.containsKey(path)) {
                parent = mItemParents.get(path);
            } else {
                var action = new Action(segment, actionEvent -> {
                });

                parent.getChildren().add(parent = mItemParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(action)));
            }

            sb.append("/");
        }

        return parent;
    }

    private void initListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            populate();
        });

        mTreeView.focusedProperty().addListener((observable, oldValue, newValue) -> {
            mLatestFocus = System.currentTimeMillis();
        });

        mTreeView.setOnKeyPressed(event -> {
            var selectedItem = mTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem != null && event.getCode() == KeyCode.ENTER) {
                if (selectedItem.getChildren().isEmpty()) {
                    Mapton.getGlobalState().send(MKey.MAP_TOOL_STARTED, event);
                }
                mTreeView.getSelectionModel().getSelectedItem().getValue().handle(null);
            }
        });

        Lookup.getDefault().lookupResult(MCommandBoxItem.class).addLookupListener(event -> {
            initTools();
        });

        Mapton.getGlobalState().addListener(evt -> {
            Almond.openAndActivateTopComponent(evt.getValue());
        }, MKey.LAYER_FAST_OPEN_TOOL);
    }

    private void initTools() {
        mItems.clear();
        Lookup.getDefault().lookupAll(MCommandBoxItem.class).forEach((tool) -> {
            mItems.add(tool);
        });
    }

    private void populate() {
        mItemParents.clear();
        var rootMark = new Action("");
        var root = new TreeItem<Action>(rootMark);

        for (var item : mItems) {
            mActionParents.put(item.getAction(), item.getParent());
            final String filter = mFilterTextField.getText();
            final boolean validFilter
                    = StringHelper.matchesSimpleGlob(item.getParent(), filter, true, true)
                    || StringHelper.matchesSimpleGlob(item.getAction().getText(), filter, true, true);

            if (validFilter) {
                var actionTreeItem = new TreeItem<Action>(item.getAction());
                String category = StringUtils.defaultString(item.getParent());

                var parent = mItemParents.computeIfAbsent(category, k -> getParent(root, category));
                parent.getChildren().add(actionTreeItem);
            }
        }

        postPopulate(root, "");
        mTreeView.setRoot(root);
    }

    private void postPopulate(TreeItem<Action> treeItem, String level) {
        var value = treeItem.getValue();
        final String path = "%s/%s".formatted(mActionParents.get(value), value.getText());
        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<Action>> c1 = (o1, o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<Action>> c2 = (o1, o2) -> o1.getValue().getText().compareTo(o2.getValue().getText());

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (var childTreeItem : treeItem.getChildren()) {
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
            String text = action.getText();
            if (StringUtils.endsWith(text, ")<")) {
                setText(StringUtils.substringBefore(text, " ("));
                setTooltip(new Tooltip(StringUtils.removeEnd(text, "<")));
            } else {
                setText(text);
            }
            setGraphic(action.getGraphic());
            setOnMouseClicked((MouseEvent event) -> {
                int clicksToSubtract = SystemHelper.age(mLatestFocus) < 1000 ? 1 : 0;
                int clicks = event.getClickCount() - clicksToSubtract;
                if (clicks == 2 && event.getButton() == MouseButton.PRIMARY) {
                    if (getChildren().isEmpty()) {
                        Mapton.getGlobalState().send(MKey.MAP_TOOL_STARTED, event);
                    }
                    action.handle(null);
                }
            });
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
        }
    }
}
