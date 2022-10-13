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
package org.mapton.core.ui.area;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.mapton.api.MArea;
import org.mapton.api.MAreaManager;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class AreaPane extends BorderPane {

    private final Set<MArea> mAreaEnabledListenerSet;
    private final MAreaManager mAreaManager = MAreaManager.getInstance();
    private final Map<String, CheckBoxTreeItem<MArea>> mAreaParents;
    private CheckModel<TreeItem<MArea>> mCheckModel;
    private final Preferences mExpandedPreferences;
    private CheckBoxTreeItem<MArea> mRootItem;
    private final Set<CheckBoxTreeItem<MArea>> mTreeItemExpanderSet;
    private final Set<CheckBoxTreeItem<MArea>> mTreeItemListenerSet;
    private CheckTreeView<MArea> mTreeView;
    private final Preferences mVisibilityPreferences;

    public AreaPane() {
        mVisibilityPreferences = NbPreferences.forModule(AreaPane.class).node("area_visibility");
        mExpandedPreferences = NbPreferences.forModule(AreaPane.class).node("area_expanded");
        mAreaParents = new TreeMap<>();
        mAreaEnabledListenerSet = Collections.synchronizedSet(new HashSet());
        mTreeItemListenerSet = Collections.synchronizedSet(new HashSet());
        mTreeItemExpanderSet = Collections.synchronizedSet(new HashSet());

        createUI();
        initListeners();
        populate();
    }

    private void createUI() {
        var browseAction = new Action(Dict.BROWSE.toString(), actionEvent -> {
        });
        browseAction.setGraphic(MaterialIcon._Social.PUBL.getImageView(getIconSizeToolBarInt()));
        browseAction.setDisabled(true);

        var actions = Arrays.asList(
                browseAction,
                ActionUtils.ACTION_SPAN
        );

        var toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        FxHelper.slimToolBar(toolBar);

        var rootArea = new MArea("");
        mRootItem = new CheckBoxTreeItem<>(rootArea);
        mTreeView = new CheckTreeView<>(mRootItem);
        mCheckModel = mTreeView.getCheckModel();
        mTreeView.setShowRoot(false);

        setTop(toolBar);
        setCenter(mTreeView);
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

    private void initListeners() {
        mAreaManager.getItems().addListener((ListChangeListener.Change<? extends MArea> c) -> {
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

        for (var area : mAreaManager.getItems()) {
            area.setName(StringUtils.substringAfterLast(area.getKey(), "/"));
            var areaTreeItem = new CheckBoxTreeItem<MArea>(area);
            String category = getCategory(area);
            var parent = areaParents.computeIfAbsent(category, k -> getParent(mRootItem, category));

            parent.getChildren().add(areaTreeItem);
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
}
