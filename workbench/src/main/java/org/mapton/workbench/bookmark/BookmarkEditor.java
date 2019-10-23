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
package org.mapton.workbench.bookmark;

import com.dlsc.workbenchfx.Workbench;
import com.dlsc.workbenchfx.model.WorkbenchDialog;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.workbench.api.WorkbenchManager;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkEditor {

    private final MBookmarkManager mManager = MBookmarkManager.getInstance();
    private final Workbench mWorkbench = WorkbenchManager.getInstance().getWorkbench();
    private final ButtonType mOkButtonType = new ButtonType(Dict.SAVE.toString(), ButtonBar.ButtonData.OK_DONE);
    private final ButtonType mCancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);
    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);

    public static BookmarkEditor getInstance() {
        return Holder.INSTANCE;
    }

    private BookmarkEditor() {
    }

    public void editCategory(final String category) {
        BookmarkCategoryPanel editPanel = new BookmarkCategoryPanel();
        editPanel.setCategory(category);
        String title = Dict.EDIT.toString();

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                editPanel,
                mOkButtonType, mCancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == mOkButtonType) {
                        String newCategory = editPanel.getCategory();
                        if (!StringUtils.equals(category, newCategory)) {
                            Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                            TreeSet<String> bookmarkNames = new TreeSet<>();
                            for (MBookmark bookmark : mManager.getItems()) {
                                if (StringUtils.startsWith(bookmark.getCategory(), category)) {
                                    String oldCategory = bookmark.getCategory();
                                    bookmark.setCategory(StringUtils.replaceOnce(bookmark.getCategory(), category, newCategory));
                                    bookmark.setTimeModified(timestamp);
                                    try {
                                        mManager.dbUpdate(bookmark);
                                    } catch (SQLException ex) {
                                        bookmarkNames.add(String.format("%s/%s", oldCategory, bookmark.getName()));
                                    }
                                }
                            }

                            mManager.dbLoad();
                            if (!bookmarkNames.isEmpty()) {
                                String delim = "\n ◆ ";
                                mWorkbench.showErrorDialog(
                                        Dict.Dialog.ERROR.toString(),
                                        String.format("%s\n%s%s", mBundle.getString("bookmark_rename_category_error"), delim, String.join(delim, bookmarkNames)),
                                        null
                                );
                            }
                        }
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    public void editColor(final String category) {
        BookmarkColorPanel editPanel = new BookmarkColorPanel();
        String title = Dict.EDIT.toString();

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                editPanel,
                mOkButtonType, mCancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == mOkButtonType) {
                        String color = editPanel.getColor();
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                        for (MBookmark bookmark : mManager.getItems()) {
                            if (StringUtils.startsWith(bookmark.getCategory(), category)) {
                                bookmark.setColor(color);
                                bookmark.setTimeModified(timestamp);
                                try {
                                    mManager.dbUpdate(bookmark);
                                } catch (SQLException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }

                        mManager.dbLoad();
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    public void editZoom(final String category) {
        BookmarkZoomPanel editPanel = new BookmarkZoomPanel();
        String title = Dict.EDIT.toString();

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                editPanel,
                mOkButtonType, mCancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == mOkButtonType) {
                        double zoom = editPanel.getZoom();
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                        for (MBookmark bookmark : mManager.getItems()) {
                            if (StringUtils.startsWith(bookmark.getCategory(), category)) {
                                bookmark.setZoom(zoom);
                                bookmark.setTimeModified(timestamp);
                                try {
                                    mManager.dbUpdate(bookmark);
                                } catch (SQLException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }

                        mManager.dbLoad();
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    public void remove(MBookmark bookmark) {
        ButtonType okButtonType = new ButtonType(Dict.REMOVE.toString(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

        String title = Dict.Dialog.TITLE_BOOKMARK_REMOVE.toString() + "?";
        String message = String.format(Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString(), bookmark.getName());

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                message,
                okButtonType, cancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == okButtonType) {
                        try {
                            if (bookmark.isCategory()) {
                                mManager.dbDelete(bookmark.getCategory());
                            } else {
                                mManager.dbDelete(bookmark);
                            }
                        } catch (ClassNotFoundException | SQLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    public void removeAll() {
        ButtonType okButtonType = new ButtonType(Dict.REMOVE_ALL.toString(), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(Dict.CANCEL.toString(), ButtonBar.ButtonData.CANCEL_CLOSE);

        String title = Dict.Dialog.TITLE_BOOKMARK_REMOVE_ALL.toString();
        String message = Dict.Dialog.MESSAGE_BOOKMARK_REMOVE_ALL.toString();

        WorkbenchDialog dialog = WorkbenchDialog.builder(
                title,
                message,
                okButtonType, cancelButtonType)
                .onResult(buttonType -> {
                    if (buttonType == okButtonType) {
                        try {
                            mManager.dbDelete();
                        } catch (ClassNotFoundException | SQLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }).build();

        mWorkbench.showDialog(dialog);
    }

    private static class Holder {

        private static final BookmarkEditor INSTANCE = new BookmarkEditor();
    }
}
