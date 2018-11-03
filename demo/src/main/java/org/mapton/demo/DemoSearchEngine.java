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
package org.mapton.demo;

import java.util.ArrayList;
import org.apache.commons.lang.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MSearchEngine;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class DemoSearchEngine implements MSearchEngine {

    private final ArrayList<MBookmark> mBookmarks = new ArrayList<>();

    public DemoSearchEngine() {
        MBookmark bookmark = new MBookmark();
        bookmark.setName("abc123");
        bookmark.setLatitude(58.0);
        bookmark.setLongitude(12.0);
        bookmark.setZoom(0.1);

        mBookmarks.add(bookmark);
    }

    @Override
    public String getName() {
        return "Demo";
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        ArrayList<MBookmark> bookmarks = new ArrayList<>();

        mBookmarks.stream()
                .filter((b) -> (StringUtils.containsIgnoreCase(String.join("/", b.getCategory(), b.getName()), searchString)))
                .forEachOrdered((bookmark) -> {
                    bookmarks.add(bookmark);
                });

        return bookmarks;
    }
}
