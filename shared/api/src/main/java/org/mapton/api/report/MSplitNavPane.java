/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.api.report;

import java.util.Comparator;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.MasterDetailPane;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public class MSplitNavPane<T extends MSplitNavType> extends MasterDetailPane {

    private final Class<? extends MSplitNavType> mClass;
    private Label mPlaceholderLabel;
    private final Preferences mPreferences;
    private final TreeMap<String, TreeItem<T>> mReportParents = new TreeMap<>();
    private TreeView<T> mTreeView;

    public MSplitNavPane(Class<T> clazz) {
        mClass = clazz;
        mPreferences = NbPreferences.forModule(mClass).node("expanded_state_" + mClass.getName());

        createUI();
    }

    private void createUI() {
        mPlaceholderLabel = new Label();

        mTreeView = new TreeView<>();
        mTreeView.setShowRoot(false);
        mTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends TreeItem<T>> c) -> {
            TreeItem<T> selectedItem = mTreeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) {
                setDetailNode(mPlaceholderLabel);
            } else {
                T selectedReport = selectedItem.getValue();
                setDetailNode(selectedReport.getNode());
                selectedReport.onSelect();
            }
        });

        Lookup.getDefault().lookupResult(mClass).addLookupListener((LookupEvent ev) -> {
            populate();
        });

        populate();

        setMasterNode(mTreeView);
        setDetailNode(mPlaceholderLabel);
        setDividerPosition(0.5);
    }

    private TreeItem<T> getParent(TreeItem<T> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mReportParents.containsKey(path)) {
                parent = mReportParents.get(path);
            } else {
                T report = (T) new MSplitNavType() {
                    @Override
                    public String getName() {
                        return segment;
                    }

                    @Override
                    public Node getNode() {
                        return null;
                    }

                    @Override
                    public String getParent() {
                        return path;
                    }

                    @Override
                    public String toString() {
                        return getName();
                    }
                };

                parent.getChildren().add(parent = mReportParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(report)));
            }

            sb.append("/");
        }

        return parent;
    }

    private void populate() {
        mReportParents.clear();

        T rootReport = (T) new MSplitNavType() {
            @Override
            public String getName() {
                return "";
            }

            @Override
            public Node getNode() {
                return null;
            }

            @Override
            public String getParent() {
                return "";
            }
        };

        TreeItem<T> root = new TreeItem<>(rootReport);

        new Thread(() -> {
            Lookup.getDefault().lookupAll(mClass).forEach((report) -> {
                TreeItem<T> reportTreeItem = new TreeItem<>((T) report);
                String category = report.getParent();
                TreeItem<T> parent = mReportParents.computeIfAbsent(category, k -> getParent(root, category));
                parent.getChildren().add(reportTreeItem);
            });

            Platform.runLater(() -> {
                postPopulate(root);
                mTreeView.setRoot(root);
            });
        }).start();
    }

    private void postPopulate(TreeItem<T> treeItem) {
        final var value = treeItem.getValue();
        final var path = String.format("%s/%s", value.getParent(), value.getName());

        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<T>> c1 = (TreeItem<T> o1, TreeItem<T> o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<T>> c2 = (TreeItem<T> o1, TreeItem<T> o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (TreeItem<T> childTreeItem : treeItem.getChildren()) {
            postPopulate(childTreeItem);
        }
    }
}
