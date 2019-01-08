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
package org.mapton.bookmarks;

import java.util.ArrayList;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MSearchEngine;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class BookmarksSearchEngine implements MSearchEngine {

    public BookmarksSearchEngine() {
    }

    @Override
    public String getName() {
        return Dict.BOOKMARKS.toString();
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        return MBookmarkManager.getInstance().dbLoad(searchString, false);
    }
}
