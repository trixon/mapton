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
package org.mapton.base.ui.bookmark;

import javafx.event.ActionEvent;
import org.controlsfx.control.action.Action;
import org.mapton.api.MBookmark;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public interface BookmarkEditor {

    void editBookmark(final MBookmark aBookmark);

    void editCategory(final String category);

    void editColor(final String category);

    void editZoom(final String category);

    default Action getAddBookmarkAction() {
        Action action = new Action(Dict.ADD_BOOKMARK.toString(), (ActionEvent t) -> {
            editBookmark(null);
        });

        return action;
    }

    void remove(MBookmark bookmark);

    void removeAll();

}
