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
package org.mapton.worldwind;

import gov.nasa.worldwind.layers.Layer;
import gov.nasa.worldwind.layers.RenderableLayer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import org.apache.commons.lang3.BooleanUtils;
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
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.worldwind.api.LayerBundle;
import org.mapton.worldwind.api.WWHelper;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.PopOverWatcher;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class LayerView extends BorderPane implements MActivatable {

    private CheckModel<TreeItem<Layer>> mCheckModel;
    private final Preferences mExpandedPreferences;
    private TextField mFilterTextField;
    private final Set<Layer> mLayerEnabledListenerSet;
    private final Map<String, CheckBoxTreeItem<Layer>> mLayerParents;
    private WorldWindowPanel mMap;
    private Action mOptionsAction;
    private PopOver mOptionsPopOver;
    private CheckBoxTreeItem<Layer> mRootItem;
    private final Set<CheckBoxTreeItem<Layer>> mTreeItemExpanderSet;
    private final Set<CheckBoxTreeItem<Layer>> mTreeItemListenerSet;
    private CheckTreeView<Layer> mTreeView;
    private final Preferences mVisibilityPreferences;

    public static LayerView getInstance() {
        return Holder.INSTANCE;
    }

    private LayerView() {
        mVisibilityPreferences = NbPreferences.forModule(LayerView.class).node("layer_visibility");
        mExpandedPreferences = NbPreferences.forModule(LayerView.class).node("layer_group_expanded");
        mLayerParents = new TreeMap<>();
        mLayerEnabledListenerSet = Collections.synchronizedSet(new HashSet());
        mTreeItemListenerSet = Collections.synchronizedSet(new HashSet());
        mTreeItemExpanderSet = Collections.synchronizedSet(new HashSet());

        Platform.runLater(() -> {
            createUI();
        });
    }

    @Override
    public void activate() {
        mFilterTextField.requestFocus();
    }

    synchronized void refresh(WorldWindowPanel map) {
        if (mMap == null) {
            mMap = map;
            DelayedResetRunner delayedResetRunner = new DelayedResetRunner(100, () -> {
                refresh(map);
            });

            mMap.getCustomLayers().addListener((ListChangeListener.Change<? extends Layer> change) -> {
                delayedResetRunner.reset();
            });

            return;
        }

        FxHelper.runLater(() -> {
            mRootItem.getChildren().clear();
            mTreeItemListenerSet.clear();
            Map<String, CheckBoxTreeItem<Layer>> layerParents = new TreeMap<>();
            SortedList<Layer> sortedLayers = mMap.getCustomLayers().sorted((Layer o1, Layer o2) -> o1.getName().compareTo(o2.getName()));
            ObservableList<Layer> filteredLayers = FXCollections.observableArrayList();

            for (Layer layer : sortedLayers) {
                Object hiddenValue = layer.getValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER);
                boolean hidden = hiddenValue != null;
                if (hidden) {
                    hidden = BooleanUtils.toBoolean(layer.getValue(WWHelper.KEY_LAYER_HIDE_FROM_MANAGER).toString());
                }

                final String filter = mFilterTextField.getText();
                final boolean validFilter
                        = StringHelper.matchesSimpleGlob(getCategory(layer), filter, true, true)
                        || StringHelper.matchesSimpleGlob(layer.getName(), filter, true, true);

                if (!hidden && validFilter) {
                    filteredLayers.add(layer);
                }
            }

            for (Layer layer : filteredLayers) {
                CheckBoxTreeItem<Layer> layerTreeItem = new CheckBoxTreeItem<>(layer);
                String category = getCategory(layer);
                CheckBoxTreeItem<Layer> parent = layerParents.computeIfAbsent(category, k -> getParent(mRootItem, category));
                parent.getChildren().add(layerTreeItem);
            }
            mLayerParents.clear();
            mLayerParents.putAll(mLayerParents);
            postPopulate(mRootItem);

            mTreeItemExpanderSet.forEach((checkBoxTreeItem) -> {
                checkBoxTreeItem.setExpanded(true);
            });

            if (getCenter() != mTreeView) {
                setCenter(mTreeView);
                initListeners();
            }
        });
    }

    private void createUI() {
        Layer rootLayer = new RenderableLayer();
        rootLayer.setName("");
        mRootItem = new CheckBoxTreeItem<>(rootLayer);
        mTreeView = new CheckTreeView<>(mRootItem);
        mCheckModel = mTreeView.getCheckModel();
        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory(param -> new LayerTreeCell());

        mOptionsPopOver = new PopOver();
        mOptionsPopOver.setTitle(Dict.OPTIONS.toString());
        mOptionsPopOver.setArrowLocation(PopOver.ArrowLocation.LEFT_CENTER);
        mOptionsPopOver.setHeaderAlwaysVisible(true);
        mOptionsPopOver.setCloseButtonEnabled(false);
        mOptionsPopOver.setDetachable(false);
        mOptionsPopOver.setAnimated(true);

        MaskerPane maskerPane = new MaskerPane();
        maskerPane.setText(MDict.LOADING_LAYERS.toString());
        maskerPane.setProgress(-1);
        setCenter(maskerPane);

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.LAYER_SEARCH.toString());
        mFilterTextField.setMinWidth(20);
        final int iconSize = (int) (getIconSizeToolBarInt() * 0.8);

        ActionGroup selectActionGroup = new ActionGroup(Dict.SHOW.toString(), MaterialIcon._Image.REMOVE_RED_EYE.getImageView(iconSize),
                new Action(Dict.SHOW.toString(), (event) -> {
                    setChecked(mRootItem, true);
                }),
                new Action(Dict.HIDE.toString(), (event) -> {
                    setChecked(mRootItem, false);
                }),
                ActionUtils.ACTION_SEPARATOR,
                new Action(Dict.EXPAND.toString(), (event) -> {
                    setExpanded(mRootItem, true);
                }),
                new Action(Dict.COLLAPSE.toString(), (event) -> {
                    setExpanded(mRootItem, false);
                })
        );

        mOptionsAction = new Action(Dict.OPTIONS.toString(), event -> {
            if (mOptionsPopOver.isShowing()) {
                mOptionsPopOver.hide();
            } else {
                var layerBundle = (LayerBundle) getSelectedTreeItem().getValue().getValue("layerBundle");
                var optionsNode = layerBundle.getOptionsView();
                var button = (ButtonBase) event.getSource();
                var buttonBounds = button.localToScreen(button.getBoundsInLocal());
                double x = buttonBounds.getMaxX() + 2;
                double y = buttonBounds.getMinY() + buttonBounds.getHeight() / 4;//FIXME Is 4 strange?
                mOptionsPopOver.setContentNode(optionsNode);
                mOptionsPopOver.show(button, x, y);
                PopOverWatcher.getInstance().registerPopOver(mOptionsPopOver, button);
            }
        });
        mOptionsAction.setGraphic(MaterialIcon._Action.SETTINGS.getImageView(getIconSizeToolBarInt()));
        mOptionsAction.setDisabled(true);

        Collection<? extends Action> actions = Arrays.asList(
                selectActionGroup,
                mOptionsAction
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), iconSize);
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        BorderPane topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(toolBar);
        toolBar.setMinWidth(iconSize * 4.8);
        FxHelper.slimToolBar(toolBar);
        setTop(topBorderPane);
    }

    private String getCategory(Layer layer) {
        return StringUtils.defaultString((String) layer.getValue(WWHelper.KEY_LAYER_CATEGORY));
    }

    private String getLayerPath(Layer layer) {
        return String.format("%s/%s", getCategory(layer), layer.getName());
    }

    private CheckBoxTreeItem<Layer> getParent(CheckBoxTreeItem<Layer> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mLayerParents.containsKey(path)) {
                parent = mLayerParents.get(path);
            } else {
                Layer layer = new RenderableLayer();
                layer.setValue(WWHelper.KEY_LAYER_CATEGORY, path);
                layer.setName(segment);

                parent.getChildren().add(parent = mLayerParents.computeIfAbsent(sb.toString(), k -> new CheckBoxTreeItem<>(layer)));
            }

            sb.append("/");
        }

        return parent;
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
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(treeItem -> {
                        if (!isCategoryTreeItem(treeItem) && !treeItem.getValue().isEnabled()) {
                            treeItem.getValue().setEnabled(true);
                            mVisibilityPreferences.putBoolean(getLayerPath(treeItem.getValue()), true);
                        }
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(treeItem -> {
                        if (!isCategoryTreeItem(treeItem) && treeItem.getValue().isEnabled()) {
                            treeItem.getValue().setEnabled(false);
                            mVisibilityPreferences.putBoolean(getLayerPath(treeItem.getValue()), false);
                        }
                    });
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
        final Layer layer = treeItem.getValue();

        if (isCategoryTreeItem(treeItem)) {
            final String path = getCategory(layer);

            if (mExpandedPreferences.getBoolean(path, false)) {
                mTreeItemExpanderSet.add(treeItem);
            }

            if (!mTreeItemListenerSet.contains(treeItem)) {
                treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                    mExpandedPreferences.putBoolean(path, newValue);
                });

                mTreeItemListenerSet.add(treeItem);
            }

            Comparator<TreeItem<Layer>> c1 = (TreeItem<Layer> o1, TreeItem<Layer> o2) -> Boolean.compare(isCategoryTreeItem(o1), isCategoryTreeItem(o2));
            Comparator<TreeItem<Layer>> c2 = (TreeItem<Layer> o1, TreeItem<Layer> o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

            treeItem.getChildren().sort(c1.reversed().thenComparing(c2));

            for (TreeItem<Layer> childTreeItem : treeItem.getChildren()) {
                postPopulate((CheckBoxTreeItem<Layer>) childTreeItem);
            }
        } else {
            if (!mLayerEnabledListenerSet.contains(layer)) {
                mLayerEnabledListenerSet.add(layer);
                layer.addPropertyChangeListener("Enabled", pce -> {
                    Platform.runLater(() -> {
                        if ((boolean) pce.getNewValue()) {
                            mCheckModel.check(treeItem);
                        } else {
                            mCheckModel.clearCheck(treeItem);
                        }
                    });
                });
            }

            if (mVisibilityPreferences.getBoolean(getLayerPath(layer), layer.isEnabled())) {
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
        if (!isCategoryTreeItem(treeItem)) {
            if (checked) {
                mCheckModel.check(treeItem);
            } else {
                mCheckModel.clearCheck(treeItem);
            }
        }

        for (TreeItem<Layer> childTreeItem : treeItem.getChildren()) {
            setChecked((CheckBoxTreeItem<Layer>) childTreeItem, checked);
        }
    }

    private void setExpanded(CheckBoxTreeItem<Layer> treeItem, boolean expanded) {
        if (isCategoryTreeItem(treeItem)) {
            if (treeItem != mRootItem) {
                treeItem.setExpanded(expanded);
            }

            for (TreeItem<Layer> childTreeItem : treeItem.getChildren()) {
                setExpanded((CheckBoxTreeItem<Layer>) childTreeItem, expanded);
            }
        }
    }

    private static class Holder {

        private static final LayerView INSTANCE = new LayerView();
    }

    class LayerTreeCell extends CheckBoxTreeCell<Layer> {

        public LayerTreeCell() {
            createUI();
        }

        @Override
        public void updateItem(Layer layer, boolean empty) {
            super.updateItem(layer, empty);

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
