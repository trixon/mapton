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
package org.mapton.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MAreaFilterManager {

    private final Set<MArea> mAreaEnabledListenerSet;
    private final Map<String, CheckBoxTreeItem<MArea>> mAreaParents;
    private CheckModel<TreeItem<MArea>> mCheckModel;
    private final Preferences mExpandedPreferences;
    private final GeometryFactory mGeometryFactory = new GeometryFactory();
    private final ObjectProperty<ObservableList<MArea>> mItemsProperty = new SimpleObjectProperty<>();
    private CheckBoxTreeItem<MArea> mRootItem;
    private final ObjectProperty<TreeItem<MArea>> mSelectedObjectProperty = new SimpleObjectProperty<>();
    private final Set<CheckBoxTreeItem<MArea>> mTreeItemExpanderSet;
    private final Set<CheckBoxTreeItem<MArea>> mTreeItemListenerSet;
    private CheckTreeView<MArea> mTreeView;
    private final Preferences mVisibilityPreferences;
    private final WKTReader mWktReader = new WKTReader();

    public static MAreaFilterManager getInstance() {
        return Holder.INSTANCE;
    }

    private MAreaFilterManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        mVisibilityPreferences = NbPreferences.forModule(MAreaFilterManager.class).node("area_visibility");
        mExpandedPreferences = NbPreferences.forModule(MAreaFilterManager.class).node("area_expanded");
        mAreaParents = new TreeMap<>();
        mAreaEnabledListenerSet = Collections.synchronizedSet(new HashSet<>());
        mTreeItemListenerSet = Collections.synchronizedSet(new HashSet<>());
        mTreeItemExpanderSet = Collections.synchronizedSet(new HashSet<>());

        createUI();
        initListeners();
        initBindings();
        populate();
    }

    public void addAll(ArrayList<MArea> areas) {
        for (var area : areas) {
            try {
                var geometry = mWktReader.read(area.getWktGeometry());
                area.setGeometry(geometry);
            } catch (ParseException ex) {
                Exceptions.printStackTrace(ex);
            }

        }

        getItems().addAll(areas);
    }

    public ObservableList<TreeItem<MArea>> getCheckedItems() {
        return mCheckModel.getCheckedItems();
    }

    public final ObservableList<MArea> getItems() {
        return mItemsProperty.get();
    }

    public CheckTreeView<MArea> getTreeView() {
        return mTreeView;
    }

    public boolean isValidCoordinate(Double lat, Double lon) {
        if (mCheckModel.getCheckedItems().isEmpty()) {
            return true;
        } else {
            var point = mGeometryFactory.createPoint(new Coordinate(lon, lat));

            for (var checkedTreeItem : mCheckModel.getCheckedItems()) {
                if (checkedTreeItem.isLeaf()) {
                    var areaGeometry = checkedTreeItem.getValue().getGeometry();
                    if (areaGeometry.contains(point)) {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    public boolean isValidCoordinate(MLatLon latLon) {
        return isValidCoordinate(latLon.getLatitude(), latLon.getLongitude());
    }

    public final ObjectProperty<ObservableList<MArea>> itemsProperty() {
        return mItemsProperty;
    }

    public ObjectProperty<TreeItem<MArea>> selectedObjectProperty() {
        return mSelectedObjectProperty;
    }

    private void createUI() {
        var rootArea = new MArea("");
        mRootItem = new CheckBoxTreeItem<>(rootArea);
        mTreeView = new CheckTreeView<>(mRootItem);
        mCheckModel = mTreeView.getCheckModel();
        mTreeView.setShowRoot(false);
    }

    private String getCategory(MArea area) {
        return StringUtils.substringBeforeLast(area.getKey(), "/");
    }

    private CheckBoxTreeItem<MArea> getParent(CheckBoxTreeItem<MArea> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mAreaParents.containsKey(path)) {
                parent = mAreaParents.get(path);
            } else {
                var area = new MArea(path);
                area.setName(segment);

                parent.getChildren().add(parent = mAreaParents.computeIfAbsent(sb.toString(), k -> new CheckBoxTreeItem<>(area)));
            }

            sb.append("/");
        }

        return parent;
    }

    private String getPath(MArea area) {
        return "%s_%s".formatted(getCategory(area), area.getName());
    }

    private void initBindings() {
        mSelectedObjectProperty.bind(mTreeView.getSelectionModel().selectedItemProperty());
    }

    private void initListeners() {
        getItems().addListener((ListChangeListener.Change<? extends MArea> c) -> {
            while (c.next()) {
                FxHelper.runLater(() -> {
                    populate();
                });
            }
        });

        mCheckModel.getCheckedItems().addListener((ListChangeListener.Change<? extends TreeItem<MArea>> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(treeItem -> {
                        if (treeItem.isLeaf() && !treeItem.getValue().isEnabled()) {
                            treeItem.getValue().setEnabled(true);
                            mVisibilityPreferences.putBoolean(getPath(treeItem.getValue()), true);
                        }
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(treeItem -> {
                        if (treeItem.isLeaf() && treeItem.getValue().isEnabled()) {
                            treeItem.getValue().setEnabled(false);
                            mVisibilityPreferences.putBoolean(getPath(treeItem.getValue()), false);
                        }
                    });
                }
            }
        });
    }

    private void populate() {
        mRootItem.getChildren().clear();
        mTreeItemListenerSet.clear();
        var areaParents = new TreeMap<String, CheckBoxTreeItem<MArea>>();

        for (var area : getItems()) {
            boolean rootItem = StringUtils.containsNone(area.getKey(), "/");
            if (rootItem) {
                area.setName(area.getKey());
            } else {
                area.setName(StringUtils.substringAfterLast(area.getKey(), "/"));
            }

            var areaTreeItem = new CheckBoxTreeItem<MArea>(area);

            if (rootItem) {
                mRootItem.getChildren().add(areaTreeItem);
            } else {
                String category = getCategory(area);
                var parent = areaParents.computeIfAbsent(category, k -> getParent(mRootItem, category));
                parent.getChildren().add(areaTreeItem);
            }
        }

        mAreaParents.clear();
        mAreaParents.putAll(mAreaParents);
        postPopulate(mRootItem);

        mTreeItemExpanderSet.forEach(checkBoxTreeItem -> {
            checkBoxTreeItem.setExpanded(true);
        });
    }

    private void postPopulate(CheckBoxTreeItem<MArea> treeItem) {
        var area = treeItem.getValue();

        if (treeItem.isLeaf()) {
            if (!mAreaEnabledListenerSet.contains(area)) {
                mAreaEnabledListenerSet.add(area);
            }

            if (mVisibilityPreferences.getBoolean(getPath(area), area.isEnabled())) {
                mCheckModel.check(treeItem);
                if (!area.isEnabled()) {
                    area.setEnabled(true);
                }
            } else {
                mCheckModel.clearCheck(treeItem);
                area.setEnabled(false);
            }
        } else {
            final String path = getPath(area);

            if (mExpandedPreferences.getBoolean(path, false)) {
                mTreeItemExpanderSet.add(treeItem);
            }

            if (!mTreeItemListenerSet.contains(treeItem)) {
                treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
                    mExpandedPreferences.putBoolean(path, newValue);
                });

                mTreeItemListenerSet.add(treeItem);
            }

            FXCollections.sort(treeItem.getChildren(), (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName()));

            for (var childTreeItem : treeItem.getChildren()) {
                postPopulate((CheckBoxTreeItem<MArea>) childTreeItem);
            }
        }
    }

    private static class Holder {

        private static final MAreaFilterManager INSTANCE = new MAreaFilterManager();
    }
}
