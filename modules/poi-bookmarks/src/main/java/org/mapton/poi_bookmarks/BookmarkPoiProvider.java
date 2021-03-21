/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.poi_bookmarks;

import java.util.ArrayList;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MPoiProvider.class)
public class BookmarkPoiProvider implements MPoiProvider {

    public BookmarkPoiProvider() {
    }

    @Override
    public String getName() {
        return Dict.BOOKMARKS.toString();
    }

    @Override
    public ArrayList<MPoi> getPois() {
        ArrayList<MPoi> pois = new ArrayList<>();

        for (var bookmark : MBookmarkManager.getInstance().dbLoad("%", false)) {
            var poi = new MPoi();
            poi.setDescription(bookmark.getDescription());
            poi.setCategory(bookmark.getCategory());
            poi.setColor(bookmark.getColor());
            poi.setDisplayMarker(bookmark.isDisplayMarker());
            poi.setLatitude(bookmark.getLatitude());
            poi.setLongitude(bookmark.getLongitude());
            poi.setName(bookmark.getName());
            poi.setZoom(bookmark.getZoom());
            poi.setUrl(bookmark.getUrl());

            pois.add(poi);
        }

        return pois;
    }
}
