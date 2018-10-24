/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.ResourceBundle;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.textfield.TextFields;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.api.MBookmark;
import se.trixon.mapton.api.MBookmarkManager;
import se.trixon.mapton.api.MContextMenuItem;
import se.trixon.mapton.api.Mapton;
import static se.trixon.mapton.api.Mapton.getIconSizeContextMenu;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkView extends BorderPane {

    private final ResourceBundle mBundle = NbBundle.getBundle(BookmarkView.class);

    private final Font mDefaultFont = Font.getDefault();
    private TextField mFilterTextField;
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();
    private final TreeView<MBookmark> mTreeView = new TreeView<>();

    public BookmarkView() {
        createUI();
        addListeners();

        mManager.dbLoad(mFilterTextField.getText());
        populate();
    }

    private void addListeners() {
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            mManager.dbLoad(newValue);

        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            populate();
        });

        mTreeView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<MBookmark>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<MBookmark>> observable, TreeItem<MBookmark> oldValue, TreeItem<MBookmark> newValue) {
                if (ObjectUtils.allNotNull(newValue, newValue.getValue().getLatitude(), newValue.getValue().getLongitude())) {
                    bookmarkGoTo(newValue.getValue());
                }
            }
        });
    }

    private void bookmarkEdit() {
        MBookmark bookmark = getSelectedBookmark();
        mManager.editBookmark(bookmark);
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

    private void bookmarkRemove() {
        final MBookmark bookmark = getSelectedBookmark();

        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), bookmark.getName()),
                    Dict.Dialog.TITLE_BOOKMARK_REMOVE.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE.toString());

            if (Dict.REMOVE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    try {
                        mManager.dbDelete(bookmark);
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }

    private void bookmarkRemoveAll() {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE_ALL.toString()};
            NotifyDescriptor d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_BOOKMARK_REMOVE_ALL.toString(),
                    Dict.Dialog.TITLE_BOOKMARK_REMOVE_ALL.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE_ALL.toString());

            if (Dict.REMOVE_ALL.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    try {
                        mManager.dbTruncate();
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }

    private void createUI() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.BOOKMARKS_SEARCH.toString());

        mTreeView.setShowRoot(false);
        mTreeView.setCellFactory((TreeView<MBookmark> param) -> new BookmarkTreeCell());

        setPrefWidth(300);
        setTop(mFilterTextField);
        setCenter(mTreeView);
    }

    private MBookmark getSelectedBookmark() {
        return mTreeView.getSelectionModel().getSelectedItem().getValue();
    }

    private void populate() {
        TreeItem<MBookmark> root = new TreeItem<>();
        root.setExpanded(true);
        ObservableList<TreeItem<MBookmark>> treeRootChildrens = root.getChildren();
        TreeMap<String, TreeItem<MBookmark>> actionParents = new TreeMap<>();
        ArrayList<TreeItem> tempRootItems = new ArrayList<>();

        for (MBookmark bookmark : mManager.getItems()) {
            TreeItem<MBookmark> treeItem = new TreeItem(bookmark);

            final String parentName = bookmark.getCategory();
            if (StringUtils.isBlank(parentName)) {
                tempRootItems.add(treeItem);
            } else {
                actionParents.computeIfAbsent(parentName, k -> new TreeItem(parentName)).getChildren().add(treeItem);
            }
        }

        Comparator<TreeItem> treeItemComparator = (TreeItem o1, TreeItem o2) -> ((MBookmark) o1.getValue()).getName().compareTo(((MBookmark) o2.getValue()).getName());

        actionParents.keySet().stream().map((key) -> {
            MBookmark parentBookmark = new MBookmark();
            parentBookmark.setName(key);
            TreeItem<MBookmark> parentItem = new TreeItem<>(parentBookmark);
            FXCollections.sort(actionParents.get(key).getChildren(), treeItemComparator);
            actionParents.get(key).getChildren().forEach((item) -> {
                parentItem.getChildren().add(item);
            });
            return parentItem;
        }).forEachOrdered((parentItem) -> {
            treeRootChildrens.add(parentItem);
        });

        Collections.sort(tempRootItems, treeItemComparator);
        tempRootItems.forEach((rootItem) -> {
            treeRootChildrens.add(rootItem);
        });

        root.getChildren().forEach((treeItem) -> {
            treeItem.setExpanded(true);//TODO Remove me
        });

        mTreeView.setRoot(root);
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
            Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                bookmarkEdit();
            });
            editAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(getIconSizeContextMenu()));

            Action removeAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
                bookmarkRemove();
            });

            Action removeAllAction = new Action(Dict.REMOVE_ALL.toString(), (ActionEvent event) -> {
                bookmarkRemoveAll();
            });

            Collection<? extends Action> actions = Arrays.asList(
                    editAction,
                    ActionUtils.ACTION_SEPARATOR,
                    removeAction,
                    removeAllAction
            );

            ContextMenu contextMenu = ActionUtils.createContextMenu(actions);

            mContextCopyMenu = new Menu(mBundle.getString("copy_location"));
            mContextOpenMenu = new Menu(mBundle.getString("open_location"));
            contextMenu.getItems().add(1, mContextCopyMenu);
            contextMenu.getItems().add(2, mContextOpenMenu);

            setOnMousePressed((MouseEvent event) -> {
                MBookmark b = this.getItem();
                if (b != null && ObjectUtils.allNotNull(b.getLatitude(), b.getLongitude())) {
                    if (event.isSecondaryButtonDown()) {
                        Mapton.getEngine().setLatitude(b.getLatitude());
                        Mapton.getEngine().setLongitude(b.getLongitude());
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
                            NbLog.v("Open location", s);
                            SystemHelper.copyToClipboard(s);
                        });
                        break;

                    case OPEN:
                        mContextOpenMenu.getItems().add(item);
                        item.setOnAction((ActionEvent event) -> {
                            String s = provider.getUrl();
                            NbLog.v("Copy location", s);
                            SystemHelper.desktopBrowse(s);
                        });
                        break;
                }
            }

            mContextCopyMenu.getItems().sorted((MenuItem o1, MenuItem o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextCopyMenu.setVisible(!mContextCopyMenu.getItems().isEmpty());

            mContextOpenMenu.getItems().sorted((MenuItem o1, MenuItem o2) -> o1.getText().compareToIgnoreCase(o2.getText()));
            mContextOpenMenu.setVisible(!mContextOpenMenu.getItems().isEmpty());
        }
    }
}
