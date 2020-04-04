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
package org.mapton.wikipedia;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MLatLon;
import org.mapton.api.MWikipediaArticle;
import org.mapton.api.MWikipediaArticleManager;
import org.mapton.api.Mapton;
import static org.mapton.wikipedia.Module.LOG_TAG;
import org.mapton.wikipedia.result.ApiResult;
import org.mapton.wikipedia.result.Coordinate;
import org.mapton.wikipedia.result.Page;
import org.mapton.wikipedia.result.Terms;
import org.mapton.wikipedia.result.Thumbnail;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MContextMenuItem.class)
public class WikipediaContextExtras extends MContextMenuItem {

    protected Locale mLocale = Locale.getDefault();
    private final MWikipediaArticleManager mWikipediaManager = MWikipediaArticleManager.getInstance();

    public WikipediaContextExtras() {
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return (event) -> {
//aaa            Almond.openAndActivateTopComponent("WikipediaTopComponent");
            String base = String.format(Locale.ENGLISH,
                    "https://%s.wikipedia.org/w/api.php?action=query",
                    mLocale.getLanguage()
            );

            final int limit = 1000;
            final int coLimit = limit;
            final int piLimit = limit;
            final int ggsLimit = limit;

            final int thumbSize = 50;
            final int radius = 10000;

            String template = String.format(Locale.ENGLISH, ""
                    + "&prop=coordinates|pageimages|pageterms"
                    + "&colimit=max"
                    + "&pilimit=max"
                    + "&ggslimit=max"
                    + "&piprop=thumbnail"
                    + "&pithumbsize=%d"
                    + "&wbptterms=description"
                    + "&generator=geosearch"
                    + "&ggscoord=%f|%f"
                    + "&ggsradius=%d"
                    + "&format=json"
                    + "&servedby=1"
                    + "&curtimestamp=1"
                    + "&responselanginfo=1",
                    thumbSize,
                    getLatitude(),
                    getLongitude(),
                    radius
            );

            try {
                URL url = new URL(base + StringUtils.replaceEach(URLEncoder.encode(template, "utf-8"),
                        new String[]{"%26", "%3D"},
                        new String[]{"&", "="}));

                Mapton.getLog().v(LOG_TAG, url.toString());

                mWikipediaManager.setLocale(mLocale);
                mWikipediaManager.getItems().clear();
                new Thread(() -> {
                    String json;
                    try {
                        json = IOUtils.toString(url, "utf-8");
                        ApiResult result = ApiResult.load(json);
                        ArrayList<MWikipediaArticle> articles = new ArrayList<>();
                        if (result.getQuery() != null && result.getQuery().getPages() != null) {
                            final LinkedHashMap<String, Page> pages = result.getQuery().getPages();
                            Mapton.getLog().v(LOG_TAG, String.format("Found %d articles", pages.size()));
                            for (Page page : pages.values()) {
                                MWikipediaArticle article = new MWikipediaArticle();
                                article.setTitle(page.getTitle());
                                Coordinate coordinate = page.getCoordinates().get(0);
                                MLatLon latLon = new MLatLon(coordinate.getLat(), coordinate.getLon());
                                article.setLatLon(latLon);
                                article.setDistance(latLon.distance(new MLatLon(getLatitude(), getLongitude())));
                                final Thumbnail thumbnail = page.getThumbnail();

                                if (thumbnail != null) {
                                    article.setThumbnail(thumbnail.getSource());
                                    article.setThumbnailHeight(thumbnail.getHeight());
                                    article.setThumbnailWidth(thumbnail.getWidth());
                                }

                                final Terms terms = page.getTerms();
                                if (terms != null && terms.getDescription() != null && terms.getDescription().length > 0) {
                                    article.setDescription(terms.getDescription()[0]);
                                }

                                articles.add(article);
                            }
                        }

                        Collections.sort(articles, (MWikipediaArticle o1, MWikipediaArticle o2) -> o1.getDistance().compareTo(o2.getDistance()));
                        mWikipediaManager.getItems().addAll(articles);
                    } catch (IOException ex) {
                        Exceptions.printStackTrace(ex);
                    }
                }).start();
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        };
    }

    @Override
    public String getName() {
        return "Wikipedia";
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

}
