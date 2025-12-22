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
package org.mapton.core.api;

import java.awt.Dimension;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.Strings;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.Mapton;
import org.mapton.core.ui.bookmark.BookmarkPanel;
import org.mapton.core.ui.bookmark.CategoryPanel;
import org.mapton.core.ui.bookmark.ColorPanel;
import org.mapton.core.ui.bookmark.ZoomPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkEditor {

    private final ResourceBundle mBundle = NbBundle.getBundle(MBookmarkManager.class);
    private final MBookmarkManager mManager = MBookmarkManager.getInstance();

    public BookmarkEditor() {
    }

    public void editBookmark(final MBookmark aBookmark) {
        SwingUtilities.invokeLater(() -> {
            var newBookmark = aBookmark;
            boolean add = aBookmark == null;
            if (add) {
                newBookmark = new MBookmark();
                newBookmark.setZoom(Mapton.getEngine().getZoom());
                newBookmark.setLatitude(Mapton.getEngine().getLockedLatitude());
                newBookmark.setLongitude(Mapton.getEngine().getLockedLongitude());
            }

            final var bookmark = newBookmark;
            var bookmarkPanel = new BookmarkPanel();
            var d = new DialogDescriptor(bookmarkPanel, Dict.BOOKMARK.toString());
            bookmarkPanel.setNotifyDescriptor(d);
            bookmarkPanel.initFx(() -> {
                bookmarkPanel.load(bookmark);
            });

            bookmarkPanel.setPreferredSize(SwingHelper.getUIScaledDim(300, 550));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                bookmarkPanel.save(bookmark);
                Platform.runLater(() -> {
                    if (add) {
                        bookmark.setTimeCreated(LocalDateTime.now());
                        mManager.add(bookmark);
                    } else {
                        bookmark.setTimeModified(LocalDateTime.now());
                        mManager.save();
                    }
                });
            }
        });
    }

    public void editCategory(final String category) {
        SwingUtilities.invokeLater(() -> {
            var categoryPanel = new CategoryPanel();
            var d = new DialogDescriptor(categoryPanel, Dict.EDIT.toString());
            categoryPanel.setNotifyDescriptor(d);
            categoryPanel.initFx(() -> {
                categoryPanel.setCategory(category);
            });

            categoryPanel.setPreferredSize(SwingHelper.getUIScaledDim(400, 100));

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                var newCategory = categoryPanel.getCategory();
                if (!Strings.CS.equals(category, newCategory)) {
                    var timestamp = LocalDateTime.now();
                    mManager.getFilteredItems(category).forEachOrdered(bookmark -> {
                        bookmark.setCategory(Strings.CS.replaceOnce(bookmark.getCategory(), category, newCategory));
                        bookmark.setTimeModified(timestamp);
                    });

                    Platform.runLater(() -> {
                        mManager.save();
                    });
                }
            }
        });
    }

    public void editColor(final String category) {
        SwingUtilities.invokeLater(() -> {
            var colorPanel = new ColorPanel();
            var d = new DialogDescriptor(colorPanel, Dict.EDIT.toString());
            colorPanel.setNotifyDescriptor(d);
            colorPanel.initFx(() -> {
            });

            colorPanel.setPreferredSize(SwingHelper.getUIScaledDim(200, 100));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                var color = colorPanel.getColor();
                var timestamp = LocalDateTime.now();
                mManager.getFilteredItems(category).forEachOrdered(bookmark -> {
                    bookmark.setColor(color);
                    bookmark.setTimeModified(timestamp);
                });

                Platform.runLater(() -> {
                    mManager.save();
                });
            }
        });
    }

    public void editZoom(final String category) {
        SwingUtilities.invokeLater(() -> {
            var zoomPanel = new ZoomPanel();
            var d = new DialogDescriptor(zoomPanel, Dict.EDIT.toString());
            zoomPanel.setNotifyDescriptor(d);
            zoomPanel.initFx(() -> {
            });

            zoomPanel.setPreferredSize(new Dimension(200, 100));
            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                var zoom = zoomPanel.getZoom();
                var timestamp = LocalDateTime.now();

                mManager.getFilteredItems(category).forEachOrdered(bookmark -> {
                    bookmark.setZoom(zoom);
                    bookmark.setTimeModified(timestamp);
                });

                Platform.runLater(() -> {
                    mManager.save();
                });
            }
        });
    }

    public Action getAddBookmarkAction() {
        var action = new Action(Dict.ADD_BOOKMARK.toString(), acttionEvent -> {
            editBookmark(null);
        });

        return action;
    }

    public void remove(final MBookmark bookmark) {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE.toString()};
            var d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_PROFILE_REMOVE.toString().formatted(bookmark.getName()),
                    Dict.Dialog.TITLE_BOOKMARK_REMOVE.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE.toString());

            if (Dict.REMOVE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    if (bookmark.isCategory()) {
                        mManager.remove(bookmark.getCategory());
                    } else {
                        mManager.remove(bookmark);
                    }
                });
            }
        });
    }

    public void removeAll() {
        SwingUtilities.invokeLater(() -> {
            String[] buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE_ALL.toString()};
            var d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_BOOKMARK_REMOVE_ALL.toString(),
                    Dict.Dialog.TITLE_BOOKMARK_REMOVE_ALL.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE_ALL.toString());

            if (Dict.REMOVE_ALL.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeAll();
                });
            }
        });
    }

    public void removeVisible() {
        SwingUtilities.invokeLater(() -> {
            var buttons = new String[]{Dict.CANCEL.toString(), Dict.REMOVE_VISIBLE.toString()};
            var d = new NotifyDescriptor(
                    Dict.Dialog.MESSAGE_BOOKMARK_REMOVE_VISIBLE.toString(),
                    Dict.Dialog.TITLE_BOOKMARK_REMOVE_VISIBLE.toString() + "?",
                    NotifyDescriptor.OK_CANCEL_OPTION,
                    NotifyDescriptor.WARNING_MESSAGE,
                    buttons,
                    Dict.REMOVE_VISIBLE.toString());

            if (Dict.REMOVE_VISIBLE.toString() == DialogDisplayer.getDefault().notify(d)) {
                Platform.runLater(() -> {
                    mManager.removeVisible();
                });
            }
        });
    }
}
