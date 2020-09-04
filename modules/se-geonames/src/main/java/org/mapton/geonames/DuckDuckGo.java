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
package org.mapton.geonames;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import org.mapton.api.MBookmark;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MLatLon;
import org.mapton.geonames.api.GeonamesManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MContextMenuItem.class)
public class DuckDuckGo extends MContextMenuItem {

    public DuckDuckGo() {
    }

    @Override
    public String getName() {
        return "DuckDuckGo";
    }

    @Override
    public ContextType getType() {
        return ContextType.OPEN;
    }

    @Override
    public String getUrl() {
        MLatLon hereLatLon = new MLatLon(getLongitude(), getLatitude());
        MBookmark nearest = new MBookmark();
        double dist = Double.MAX_VALUE;

        for (MBookmark bookmark : getResults()) {
            MLatLon bookmarkLatLon = new MLatLon(bookmark.getLongitude(), bookmark.getLatitude());
            double newDistance = hereLatLon.distance(bookmarkLatLon);
            if (newDistance < dist) {
                dist = newDistance;
                nearest = bookmark;
                if (dist < 1000) {
                    break;
                }
            }
        }

        try {
            return String.format("https://duckduckgo.com/?q=%s",
                    URLEncoder.encode(nearest.getName(), "UTF-8")
            );
        } catch (UnsupportedEncodingException ex) {
            return null;
        }
    }

    private ArrayList<MBookmark> getResults() {
        ArrayList<MBookmark> bookmarks = new ArrayList<>();
        GeonamesManager.getInstance().getGeonames().stream()
                .forEachOrdered((g) -> {
                    MBookmark b = new MBookmark();
                    b.setName(g.getName());
                    b.setCategory(g.getCountryCode());
                    b.setLatitude(g.getLatitude());
                    b.setLongitude(g.getLongitude());
                    b.setZoom(0.5);
                    bookmarks.add(b);
                });

        return bookmarks;
    }
}
