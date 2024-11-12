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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckModel;
import org.controlsfx.control.CheckTreeView;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionGroup;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MActivatable;
import org.mapton.api.MDict;
import org.mapton.api.MKey;
import org.mapton.api.MRunnable;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.api.ui.MOptionsPopOver;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LayerObjectView extends BorderPane implements MActivatable {

    private CheckModel<TreeItem<Layer>> mCheckModel;
    private ContextMenu mContextMenu;
    private final Preferences mExpandedPreferences;
    private TextField mFilterTextField;
    private final Set<Layer> mLayerEnabledListenerSet;
    private final Map<String, CheckBoxTreeItem<Layer>> mLayerParents;
    private final HashMap<Layer, CheckBoxTreeCell<Layer>> mLayerToCheckBoxTreeCell = new HashMap<>();
    private WorldWindowPanel mMap;
    private Action mOptionsAction;
    private MOptionsPopOver mOptionsPopOver;
    private CheckBoxTreeItem<Layer> mRootItem;
    private ToolBar mToolBar;
    private final Set<CheckBoxTreeItem<Layer>> mTreeItemExpanderSet;
    private final Set<CheckBoxTreeItem<Layer>> mTreeItemListenerSet;
    private CheckTreeView<Layer> mTreeView;
    private final Preferences mVisibilityPreferences;

    public static LayerObjectView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerObjectView() {
        mVisibilityPreferences = NbPreferences.forModule(LayerObjectView.class).node("layer_visibility");
        mExpandedPreferences = NbPreferences.forModule(LayerObjectView.class).node("layer_group_expanded");
        mLayerParents = new TreeMap<>();
        mLayerEnabledListenerSet = Collections.synchronizedSet(new HashSet<>());
        mTreeItemListenerSet = Collections.synchronizedSet(new HashSet<>());
        mTreeItemExpanderSet = Collections.synchronizedSet(new HashSet<>());

        Platform.runLater(() -> {
            setCenter(new ProgressIndicator());
        });

        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_INITIALIZED, () -> {
            FxHelper.runLater(() -> {
                createUI();
                refresh(mMap);
            });
        });
    }

    @Override
    public void activate() {
        mFilterTextField.requestFocus();
    }

    synchronized void refresh(WorldWindowPanel map) {
        if (ObjectUtils.anyNull(mMap)) {
            if (map == null) {
                return;
            }
            mMap = map;
            var delayedResetRunner = new DelayedResetRunner(100, () -> {
                refresh(map);
            });

            mMap.getCustomLayers().addListener((ListChangeListener.Change<? extends Layer> change) -> {
                delayedResetRunner.reset();
            });

            return;
        }

        if (mRootItem == null) {
            return;
        }

        mRootItem.getChildren().clear();
        mTreeItemListenerSet.clear();
        var layerParents = new TreeMap<String, CheckBoxTreeItem<Layer>>();
        var sortedLayers = mMap.getCustomLayers().sorted((o1, o2) -> o1.getName().compareTo(o2.getName()));
        ObservableList<Layer> filteredLayers = FXCollections.observableArrayList();

        for (var layer : sortedLayers) {
            var hiddenValue = layer.getValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER);
            boolean hidden = hiddenValue != null;
            if (hidden) {
                hidden = BooleanUtils.toBoolean(layer.getValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER).toString());
            }

            final String filter = mFilterTextField.getText();
            final boolean validFilter
                    = StringHelper.matchesSimpleGlob(WWHelper.getCategory(layer), filter, true, true)
                    || StringHelper.matchesSimpleGlob(layer.getName(), filter, true, true);

            if (!hidden && validFilter) {
                filteredLayers.add(layer);
            }
        }

        for (var layer : filteredLayers) {
            var layerTreeItem = new CheckBoxTreeItem<Layer>(layer);
            String category = WWHelper.getCategory(layer);
            var parent = layerParents.computeIfAbsent(category, k -> getParent(mRootItem, category));
            parent.getChildren().add(layerTreeItem);
        }
        mLayerParents.clear();
        mLayerParents.putAll(mLayerParents);

        postPopulate(mRootItem);

        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED, () -> {
            FxHelper.runLater(() -> {
                restoreLayerVisibility(mRootItem);
                setCenter(mTreeView);
            });
        });

        mTreeItemExpanderSet.forEach(checkBoxTreeItem -> {
            checkBoxTreeItem.setExpanded(true);
        });

        if (getCenter() != mTreeView) {
            initListeners();
        }
    }

    private void createUI() {
        var rootLayer = new RenderableLayer();
        rootLayer.setName("");
        mRootItem = new CheckBoxTreeItem<>(rootLayer);
        mTreeView = new CheckTreeView<>(mRootItem);
        mCheckModel = mTreeView.getCheckModel();
        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory(param -> new LayerTreeCell());

        var runOnceChecker = new HashSet<Node>();

        mOptionsPopOver = new MOptionsPopOver();
        mOptionsPopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        mOptionsPopOver.setOnShowing(windowEvent -> {
            var layerBundle = (LayerBundle) getSelectedTreeItem().getValue().getValue("layerBundle");
            var optionsView = layerBundle.getOptionsView();
            mOptionsPopOver.setContentNode(optionsView);
            if (optionsView instanceof MRunnable r) {
                FxHelper.runLaterDelayed(50, () -> {
                    if (!runOnceChecker.contains(optionsView)) {
                        runOnceChecker.add(optionsView);
                        r.runOnce();
                    }
                    r.run();
                });
            }
        });

        mOptionsAction = mOptionsPopOver.getAction();
        mOptionsAction.setDisabled(true);

        var maskerPane = new MaskerPane();
        maskerPane.setText(MDict.LOADING_LAYERS.toString());
        maskerPane.setProgress(-1);
        setCenter(maskerPane);

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.LAYER_SEARCH.toString());
        mFilterTextField.setMinWidth(20);
        final int iconSize = (int) (getIconSizeToolBarInt() * 0.8);

        var selectActionGroup = new ActionGroup(Dict.SHOW.toString(), MaterialIcon._Image.REMOVE_RED_EYE.getImageView(iconSize),
                new Action(Dict.SHOW.toString(), actionEvent -> {
                    setChecked(getSelectedCheckBoxTreeItem(), true);
                }),
                new Action(Dict.HIDE.toString(), actionEvent -> {
                    setChecked(getSelectedCheckBoxTreeItem(), false);
                }),
                ActionUtils.ACTION_SEPARATOR,
                new Action(Dict.EXPAND.toString(), actionEvent -> {
                    setExpanded(getSelectedCheckBoxTreeItem(), true);
                }),
                new Action(Dict.COLLAPSE.toString(), actionEvent -> {
                    setExpanded(getSelectedCheckBoxTreeItem(), false);
                })
        );

        var actions = Arrays.asList(
                selectActionGroup,
                mOptionsAction
        );

        mToolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(mToolBar.getItems().stream(), iconSize);
        FxHelper.undecorateButtons(mToolBar.getItems().stream());
        FxHelper.slimToolBar(mToolBar);

        var topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(mToolBar);
        mToolBar.setMinWidth(iconSize * 4.8);
        setTop(topBorderPane);

        var optionsItem = new MenuItem(Dict.OPTIONS.toString());
        optionsItem.disableProperty().bind(mOptionsAction.disabledProperty());
        optionsItem.setOnAction(actionEvent -> {
            mOptionsAction.handle(new ActionEvent(getOptionsToolBarButton(), this));

        });

        mContextMenu = new ContextMenu(optionsItem);
    }

    private ButtonBase getOptionsToolBarButton() {
        return FxHelper.getButtonForAction(mOptionsAction, mToolBar.getItems());
    }

    private CheckBoxTreeItem<Layer> getParent(CheckBoxTreeItem<Layer> parent, String category) {
        var categorySegments = StringUtils.split(category, "/");
        var sb = new StringBuilder();

        for (var segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mLayerParents.containsKey(path)) {
                parent = mLayerParents.get(path);
            } else {
                var layer = new RenderableLayer();
                layer.setValue(WWHelper.KEY_LAYER_CATEGORY, path);
                layer.setName(segment);

                parent.getChildren().add(parent = mLayerParents.computeIfAbsent(sb.toString(), k -> new CheckBoxTreeItem<>(layer)));
            }

            sb.append("/");
        }

        return parent;
    }

    private CheckBoxTreeItem<Layer> getSelectedCheckBoxTreeItem() {
        return (CheckBoxTreeItem<Layer>) mTreeView.getSelectionModel().getSelectedItem();
    }

    private TreeItem<Layer> getSelectedTreeItem() {
        return mTreeView.getSelectionModel().getSelectedItem();
    }

    private void initListeners() {
        mTreeView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                var layerBundle = (LayerBundle) getSelectedTreeItem().getValue().getValue("layerBundle");
                var optionsNode = layerBundle.getOptionsView();
                mOptionsAction.setDisabled(optionsNode == null);
            } catch (Exception e) {
                mOptionsAction.setDisabled(true);
            }
        });

        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            refresh(mMap);
        });

        mCheckModel.getCheckedItems().addListener((ListChangeListener.Change<? extends TreeItem<Layer>> c) -> {
            synchronized (this) {
                while (c.next()) {
                    if (c.wasAdded()) {
                        c.getAddedSubList().forEach(treeItem -> {
                            if (!isCategoryTreeItem(treeItem) && !treeItem.getValue().isEnabled()) {
                                treeItem.getValue().setEnabled(true);
                                mVisibilityPreferences.putBoolean(WWHelper.getLayerPath(treeItem.getValue()), true);
                            }
                        });
                    } else if (c.wasRemoved()) {
                        c.getRemoved().forEach(treeItem -> {
                            if (!isCategoryTreeItem(treeItem) && treeItem.getValue().isEnabled()) {
                                treeItem.getValue().setEnabled(false);
                                mVisibilityPreferences.putBoolean(WWHelper.getLayerPath(treeItem.getValue()), false);
                            }
                        });
                    }
                }
            }
        });

        Mapton.getGlobalState().addListener(evt -> {
            Platform.runLater(() -> {
                setChecked(mRootItem, false);
            });
        }, MKey.MAP_CLEAR_ALL_LAYERS);
    }

    private boolean isCategoryTreeItem(TreeItem<Layer> treeItem) {
        return !treeItem.getChildren().isEmpty();
    }

    private void postPopulate(CheckBoxTreeItem<Layer> treeItem) {
        var layer = treeItem.getValue();

        if (isCategoryTreeItem(treeItem)) {
            final String path = WWHelper.getCategory(layer);

            if (mExpandedPreferences.getBoolean(path, false)) {
                mTreeItemExpanderSet.add(treeItem);
            }

            if (!mTreeItemListenerSet.contains(treeItem)) {
                treeItem.expandedProperty().addListener((observable, oldValue, newValue) -> {
                    mExpandedPreferences.putBoolean(path, newValue);
                });

                mTreeItemListenerSet.add(treeItem);
            }

            Comparator<TreeItem<Layer>> c1 = (o1, o2) -> Boolean.compare(isCategoryTreeItem(o1), isCategoryTreeItem(o2));
            Comparator<TreeItem<Layer>> c2 = (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

            treeItem.getChildren().sort(c1.reversed().thenComparing(c2));

            for (var childTreeItem : treeItem.getChildren()) {
                postPopulate((CheckBoxTreeItem<Layer>) childTreeItem);
            }
        } else {
            if (!mLayerEnabledListenerSet.contains(layer)) {
                mLayerEnabledListenerSet.add(layer);
                layer.addPropertyChangeListener("Enabled", pce -> {
                    boolean newValue = (boolean) pce.getNewValue();
                    if (newValue) {
                        mCheckModel.check(treeItem);
                    } else {
                        try {
                            mCheckModel.clearCheck(treeItem);
                        } catch (UnsupportedOperationException e) {
                            System.err.println("Error detected in WWLayerObjectView while clearing check");
                            System.err.println(e.toString());
                        }
                    }

                    mVisibilityPreferences.putBoolean(WWHelper.getLayerPath(treeItem.getValue()), newValue);
                });
            }
        }
    }

    private void restoreLayerVisibility(CheckBoxTreeItem<Layer> treeItem) {
        if (isCategoryTreeItem(treeItem)) {
            for (var childTreeItem : treeItem.getChildren()) {
                restoreLayerVisibility((CheckBoxTreeItem<Layer>) childTreeItem);
            }
        } else {
            var layer = treeItem.getValue();

            if (mVisibilityPreferences.getBoolean(WWHelper.getLayerPath(layer), layer.isEnabled())) {
                mCheckModel.check(treeItem);
                if (!layer.isEnabled()) {
                    layer.setEnabled(true);
                }
            } else {
                mCheckModel.clearCheck(treeItem);
                layer.setEnabled(false);
            }
        }
    }

    private void setChecked(CheckBoxTreeItem<Layer> treeItem, boolean checked) {
        if (treeItem == null) {
            return;
        }

        if (!isCategoryTreeItem(treeItem)) {
            if (checked) {
                mCheckModel.check(treeItem);
            } else {
                mCheckModel.clearCheck(treeItem);
            }
        }

        for (var childTreeItem : treeItem.getChildren()) {
            setChecked((CheckBoxTreeItem<Layer>) childTreeItem, checked);
        }
    }

    private void setExpanded(CheckBoxTreeItem<Layer> treeItem, boolean expanded) {
        if (treeItem == null) {
            return;
        }

        if (isCategoryTreeItem(treeItem)) {
            if (treeItem != mRootItem) {
                treeItem.setExpanded(expanded);
            }

            for (var childTreeItem : treeItem.getChildren()) {
                setExpanded((CheckBoxTreeItem<Layer>) childTreeItem, expanded);
            }
        }
    }

    private static class Holder {

        private static final LayerObjectView INSTANCE = new LayerObjectView();
    }

    class LayerTreeCell extends CheckBoxTreeCell<Layer> {

        public LayerTreeCell() {
            createUI();
        }

        @Override
        public void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);
            mLayerToCheckBoxTreeCell.put(layer, this);
            var treeItem = getTreeItem();
            if (treeItem != null) {
                if (treeItem.isLeaf()) {
                    setContextMenu(mContextMenu);
                } else {
                    setContextMenu(null);
                }
            }

            if (layer == null || empty) {
                clearContent();
            } else {
                addContent(layer);
            }
        }

        private void addContent(Layer layer) {
            setText(layer.getName());
            setOnMouseClicked(mouseEvent -> {
                if (mouseEvent.getClickCount() == 2 && mouseEvent.getButton() == MouseButton.PRIMARY) {
                    if (layer.hasKey(WWHelper.KEY_FAST_OPEN)) {
                        Mapton.getGlobalState().put(MKey.LAYER_FAST_OPEN_TOOL, layer.getValue(WWHelper.KEY_FAST_OPEN));
                        if (!mouseEvent.isAltDown()) {
                            mCheckModel.check(this.getTreeItem());
                        }
                    }
                }
            });
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
            setOnMouseClicked(null);
        }

        private void createUI() {
        }
    }
}
