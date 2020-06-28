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
package org.mapton.base.ui.news;

import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.nbp.core.news.NewsProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
class DynamicNewsSection extends NewsSection {

    public DynamicNewsSection() {
        Lookup.Result<NewsProvider> newsResult = Lookup.getDefault().lookupResult(NewsProvider.class);
        newsResult.addLookupListener((LookupEvent ev) -> {
            refresh();
        });

        refresh();
    }

    @Override
    public void refresh() {
        StringBuilder builder = new StringBuilder();
        builder.append("<h1>").append(Dict.NEWS.toString()).append("</h1>");

        //TODO Populate news from GlobalState
        mWebView.getEngine().loadContent(builder.toString(), "text/html");
    }

}
