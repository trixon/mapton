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
package org.mapton.addon.wikipedia.api;

import java.util.ArrayList;
import java.util.Locale;
import org.mapton.api.MBaseDataManager;

/**
 *
 * @author Patrik Karlström
 */
public class WikipediaArticleManager extends MBaseDataManager<WikipediaArticle> {

    private Locale mLocale = Locale.getDefault();

    public static WikipediaArticleManager getInstance() {
        return Holder.INSTANCE;
    }

    private WikipediaArticleManager() {
        super(WikipediaArticle.class);
    }

    public Locale getLocale() {
        return mLocale;
    }

    public void setLocale(Locale locale) {
        mLocale = locale;
    }

    @Override
    protected void applyTemporalFilter() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void load(ArrayList<WikipediaArticle> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final WikipediaArticleManager INSTANCE = new WikipediaArticleManager();
    }
}
