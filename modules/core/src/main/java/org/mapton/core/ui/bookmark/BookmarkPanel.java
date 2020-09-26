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

import javafx.scene.Scene;
import org.mapton.api.MBookmark;
import org.mapton.base.ui.bookmark.BookmarkView;
import se.trixon.almond.nbp.fx.FxDialogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkPanel extends FxDialogPanel {

    private BookmarkView mBookmarkView;

    public BookmarkPanel() {
    }

    public void load(MBookmark bookmark) {
        mBookmarkView.load(bookmark);
    }

    public void save(MBookmark bookmark) {
        mBookmarkView.save(bookmark);
    }

    @Override
    protected void fxConstructor() {
        mBookmarkView = new BookmarkView();
        setScene(new Scene(mBookmarkView));
    }
}
