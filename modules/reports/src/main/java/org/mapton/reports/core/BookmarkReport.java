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
package org.mapton.reports.core;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import java.util.ArrayList;
import java.util.TreeMap;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBookmark;
import org.mapton.api.MBookmarkManager;
import org.mapton.api.Mapton;
import org.mapton.reports.api.MReport;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MReport.class)
public class BookmarkReport extends MReport {

    private final MBookmarkManager mManager = MBookmarkManager.getInstance();
    private final String mName = Dict.BOOKMARKS.toString();
    private WebView mWebView;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(new BookmarkReport().getContent().renderFormatted());
    }

    public BookmarkReport() {
    }

    public ContainerTag getContent() {
        final TreeMap<String, ArrayList<MBookmark>> bookmarkCategories = new TreeMap<>();

        for (MBookmark bookmark : mManager.dbLoad("*", false)) {
            bookmarkCategories.computeIfAbsent(bookmark.getCategory(), k -> new ArrayList<>()).add(bookmark);
        }

        ContainerTag html = html(
                head(
                        title(mName)
                ),
                body(
                        h1(mName),
                        hr(),
                        div(
                                each(bookmarkCategories.keySet(), category
                                        -> div(
                                        h2(category),
                                        table(
                                                tr(
                                                        th(Dict.NAME.toString()),
                                                        th(Dict.DESCRIPTION.toString()),
                                                        th(Dict.LATITUDE.toString()),
                                                        th(Dict.LONGITUDE.toString())
                                                ),
                                                tbody(
                                                        each(filter(mManager.dbLoad(), bookmark -> bookmark.getCategory().equals(category)), bookmark
                                                                -> tr(
                                                                td(bookmark.getName()),
                                                                td(StringUtils.defaultString(bookmark.getDescription())),
                                                                td(String.format("%.6f", bookmark.getLatitude())),
                                                                td(String.format("%.6f", bookmark.getLongitude()))
                                                        )
                                                        )
                                                )
                                        )
                                )
                                )
                        )));

        return html;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public Node getNode() {
        if (mWebView == null) {
            mWebView = new WebView();
            Mapton.applyHtmlCss(mWebView, "report.css");
        }

        Platform.runLater(() -> {
            mWebView.getEngine().loadContent(getContent().render());
        });

        return mWebView;
    }

    @Override
    public String getParent() {
        return String.format("- %s -", Mapton.LOG_TAG);
    }
}
