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
package se.trixon.mapton.core.bookmark;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.core.api.Mapton;
import static se.trixon.mapton.core.api.Mapton.getIconSizeContextMenu;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkView extends BorderPane {

    private final ObservableList<Bookmark> mBookmarks = FXCollections.observableArrayList();
    private final Font mDefaultFont = Font.getDefault();
    private final TextField mFilterTextField;
    private final ListView<Bookmark> mListView;
    private final BookmarkManager mManager = BookmarkManager.getInstance();

    public BookmarkView() {
        mFilterTextField = new TextField();
        mFilterTextField.setPromptText(Dict.BOOKMARKS_SEARCH.toString());
        mListView = new ListView<>(mBookmarks);
        mListView.setPlaceholder(new Label(Dict.NO_BOOKMARKS.toString()));
        mListView.setCellFactory((ListView<Bookmark> param) -> new BookmarkListCell());
        mListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Bookmark> observable, Bookmark oldValue, Bookmark newValue) -> {
            if (newValue != null) {
                bookmarkGoTo(newValue);
            }
        });

        setTop(mFilterTextField);
        setCenter(mListView);
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            updateBookmarks(newValue);
        });

        mManager.getItems().addListener((ListChangeListener.Change<? extends Bookmark> c) -> {
            updateBookmarks(mFilterTextField.getText());
        });

        updateBookmarks(mFilterTextField.getText());
    }

    private void bookmarkCopy() {
        Bookmark bookmark = getSelectedBookmark();
        SystemHelper.copyToClipboard(String.format("geo:%f,%f", bookmark.getLatitude(), bookmark.getLongitude()));
    }

    private void bookmarkEdit() {
        Bookmark bookmark = getSelectedBookmark();
        mManager.editBookmark(bookmark);
    }

    private void bookmarkGoTo(Bookmark bookmark) {
        try {
            mManager.goTo(bookmark);
        } catch (ClassNotFoundException | SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private void bookmarkRemove() {
        final Bookmark bookmark = getSelectedBookmark();

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

    private Bookmark getSelectedBookmark() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    private void updateBookmarks(String filter) {
        mBookmarks.clear();
        mManager.getItems().stream()
                .filter((item) -> (StringUtils.containsIgnoreCase(String.join(" ", item.getName(), item.getCategory(), item.getDescription()), filter)))
                .forEachOrdered((item) -> {
                    mBookmarks.add(item);
                });

        Comparator<Bookmark> comparator = Comparator.comparing(Bookmark::getCategory)
                .thenComparing(Comparator.comparing(Bookmark::getName))
                .thenComparing(Comparator.comparing(Bookmark::getDescription));

        FXCollections.sort(mBookmarks, comparator);
    }

    class BookmarkListCell extends ListCell<Bookmark> {

        private final Label mCatLabel = new Label();
        private final Label mDescLabel = new Label();
        private final VBox mMainBox = new VBox();
        private final Label mNameLabel = new Label();

        public BookmarkListCell() {
            createUI();
        }

        @Override
        protected void updateItem(Bookmark bookmark, boolean empty) {
            super.updateItem(bookmark, empty);

            if (bookmark == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                addContent(bookmark);
            }
        }

        private void addContent(Bookmark bookmark) {
            setText(null);

            mNameLabel.textProperty().bind(bookmark.nameProperty());
            mCatLabel.textProperty().bind(bookmark.categoryProperty());
            mDescLabel.textProperty().bind(bookmark.descriptionProperty());

            setGraphic(mMainBox);
        }

        private void createUI() {
            final Color iconColor = Mapton.getIconColor();

            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.2));

            Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                bookmarkEdit();
            });
            editAction.setGraphic(MaterialIcon._Content.CREATE.getImageView(getIconSizeContextMenu()));

            Action copyAction = new Action(Dict.COPY.toString(), (ActionEvent event) -> {
                bookmarkCopy();
            });
            copyAction.setGraphic(MaterialIcon._Content.CONTENT_COPY.getImageView(getIconSizeContextMenu()));

            Action removeAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
                bookmarkRemove();
            });

            Action removeAllAction = new Action(Dict.REMOVE_ALL.toString(), (ActionEvent event) -> {
                bookmarkRemoveAll();
            });

            mMainBox.getChildren().addAll(
                    mNameLabel,
                    mCatLabel,
                    mDescLabel
            );

            Collection<? extends Action> actions = Arrays.asList(
                    editAction,
                    copyAction,
                    ActionUtils.ACTION_SEPARATOR,
                    removeAction,
                    removeAllAction
            );

            ContextMenu contextMenu = ActionUtils.createContextMenu(actions);

            mMainBox.setOnMousePressed((MouseEvent event) -> {
                if (event.isSecondaryButtonDown()) {
                    contextMenu.show(mMainBox, event.getScreenX(), event.getScreenY());
                } else if (event.isPrimaryButtonDown()) {
                    contextMenu.hide();
                    bookmarkGoTo(this.getItem());
                }
            });
        }
    }
}
