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
package org.mapton.bookmarks;

import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MLatLon;
import org.mapton.api.Mapton;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 *
 * @author Patrik Karlström
 */
public class BookmarkQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (MBookmark bookmark : MBookmarkManager.getInstance().dbLoad(request.getText(), false)) {
            if (!response.addResult(() -> {
                Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()), bookmark.getZoom());
            }, bookmark.getName())) {
                break;
            }
        }
    }
}
