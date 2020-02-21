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
import java.util.Locale;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.Mapton;
import org.mapton.api.report.MSplitNavSettings.TitleMode;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;
import static se.trixon.almond.util.fx.FxHelper.getScaledFontSize;

/**
 *
 * @author Patrik Karlström
 * @param <T extends MSplitNavType>
 */
public class MSplitNavPane<T extends MSplitNavType> extends BorderPane {

    private final Class<? extends MSplitNavType> mClass;
    private BorderPane mDetailBorderPane;
    private StackPane mDetailTopPane;
    private TextField mFilterTextField;
    private BorderPane mMasterBorderPane;
    private final TreeMap<String, TreeItem<T>> mParents = new TreeMap<>();
    private Label mPlaceholderLabel;
    private final Preferences mPreferences;
    private Label mTitleLabel;
    private ToolBar mToolBar;
    private TreeView<T> mTreeView;
    private final String mTypeName;

    public MSplitNavPane(Class<T> clazz, String typeName) {
        mClass = clazz;
        mTypeName = typeName;
        mPreferences = NbPreferences.forModule(mClass).node("expanded_state_" + mClass.getName());

        createUI();
    }

    private void createUI() {
        mPlaceholderLabel = new Label();

        mTreeView = new TreeView<>();
        mTreeView.setShowRoot(false);
        mTreeView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends TreeItem<T>> c) -> {
            if (mTreeView.getSelectionModel().isEmpty()) {
                mDetailBorderPane.setTop(null);
                mDetailBorderPane.setCenter(mPlaceholderLabel);
            } else {
                Platform.runLater(() -> {
                    load(mTreeView.getSelectionModel().getSelectedItem().getValue());
                });
            }
        });

        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(String.format("%s %s", Dict.SEARCH.toString(), mTypeName.toLowerCase(Locale.getDefault())));
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            populate();
        });

        mMasterBorderPane = new BorderPane(mTreeView);
        mMasterBorderPane.setPrefWidth(FxHelper.getUIScaled(300));
        mMasterBorderPane.setTop(mFilterTextField);

        mDetailTopPane = new StackPane();

        mTitleLabel = new Label();
        mTitleLabel.setPrefHeight(Mapton.getIconSizeToolBarInt() * 1.3);
        mTitleLabel.setStyle(String.format("-fx-font-size: %dpx;", (int) (getScaledFontSize() * 1.5)));

        mToolBar = new ToolBar();

        FxHelper.slimToolBar(mToolBar);

        mDetailBorderPane = new BorderPane(mPlaceholderLabel);

        setLeft(mMasterBorderPane);
        setCenter(mDetailBorderPane);

        Lookup.getDefault().lookupResult(mClass).addLookupListener((LookupEvent ev) -> {
            populate();
        });

        populate();
    }

    private TreeItem<T> getParent(TreeItem<T> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mParents.containsKey(path)) {
                parent = mParents.get(path);
            } else {
                T type = (T) new MSplitNavType() {
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
                    public MSplitNavSettings getSplitNavSettings() {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }

                    @Override
                    public void setName(String name) {
                    }

                    @Override
                    public void setParent(String parent) {
                    }

                    @Override
                    public String toString() {
                        return getName();
                    }
                };

                parent.getChildren().add(parent = mParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(type)));
            }

            sb.append("/");
        }

        return parent;
    }

    private void load(T selectedType) {
        final Node node = selectedType.getNode();
        if (node != null) {
            mDetailBorderPane.setTop(mDetailTopPane);
            mDetailBorderPane.setCenter(node);
            loadSettings(selectedType);

            selectedType.onSelect();
        } else {
            mDetailBorderPane.setTop(null);
            mDetailBorderPane.setCenter(null);
        }
    }

    private void loadSettings(T item) {
        setTitle(item);

        MSplitNavSettings settings = item.getSplitNavSettings();
        final Color color = settings.getTitleColor();
        if (color != null) {
            var background = FxHelper.createBackground(color);
            mDetailTopPane.setBackground(background);
            mToolBar.setStyle(String.format("-fx-background-color: #%s;", FxHelper.colorToHexRGBA(color)));
        }

        final ObservableList<Node> children = mDetailTopPane.getChildren();
        children.clear();

        mToolBar.getItems().clear();
        if (!settings.getToolBarItems().isEmpty()) {
            children.add(mToolBar);
            mToolBar.getItems().setAll(settings.getToolBarItems());
        }

        if (settings.getTitleMode() != TitleMode.NONE) {
            children.add(mTitleLabel);
        }
    }

    private void populate() {
        mParents.clear();

        T rootType = (T) new MSplitNavType() {
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

            @Override
            public MSplitNavSettings getSplitNavSettings() {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            @Override
            public void setName(String name) {
            }

            @Override
            public void setParent(String parent) {
            }
        };

        TreeItem<T> root = new TreeItem<>(rootType);

//        new Thread(() -> {
        final String filter = mFilterTextField.getText();
        Lookup.getDefault().lookupAll(mClass).forEach((type) -> {
            final boolean validFilter
                    = StringHelper.matchesSimpleGlob(type.getParent(), filter, true, true)
                    || StringHelper.matchesSimpleGlob(type.getName(), filter, true, true);

            if (validFilter) {
                TreeItem<T> treeItem = new TreeItem<>((T) type);
                String category = type.getParent();
                TreeItem<T> parent = mParents.computeIfAbsent(category, k -> getParent(root, category));
                parent.getChildren().add(treeItem);
            }
        });

//            Platform.runLater(() -> {
        postPopulate(root);
        mTreeView.setRoot(root);
//            });
//        }).start();
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

    private void setTitle(T item) {
        MSplitNavSettings settings = item.getSplitNavSettings();

        String titlePrefix;
        switch (settings.getTitleMode()) {
            case FULL_PATH:
                titlePrefix = item.getParent();
                break;

            case NAME_WITH_PARENT:
                if (StringUtils.contains(item.getParent(), "/")) {
                    titlePrefix = StringUtils.substringAfterLast(item.getParent(), "/");
                } else {
                    titlePrefix = item.getParent();
                }
                break;

            default:
                titlePrefix = "";
        }

        if (titlePrefix.length() > 0) {
            titlePrefix = titlePrefix + "/";
        }

        if (settings.getTitle() == null) {
            mTitleLabel.setText(titlePrefix + item.getName());
        } else {
            mTitleLabel.setText(titlePrefix + settings.getTitle());
        }
    }
}
