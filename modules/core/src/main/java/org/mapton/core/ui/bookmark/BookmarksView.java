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
package org.mapton.core.ui.bookmark;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.prefs.Preferences;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToolBar;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MActivatable;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MDict;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeContextMenu;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarksView extends BorderPane implements MActivatable {

    private final Map<String, TreeItem<MBookmark>> mBookmarkParents = new TreeMap<>();
    private BookmarkEditor mEditor;
    private TextField mFilterTextField;
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();
    private final PopOver mPopOver;
    private final Preferences mPreferences = NbPreferences.forModule(BookmarksView.class).node("expanded_state");
    private final TreeView<MBookmark> mTreeView = new TreeView<>();

    public BookmarksView(PopOver popOver) {
        mPopOver = popOver;
        mEditor = new BookmarkEditor();
        createUI();

        mManager.dbLoad(mFilterTextField.getText(), true);
        populate();
        addListeners();
    }

    @Override
    public void activate() {
        mFilterTextField.requestFocus();
    }

    private void addListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            mManager.dbLoad(newValue, true);
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            populate();
        });

        mTreeView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<MBookmark>> observable, TreeItem<MBookmark> oldValue, TreeItem<MBookmark> newValue) -> {
            try {
                bookmarkGoTo(newValue.getValue());
            } catch (NullPointerException e) {
                //nvm
            }
        });
    }

    private void bookmarkEdit() {
        MBookmark bookmark = getSelectedBookmark();
        if (bookmark != null) {
            if (bookmark.isCategory()) {
                mEditor.editCategory(bookmark.getCategory());
            } else {
                mEditor.editBookmark(bookmark);
            }
        }
    }

    private void bookmarkGoTo(MBookmark bookmark) {
        try {
            mManager.goTo(bookmark);
        } catch (NullPointerException ex) {
            // nvm
        } catch (ClassNotFoundException | SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void createUI() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.BOOKMARKS_SEARCH.toString());
        mFilterTextField.setMinWidth(20);

        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory(param -> new BookmarkTreeCell());

        Collection<? extends Action> actions = Arrays.asList(
                new FileImportAction(mPopOver).getAction(this),
                new FileExportAction(mPopOver).getAction(this)
        );

        ToolBar toolBar = ActionUtils.createToolBar(actions, ActionUtils.ActionTextBehavior.HIDE);
        FxHelper.adjustButtonWidth(toolBar.getItems().stream(), getIconSizeToolBarInt());
        FxHelper.undecorateButtons(toolBar.getItems().stream());
        BorderPane topBorderPane = new BorderPane(mFilterTextField);
        topBorderPane.setRight(toolBar);
        toolBar.setMinWidth(getIconSizeToolBarInt() * 3.5);
        FxHelper.slimToolBar(toolBar);
        setTop(topBorderPane);
        setCenter(mTreeView);
    }

    private TreeItem<MBookmark> getParent(TreeItem<MBookmark> parent, String category) {
        String[] categorySegments = StringUtils.split(category, "/");
        StringBuilder sb = new StringBuilder();

        for (String segment : categorySegments) {
            sb.append(segment);
            String path = sb.toString();

            if (mBookmarkParents.containsKey(path)) {
                parent = mBookmarkParents.get(path);
            } else {
                MBookmark bookmark = new MBookmark();
                bookmark.setCategory(path);
                bookmark.setName(segment);

                parent.getChildren().add(parent = mBookmarkParents.computeIfAbsent(sb.toString(), k -> new TreeItem<>(bookmark)));
            }

            sb.append("/");
        }

        return parent;
    }

    private MBookmark getSelectedBookmark() {
        TreeItem<MBookmark> item = mTreeView.getSelectionModel().getSelectedItem();

        return item != null ? item.getValue() : null;
    }

    private void populate() {
        MBookmark rootMark = new MBookmark();
        rootMark.setName("");
        TreeItem<MBookmark> root = new TreeItem<>(rootMark);
        Map<String, TreeItem<MBookmark>> bookmarkParents = new TreeMap<>();

        for (MBookmark bookmark : mManager.getItems()) {
            TreeItem<MBookmark> bookmarkTreeItem = new TreeItem<>(bookmark);
            String category = bookmark.getCategory();
            TreeItem<MBookmark> parent = bookmarkParents.computeIfAbsent(category, k -> getParent(root, category));
            parent.getChildren().add(bookmarkTreeItem);
        }
        mBookmarkParents.clear();
        mBookmarkParents.putAll(mBookmarkParents);

        postPopulate(root, "");
        mTreeView.setRoot(root);
    }

    private void postPopulate(TreeItem<MBookmark> treeItem, String level) {
        final MBookmark value = treeItem.getValue();
        final String path = String.format("%s/%s", value.getCategory(), value.getName());
        treeItem.setExpanded(mPreferences.getBoolean(path, false));

        treeItem.expandedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            mPreferences.putBoolean(path, newValue);
        });

        Comparator<TreeItem<MBookmark>> c1 = (o1, o2) -> Boolean.compare(o1.getChildren().isEmpty(), o2.getChildren().isEmpty());
        Comparator<TreeItem<MBookmark>> c2 = (o1, o2) -> o1.getValue().getName().compareTo(o2.getValue().getName());

        treeItem.getChildren().sort(c1.thenComparing(c2));

        for (TreeItem<MBookmark> childTreeItem : treeItem.getChildren()) {
            postPopulate(childTreeItem, level + "-");
        }
    }

    class BookmarkTreeCell extends TreeCell<MBookmark> {

        private Menu mContextCopyMenu;
        private Menu mContextOpenMenu;

        public BookmarkTreeCell() {
            createUI();
        }

        @Override
        protected void updateItem(MBookmark bookmark, boolean empty) {
            super.updateItem(bookmark, empty);

            if (bookmark == null || empty) {
                clearContent();
            } else {
                addContent(bookmark);
            }
        }

        private void addContent(MBookmark bookmark) {
            setText(bookmark.getName());
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            Color color = Mapton.options().getIconColorForBackground();
            Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                bookmarkEdit();
            });
            editAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(getIconSizeContextMenu(), color));

            Action editColorAction = new Action(Dict.COLOR.toString(), (ActionEvent event) -> {
                mEditor.editColor(getSelectedBookmark().getCategory());
            });
            editColorAction.setGraphic(MaterialIcon._Image.COLORIZE.getImageView(getIconSizeContextMenu(), color));

            Action editZoomAction = new Action(Dict.ZOOM.toString(), (ActionEvent event) -> {
                mEditor.editZoom(getSelectedBookmark().getCategory());
            });
            editZoomAction.setGraphic(MaterialIcon._Editor.FORMAT_SIZE.getImageView(getIconSizeContextMenu(), color));

            Action zoomExtentAction = new Action(Dict.ZOOM_EXTENTS.toString(), (ActionEvent event) -> {
                Mapton.getEngine().fitToBounds(mManager.getExtents(getSelectedBookmark().getCategory()));
            });

            Action removeAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
                mEditor.remove(getSelectedBookmark());
            });

            Action removeAllAction = new Action(Dict.REMOVE_ALL.toString(), (ActionEvent event) -> {
                mEditor.removeAll();
            });

            Collection<? extends Action> actions = Arrays.asList(
                    editAction,
                    editColorAction,
                    editZoomAction,
                    zoomExtentAction,
                    ActionUtils.ACTION_SEPARATOR,
                    removeAction,
                    removeAllAction
            );

            ContextMenu contextMenu = ActionUtils.createContextMenu(actions);
            mContextCopyMenu = new Menu(MDict.COPY_LOCATION.toString());
            mContextOpenMenu = new Menu(MDict.OPEN_LOCATION.toString());

            contextMenu.getItems().add(4, mContextOpenMenu);
            contextMenu.getItems().add(4, mContextCopyMenu);

            setOnMousePressed((MouseEvent event) -> {
                getScene().getWindow().requestFocus();
                mTreeView.requestFocus();
                MBookmark b = this.getItem();

                if (b != null) {
                    Mapton.getEngine().setLockedLatitude(b.getLatitude());
                    Mapton.getEngine().setLockedLongitude(b.getLongitude());

                    if (event.isSecondaryButtonDown()) {
                        mContextCopyMenu.setDisable(b.isCategory());
                        mContextOpenMenu.setDisable(b.isCategory());
                        editColorAction.setDisabled(!b.isCategory());
                        editZoomAction.setDisabled(!b.isCategory());
                        zoomExtentAction.setDisabled(!b.isCategory());

                        if (!b.isCategory()) {
                            Mapton.getEngine().setLatitude(b.getLatitude());
                            Mapton.getEngine().setLongitude(b.getLongitude());
                        }

                        contextMenu.show(this, event.getScreenX(), event.getScreenY());
                    } else if (event.isPrimaryButtonDown()) {
                        contextMenu.hide();
                        bookmarkGoTo(b);
                    }
                }
            });

            Lookup.getDefault().lookupResult(MContextMenuItem.class).addLookupListener((LookupEvent ev) -> {
                populateContextProviders();
            });

            populateContextProviders();
        }

        private void populateContextProviders() {
            mContextCopyMenu.getItems().clear();
            mContextOpenMenu.getItems().clear();

            for (MContextMenuItem provider : Lookup.getDefault().lookupAll(MContextMenuItem.class)) {
                MenuItem item = new MenuItem(provider.getName());
                switch (provider.getType()) {
                    case COPY:
                        mContextCopyMenu.getItems().add(item);
                        item.setOnAction((ActionEvent event) -> {
                            String s = provider.getUrl();
                            Mapton.getLog().v("Open location", s);
                            SystemHelper.copyToClipboard(s);
                        });
                        break;

                    case OPEN:
                        mContextOpenMenu.getItems().add(item);
                        item.setOnAction((ActionEvent event) -> {
                            String s = provider.getUrl();
                            Mapton.getLog().v("Copy location", s);
                            SystemHelper.desktopBrowse(s);
                        });
                        break;
                }
            }

            mContextCopyMenu.getItems().sorted((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextCopyMenu.setVisible(!mContextCopyMenu.getItems().isEmpty());

            mContextOpenMenu.getItems().sorted((o1, o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextOpenMenu.setVisible(!mContextOpenMenu.getItems().isEmpty());
        }
    }
}
