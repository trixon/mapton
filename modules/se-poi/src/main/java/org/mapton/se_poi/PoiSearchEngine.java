/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.se_poi;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MDict;
import org.mapton.api.MPoiManager;
import org.mapton.api.MSearchEngine;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class PoiSearchEngine implements MSearchEngine {

    public PoiSearchEngine() {
    }

    @Override
    public String getName() {
        return MDict.POI.toString();
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        var bookmarks = new ArrayList<MBookmark>();

        MPoiManager.getInstance().getAllItems().stream()
                .filter((poi) -> (StringUtils.containsIgnoreCase(String.join("/", poi.getCategory(), poi.getName(), poi.getProvider()), searchString)))
                .forEachOrdered((poi) -> {
                    MBookmark b = new MBookmark();
                    b.setName(poi.getName());
                    b.setCategory(poi.getCategory());
                    b.setLatitude(poi.getLatitude());
                    b.setLongitude(poi.getLongitude());
                    b.setZoom(poi.getZoom());

                    bookmarks.add(b);
                });

        return bookmarks;
    }
}
