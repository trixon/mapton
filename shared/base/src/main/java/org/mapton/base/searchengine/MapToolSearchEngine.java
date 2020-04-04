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
package org.mapton.base.searchengine;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MDict;
import org.mapton.api.MSearchEngine;
import org.mapton.api.MToolBase;
import org.mapton.api.MToolMap;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class MapToolSearchEngine implements MSearchEngine {

    public MapToolSearchEngine() {
    }

    @Override
    public String getName() {
        return MDict.MAP_TOOLS.toString();
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        int limit = StringUtils.isBlank(searchString) ? Integer.MAX_VALUE : 20;
        ArrayList<MBookmark> bookmarks = new ArrayList<>();
        Lookup.getDefault().lookupAll(MToolMap.class).stream()
                .filter((t) -> (StringUtils.containsIgnoreCase(String.join("/", getName(), t.getParent(), t.getAction().getText(), ""), searchString)))
                //                .limit(limit)
                .sorted((MToolBase o1, MToolBase o2) -> o1.getAction().getText().compareToIgnoreCase(o2.getAction().getText()))
                .forEachOrdered((t) -> {
                    MBookmark b = new MBookmark();
                    b.setName(t.getAction().getText());
                    b.setCategory(t.getParent());
                    b.setLatitude(.0);
                    b.setLongitude(.0);
                    b.setValue("action", t.getAction());
                    bookmarks.add(b);
                });

        return bookmarks;
    }
}
