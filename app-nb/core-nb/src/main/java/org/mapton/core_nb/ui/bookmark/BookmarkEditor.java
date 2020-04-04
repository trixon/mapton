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
package org.mapton.core_nb.ui.bookmark;

import java.awt.Dimension;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.Mapton;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = org.mapton.base.ui.bookmark.BookmarkEditor.class)
public class BookmarkEditor implements org.mapton.base.ui.bookmark.BookmarkEditor {

    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();

    public BookmarkEditor() {
    }

    @Override
    public void editBookmark(final MBookmark aBookmark) {
        SwingUtilities.invokeLater(() -> {
            MBookmark newBookmark = aBookmark;
            boolean add = aBookmark == null;
            if (add) {
                newBookmark = new MBookmark();
                newBookmark.setZoom(Mapton.getEngine().getZoom());
                newBookmark.setLatitude(Mapton.getEngine().getLockedLatitude());
                newBookmark.setLongitude(Mapton.getEngine().getLockedLongitude());
            }

            final MBookmark bookmark = newBookmark;
            BookmarkPanel bookmarkPanel = new BookmarkPanel();
            DialogDescriptor d = new DialogDescriptor(bookmarkPanel, Dict.BOOKMARK.toString());
            bookmarkPanel.setDialogDescriptor(d);
            bookmarkPanel.initFx(() -> {
                bookmarkPanel.load(bookmark);
            });

            bookmarkPanel.setPreferredSize(SwingHelper.getUIScaledDim(300, 500));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                bookmarkPanel.save(bookmark);
                Platform.runLater(() -> {
                    try {
                        if (add) {
                            mManager.dbInsert(bookmark);
                        } else {
                            bookmark.setTimeModified(new Timestamp(System.currentTimeMillis()));
                            mManager.dbUpdate(bookmark);
                            mManager.dbLoad();
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }

    @Override
    public void editCategory(final String category) {
        SwingUtilities.invokeLater(() -> {
            CategoryPanel categoryPanel = new CategoryPanel();
            DialogDescriptor d = new DialogDescriptor(categoryPanel, Dict.EDIT.toString());
            categoryPanel.setDialogDescriptor(d);
            categoryPanel.initFx(() -> {
                categoryPanel.setCategory(category);
            });

            categoryPanel.setPreferredSize(SwingHelper.getUIScaledDim(400, 100));

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                String newCategory = categoryPanel.getCategory();
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

                    Platform.runLater(() -> {
                        mManager.dbLoad();
                    });

                    if (!bookmarkNames.isEmpty()) {
                        String delim = "\n ◆ ";
                        NbMessage.error(Dict.Dialog.ERROR.toString(),
                                String.format("%s\n%s%s", mBundle.getString("bookmark_rename_category_error"), delim, String.join(delim, bookmarkNames))
                        );
                    }
                }
            }
        });
    }

    @Override
    public void editColor(final String category) {
        SwingUtilities.invokeLater(() -> {
            ColorPanel colorPanel = new ColorPanel();
            DialogDescriptor d = new DialogDescriptor(colorPanel, Dict.EDIT.toString());
            colorPanel.setDialogDescriptor(d);
            colorPanel.initFx(() -> {
            });

            colorPanel.setPreferredSize(SwingHelper.getUIScaledDim(200, 100));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                String color = colorPanel.getColor();
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

                Platform.runLater(() -> {
                    mManager.dbLoad();
                });
            }
        });
    }

    @Override
    public void editZoom(final String category) {
        SwingUtilities.invokeLater(() -> {
            ZoomPanel zoomPanel = new ZoomPanel();
            DialogDescriptor d = new DialogDescriptor(zoomPanel, Dict.EDIT.toString());
            zoomPanel.setDialogDescriptor(d);
            zoomPanel.initFx(() -> {
            });

            zoomPanel.setPreferredSize(new Dimension(200, 100));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                double zoom = zoomPanel.getZoom();
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

                Platform.runLater(() -> {
                    mManager.dbLoad();
                });
            }
        });
    }

    @Override
    public void remove(final MBookmark bookmark) {
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
                        if (bookmark.isCategory()) {
                            mManager.dbDelete(bookmark.getCategory());
                        } else {
                            mManager.dbDelete(bookmark);
                        }
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }

    @Override
    public void removeAll() {
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
                        mManager.dbDelete();
                    } catch (ClassNotFoundException | SQLException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                });
            }
        });
    }
}
