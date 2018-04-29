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

import java.util.Arrays;
import java.util.Collection;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
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
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.glyphfont.FontAwesome;
import org.controlsfx.glyphfont.GlyphFont;
import org.controlsfx.glyphfont.GlyphFontRegistry;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkView extends BorderPane {

    private static final int ICON_SIZE = 16;
    private final Font mDefaultFont = Font.getDefault();
    private final TextField mFilterTextField;
    private final GlyphFont mFontAwesome = GlyphFontRegistry.font("FontAwesome");
    private final Color mIconColor = Color.BLACK;
    private final ListView<Bookmark> mListView;
    private final BookmarkManager mManager = BookmarkManager.getInstance();

    public BookmarkView() {
        mFilterTextField = new TextField();
        mFilterTextField.setPromptText(Dict.BOOKMARKS_SEARCH.toString());
        mListView = new ListView<>(mManager.getItems());
        mListView.setPlaceholder(new Label(Dict.NO_BOOKMARKS.toString()));
        mListView.setCellFactory((ListView<Bookmark> param) -> new BookmarkListCell());
        mListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends Bookmark> observable, Bookmark oldValue, Bookmark newValue) -> {
            bookmarkGoTo();
        });

        setTop(mFilterTextField);
        setCenter(mListView);
        //TODO Add bookmark search
    }

    private void bookmarkCopy() {
        Bookmark bookmark = getSelectedBookmark();
        SystemHelper.copyToClipboard(String.format("geo:%f,%f", bookmark.getLatitude(), bookmark.getLongitude()));
    }

    private void bookmarkEdit() {
        Bookmark bookmark = getSelectedBookmark();
        mManager.editBookmark(bookmark);
    }

    private void bookmarkGoTo() {
        //TODO Pan map
        System.out.println("GO TO BOOKMARK " + System.currentTimeMillis());
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
                    mListView.getItems().remove(bookmark);
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
                    mListView.getItems().clear();
                });
            }
        });
    }

    private Bookmark getSelectedBookmark() {
        return mListView.getSelectionModel().getSelectedItem();
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
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1.2));

            Action editAction = new Action(Dict.EDIT.toString(), (ActionEvent event) -> {
                bookmarkEdit();
            });
            editAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.EDIT).size(ICON_SIZE).color(mIconColor));

            Action copyAction = new Action(Dict.COPY.toString(), (ActionEvent event) -> {
                bookmarkCopy();
            });
            copyAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.COPY).size(ICON_SIZE).color(mIconColor));

            Action removeAction = new Action(Dict.REMOVE.toString(), (ActionEvent event) -> {
                bookmarkRemove();
            });
            //removeAction.setGraphic(mFontAwesome.create(FontAwesome.Glyph.TRASH).size(ICON_SIZE).color(mIconColor));

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
                    bookmarkGoTo();
                }
            });
        }
    }
}
