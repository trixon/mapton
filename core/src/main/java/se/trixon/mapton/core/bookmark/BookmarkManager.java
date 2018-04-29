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

import java.awt.Dimension;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javax.swing.SwingUtilities;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkManager {

    private ObjectProperty<ObservableList<Bookmark>> mItems = new SimpleObjectProperty<>(this, "items");

    public static BookmarkManager getInstance() {
        return Holder.INSTANCE;
    }

    private BookmarkManager() {
        mItems.setValue(FXCollections.observableArrayList());
    }

    public void editBookmark(final Bookmark aBookmark) {
        SwingUtilities.invokeLater(() -> {
            Bookmark newBookmark = aBookmark;
            boolean add = aBookmark == null;
            if (add) {
                newBookmark = new Bookmark();
                newBookmark.setCategory("default");
                newBookmark.setZoom(5);
                newBookmark.setLatitude(55);
                newBookmark.setLongitude(11);
            }

            final Bookmark bookmark = newBookmark;
            BookmarkPanel bookmarkPanel = new BookmarkPanel();
            DialogDescriptor d = new DialogDescriptor(bookmarkPanel, Dict.BOOKMARK.toString());
            bookmarkPanel.setDialogDescriptor(d);
            bookmarkPanel.initFx(() -> {
                bookmarkPanel.load(bookmark);
            });

            bookmarkPanel.setPreferredSize(new Dimension(300, 400));

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                bookmarkPanel.save(bookmark);
                if (add) {
                    Platform.runLater(() -> {
                        getItems().add(bookmark);
                    });
                }
            }
        });
    }

    public FxActionSwing getAddBookmarkAction() {
        FxActionSwing action = new FxActionSwing(Dict.ADD_BOOKMARK.toString(), () -> {
            editBookmark(null);
        });

        return action;
    }

    public final ObservableList<Bookmark> getItems() {
        return mItems == null ? null : mItems.get();
    }

    public final ObjectProperty<ObservableList<Bookmark>> itemsProperty() {
        if (mItems == null) {
            mItems = new SimpleObjectProperty<>(this, "items");
        }

        return mItems;
    }

    private static class Holder {

        private static final BookmarkManager INSTANCE = new BookmarkManager();
    }
}
