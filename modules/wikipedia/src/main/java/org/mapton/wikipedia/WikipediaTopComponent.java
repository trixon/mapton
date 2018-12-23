/*
 * Copyright 2018 Patrik Karlström.
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

import java.util.Locale;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import org.controlsfx.control.MasterDetailPane;
import org.mapton.api.MMapMagnet;
import org.mapton.api.MTopComponent;
import org.mapton.api.MWikipediaArticle;
import org.mapton.api.MWikipediaArticleManager;
import org.mapton.api.Mapton;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@ConvertAsProperties(
        dtd = "-//org.mapton.wikipedia//Wikipedia//EN",
        autostore = false
)
@TopComponent.Description(
        preferredID = "WikipediaTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE",
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "properties", openAtStartup = false)
public final class WikipediaTopComponent extends MTopComponent implements MMapMagnet {

    private ListView<MWikipediaArticle> mListView;
    private final Options mOptions = Options.getInstance();
    private BorderPane mRoot;
    private WebView mWebView;
    private final MWikipediaArticleManager mWikipediaArticleManager = MWikipediaArticleManager.getInstance();

    public WikipediaTopComponent() {
        setName("Wikipedia");
    }

    @Override
    protected void initFX() {
        setScene(createScene());
    }

    void readProperties(java.util.Properties p) {
        String version = p.getProperty("version");
        // TODO read your settings according to their version
    }

    void writeProperties(java.util.Properties p) {
        // better to version settings since initial version as advocated at
        // http://wiki.apidesign.org/wiki/PropertyFiles
        p.setProperty("version", "1.0");
        // TODO store your settings
    }

    private Scene createScene() {
        mListView = new ListView<>();
        mWebView = new WebView();
        mWebView.setZoom(0.8);
        MasterDetailPane masterDetailPane = new MasterDetailPane(Side.TOP, mListView, mWebView, true);
        masterDetailPane.setDividerPosition(0.5);

        Label titleLabel = createTitle("Wikipedia");
        mRoot = new BorderPane(masterDetailPane);
        mRoot.setTop(titleLabel);
        titleLabel.prefWidthProperty().bind(mRoot.widthProperty());

        mWikipediaArticleManager.getItems().addListener((ListChangeListener.Change<? extends MWikipediaArticle> c) -> {
            Platform.runLater(() -> {
                mListView.getItems().setAll(mWikipediaArticleManager.getItems());
                if (mWikipediaArticleManager.getItems().size() > 0) {
                    select(mWikipediaArticleManager.getItems().get(0));
                } else {
                    select(null);
                }
            });
        });

        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MWikipediaArticle> c) -> {
            select(mListView.getSelectionModel().getSelectedItem());
        });

        return new Scene(mRoot);
    }

    private void select(MWikipediaArticle article) {
        if (article == null) {
            mWebView.getEngine().loadContent("");
        } else {
            Mapton.getEngine().panTo(article.getLatLon());
            final String url = String.format(Locale.ENGLISH,
                    "https://%s.m.wikipedia.org/wiki/%s",
                    mWikipediaArticleManager.getLocale().getLanguage(),
                    article.getTitle());
            System.out.println(url);
            mWebView.getEngine().load(url);
        }
    }
}
