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
package org.mapton.base.ui.poi;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class PoiCategoryCheckTreeView extends CheckTreeView<MPoi> {

    private CheckModel<TreeItem<MPoi>> mCheckModel;
    private final Preferences mExpandedPreferences;
    private final MPoiManager mManager = MPoiManager.getInstance();
    private final HashSet<MPoi> mPoiEnabledListenerSet;
    private final Map<String, CheckBoxTreeItem<MPoi>> mPoiParents;
    private final Map<TreeItem<MPoi>, TreeItem<MPoi>> mPoisToRemove;
    private CheckBoxTreeItem<MPoi> mRootItem;
    private final HashSet<CheckBoxTreeItem<MPoi>> mTreeItemExpanderSet;
    private final HashSet<CheckBoxTreeItem<MPoi>> mTreeItemListenerSet;
    private final Preferences mVisibilityPreferences;

    public PoiCategoryCheckTreeView() {
        mVisibilityPreferences = NbPreferences.forModule(PoiCategoryCheckTreeView.class).node("poi_visibility");
        mExpandedPreferences = NbPreferences.forModule(PoiCategoryCheckTreeView.class).node("poi_expanded");
        mPoiParents = new TreeMap<>();
        mPoisToRemove = new HashMap<>();
        mPoiEnabledListenerSet = new HashSet<>();
        mTreeItemListenerSet = new HashSet<>();
        mTreeItemExpanderSet = new HashSet<>();

        createUI();
    }

    void populate() {
        mPoiParents.clear();
        mRootItem.getChildren().clear();
        mTreeItemListenerSet.clear();
        mPoisToRemove.clear();

        SortedList<MPoi> sortedPois = mManager.getAllItems().sorted((MPoi o1, MPoi o2) -> o1.getName().compareTo(o2.getName()));

        for (MPoi poi : sortedPois) {
            CheckBoxTreeItem<MPoi> poiTreeItem = new CheckBoxTreeItem<>(poi);
            String category = getCategory(poi);
            CheckBoxTreeItem<MPoi> parent = mPoiParents.computeIfAbsent(category, k -> getParent(mRootItem, category, poi));
            parent.getChildren().add(poiTreeItem);
        }

        postPopulate(mRootItem);

        for (Map.Entry<TreeItem<MPoi>, TreeItem<MPoi>> entry : mPoisToRemove.entrySet()) {
            entry.getValue().getChildren().remove(entry.getKey());
        }

        mTreeItemExpanderSet.forEach((checkBoxTreeItem) -> {
            checkBoxTreeItem.setExpanded(true);
        });
    }

    private void createUI() {
        mRootItem = new CheckBoxTreeItem<>(new MPoi());

        setRoot(mRootItem);
        mCheckModel = getCheckModel();
        setShowRoot(false);
        setCellFactory(param -> new PoiTreeCell());
    }

    private String getCategory(MPoi poi) {
        return String.format("%s/%s",
                StringUtils.defaultString(poi.getProvider()),
                StringUtils.defaultString(poi.getCategory())
        );
    }

    private CheckBoxTreeItem<MPoi> getParent(CheckBoxTreeItem<MPoi> parent, String category, MPoi poi) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mPoiParents.containsKey(path)) {
                parent = mPoiParents.get(path);
            } else {
                MPoi parentPoi = new MPoi();
                parentPoi.setName(segment);
                parentPoi.setProvider(poi.getProvider());
                parentPoi.setCategory(path);

                parent.getChildren().add(parent = mPoiParents.computeIfAbsent(sb.toString(), k -> new CheckBoxTreeItem<>(parentPoi)));
            }

            sb.append("/");
        }

        return parent;
    }

    private boolean isCategoryTreeItem(TreeItem<MPoi> treeItem) {
        return !treeItem.getChildren().isEmpty();
    }

    private void postPopulate(CheckBoxTreeItem<MPoi> treeItem) {
        final MPoi poi = treeItem.getValue();

        if (isCategoryTreeItem(treeItem)) {
            final String path = StringUtils.defaultString(poi.getCategory(), "DEFAULT");

            if (mExpandedPreferences.getBoolean(path, false)) {
                mTreeItemExpanderSet.add(treeItem);
            }

            if (!mTreeItemListenerSet.contains(treeItem)) {
                treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    mExpandedPreferences.putBoolean(path, newValue);
                });

                mTreeItemListenerSet.add(treeItem);
            }

            Comparator<TreeItem<MPoi>> c1 = (TreeItem<MPoi> o1, TreeItem<MPoi> o2) -> Boolean.compare(isCategoryTreeItem(o1), isCategoryTreeItem(o2));
            Comparator<TreeItem<MPoi>> c2 = (TreeItem<MPoi> o1, TreeItem<MPoi> o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

            treeItem.getChildren().sort(c1.reversed().thenComparing(c2));

            for (TreeItem<MPoi> childTreeItem : treeItem.getChildren()) {
                postPopulate((CheckBoxTreeItem<MPoi>) childTreeItem);
            }
        } else {
            mPoisToRemove.put(treeItem, treeItem.getParent());
        }
    }

    class PoiTreeCell extends CheckBoxTreeCell<MPoi> {

        public PoiTreeCell() {
            createUI();
        }

        @Override
        public void updateItem(MPoi poi, boolean empty) {
            super.updateItem(poi, empty);

            if (poi == null || empty) {
                clearContent();
            } else {
                addContent(poi);
            }
        }

        private void addContent(MPoi poi) {
            setText(poi.getName());
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
        }
    }
}
