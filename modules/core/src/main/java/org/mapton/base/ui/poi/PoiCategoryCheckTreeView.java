/*
 * Copyright 2022 Patrik Karlström.
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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckTreeView;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class PoiCategoryCheckTreeView extends CheckTreeView<MPoi> {

    private ListChangeListener<TreeItem<MPoi>> mCheckedItemsChangeListener;
    private final DelayedResetRunner mDelayedResetRunner;
    private final Preferences mExpandedPreferences;
    private final MPoiManager mManager = MPoiManager.getInstance();
    private final Map<String, CheckBoxTreeItem<MPoi>> mPoiParents;
    private final Map<TreeItem<MPoi>, TreeItem<MPoi>> mPoisToRemove;
    private CheckBoxTreeItem<MPoi> mRootItem;
    private final HashSet<CheckBoxTreeItem<MPoi>> mTreeItemExpanderSet;
    private final HashSet<CheckBoxTreeItem<MPoi>> mTreeItemListenerSet;
    private final Preferences mVisibilityPreferences;

    public PoiCategoryCheckTreeView() {
        mVisibilityPreferences = NbPreferences.forModule(PoiCategoryCheckTreeView.class).node("poi_visibility");
        mExpandedPreferences = NbPreferences.forModule(PoiCategoryCheckTreeView.class).node("poi_expanded");
        mPoiParents = Collections.synchronizedMap(new TreeMap<>());
        mPoisToRemove = Collections.synchronizedMap(new HashMap<>());
        mTreeItemListenerSet = new HashSet<>();
        mTreeItemExpanderSet = new HashSet<>();

        mDelayedResetRunner = new DelayedResetRunner(50, () -> {
            var categories = new TreeSet<String>();

            getCheckModel().getCheckedItems().forEach(checkedItem -> {
                categories.add(getPath(checkedItem.getValue()));
            });

            mManager.categoriesProperty().set(new TreeSet<>());
            mManager.categoriesProperty().set(categories);
        });

        createUI();
        initListeners();
    }

    private void createUI() {
        mRootItem = new CheckBoxTreeItem<>(new MPoi());

        setRoot(mRootItem);
        setShowRoot(false);
        setCellFactory(treeView -> new PoiTreeCell());
    }

    private String getCategory(MPoi poi) {
        return "%s/%s".formatted(
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

    private String getPath(MPoi poi) {
        return StringUtils.defaultString(poi.getCategory(), "DEFAULT");
    }

    private void initListeners() {
        mCheckedItemsChangeListener = (ListChangeListener.Change<? extends TreeItem<MPoi>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach((treeItem) -> {
                        if (!isCategoryTreeItem(treeItem)) {
                            mVisibilityPreferences.putBoolean(getPath(treeItem.getValue()), true);
                        }
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach((treeItem) -> {
                        if (!isCategoryTreeItem(treeItem)) {
                            mVisibilityPreferences.putBoolean(getPath(treeItem.getValue()), false);
                        }
                    });
                }
            }

            mDelayedResetRunner.reset();
        };

        mManager.trigRefreshCategoriesProperty().addListener((observable, oldValue, newValue) -> {
            getCheckModel().getCheckedItems().removeListener(mCheckedItemsChangeListener);
            populate();
            getCheckModel().getCheckedItems().addListener(mCheckedItemsChangeListener);
        });
    }

    private boolean isCategoryTreeItem(TreeItem<MPoi> treeItem) {
        return !treeItem.getChildren().isEmpty();
    }

    private void populate() {
        mRootItem.getChildren().clear();
        mTreeItemListenerSet.clear();
        mPoisToRemove.clear();
        mPoiParents.clear();

        Map<String, CheckBoxTreeItem<MPoi>> poiParents = new TreeMap<>();

        for (var poi : mManager.getAllItems()) {
            var poiTreeItem = new CheckBoxTreeItem<>(poi);
            var category = getCategory(poi);
            var parent = poiParents.computeIfAbsent(category, k -> getParent(mRootItem, category, poi));
            parent.getChildren().add(poiTreeItem);
        }

        mPoiParents.clear();
        mPoiParents.putAll(poiParents);

        postPopulate(mRootItem);

        for (Map.Entry<TreeItem<MPoi>, TreeItem<MPoi>> entry : mPoisToRemove.entrySet()) {
            try {
                entry.getValue().getChildren().remove(entry.getKey());
            } catch (Exception e) {
            }
        }

        mTreeItemExpanderSet.forEach((checkBoxTreeItem) -> {
            checkBoxTreeItem.setExpanded(true);
        });

        getCheckModel().clearChecks();
        postPopulateRestoreSelection(mRootItem);

        mDelayedResetRunner.reset();
    }

    private void postPopulate(CheckBoxTreeItem<MPoi> treeItem) {
        final MPoi poi = treeItem.getValue();

        if (isCategoryTreeItem(treeItem)) {
            final String path = getPath(poi);

            if (mExpandedPreferences.getBoolean(path, false)) {
                mTreeItemExpanderSet.add(treeItem);
            }

            if (!mTreeItemListenerSet.contains(treeItem)) {
                treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    mExpandedPreferences.putBoolean(path, newValue);
                });

                mTreeItemListenerSet.add(treeItem);
            }

            Comparator<TreeItem<MPoi>> c1 = (TreeItem<MPoi> o1, TreeItem<MPoi> o2) -> {
                final String s1 = o1.getValue().getName().toLowerCase(Locale.getDefault());
                final String s2 = o2.getValue().getName().toLowerCase(Locale.getDefault());
                return s1.compareToIgnoreCase(s2);
            };
            treeItem.getChildren().sort(c1);

            for (TreeItem<MPoi> childTreeItem : treeItem.getChildren()) {
                postPopulate((CheckBoxTreeItem<MPoi>) childTreeItem);
            }
        } else {
            mPoisToRemove.put(treeItem, treeItem.getParent());
        }
    }

    private void postPopulateRestoreSelection(CheckBoxTreeItem<MPoi> treeItem) {
        if (treeItem.getChildren().isEmpty()) {
            if (mVisibilityPreferences.getBoolean(getPath(treeItem.getValue()), true)) {
                getCheckModel().check(treeItem);
            } else {
                getCheckModel().clearCheck(treeItem);
            }
        } else {
            for (TreeItem<MPoi> childTreeItem : treeItem.getChildren()) {
                postPopulateRestoreSelection((CheckBoxTreeItem<MPoi>) childTreeItem);
            }
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
