/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.core.ui.news;

import java.util.ArrayList;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import se.trixon.almond.nbp.core.news.NewsItem;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
class DynamicNewsSection extends NewsSection {

    public DynamicNewsSection() {
        Mapton.getGlobalState().addListener(gsce -> {
            FxHelper.runLater(() -> {
                refresh();
            });
        }, MKey.APP_NEWS_DYNAMIC);

        refresh();
    }

    @Override
    public void refresh() {
        StringBuilder builder = new StringBuilder();
        builder.append("<h1>").append(Dict.NEWS.toString()).append("</h1>");
        ArrayList<NewsItem> newsItems = Mapton.getGlobalState().get(MKey.APP_NEWS_DYNAMIC);
        if (newsItems != null) {
            for (NewsItem newsItem : newsItems) {
                builder.append("<h2>").append(newsItem.getName()).append("</h2>");
                builder.append(newsItem.getNews());
            }
        }
        mWebView.getEngine().loadContent(builder.toString(), "text/html");
    }

}
