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
import java.util.Arrays;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
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

    private final Font mDefaultFont = Font.getDefault();
    private final TextField mFilterTextField;
    private final ListView<MBookmark> mListView;
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();

    public BookmarkView() {
        mFilterTextField = TextFields.createClearableTextField();
        mFilterTextField.setPromptText(Dict.BOOKMARKS_SEARCH.toString());
        mListView = new ListView<>(mManager.getItems());
        mListView.setPlaceholder(new Label(Dict.NO_BOOKMARKS.toString()));
        mListView.setCellFactory((ListView<MBookmark> param) -> new BookmarkListCell());
        mListView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends MBookmark> observable, MBookmark oldValue, MBookmark newValue) -> {
            if (newValue != null) {
                bookmarkGoTo(newValue);
            }
        });

        setPrefWidth(300);
        setTop(mFilterTextField);
        setCenter(mListView);
        mFilterTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            mManager.dbLoad(newValue);

        });

        mManager.dbLoad(mFilterTextField.getText());
    }

    private void bookmarkEdit() {
        MBookmark bookmark = getSelectedBookmark();
        mManager.editBookmark(bookmark);
    }

    private void bookmarkGoTo(MBookmark bookmark) {
        try {
            mManager.goTo(bookmark);
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

    private MBookmark getSelectedBookmark() {
        return mListView.getSelectionModel().getSelectedItem();
    }

    class BookmarkListCell extends ListCell<MBookmark> {

        private final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);

        private final Label mCatLabel = new Label();
        private Menu mContextCopyMenu;
        private Menu mContextOpenMenu;
        private final Label mDescLabel = new Label();
        private final VBox mMainBox = new VBox();
        private final Label mNameLabel = new Label();

        public BookmarkListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MBookmark bookmark, boolean empty) {
            super.updateItem(bookmark, empty);

            if (bookmark == null || empty) {
                setText(null);
                setGraphic(null);
            } else {
                addContent(bookmark);
            }
        }

        private void addContent(MBookmark bookmark) {
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
                    ActionUtils.ACTION_SEPARATOR,
                    removeAction,
                    removeAllAction
            );

            ContextMenu contextMenu = ActionUtils.createContextMenu(actions);

            mContextCopyMenu = new Menu(mBundle.getString("copy_location"));
            mContextOpenMenu = new Menu(mBundle.getString("open_location"));
            contextMenu.getItems().add(1, mContextCopyMenu);
            contextMenu.getItems().add(2, mContextOpenMenu);

            mMainBox.setOnMousePressed((MouseEvent event) -> {
                MBookmark b = this.getItem();
                if (event.isSecondaryButtonDown()) {
                    Mapton.getEngine().setLatitude(b.getLatitude());
                    Mapton.getEngine().setLongitude(b.getLongitude());
                    contextMenu.show(mMainBox, event.getScreenX(), event.getScreenY());
                } else if (event.isPrimaryButtonDown()) {
                    contextMenu.hide();
                    bookmarkGoTo(b);
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
