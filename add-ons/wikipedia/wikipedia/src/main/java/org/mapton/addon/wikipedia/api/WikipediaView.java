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
package org.mapton.addon.wikipedia.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.MasterDetailPane;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import static org.mapton.addon.wikipedia.Module.LOG_TAG;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.fx.FxHelper;

public final class WikipediaView extends BorderPane {

    private final HashMap<String, Image> mImageCache = new HashMap<>();
    private ListView<WikipediaArticle> mListView;
    private WebView mWebView;
    private final WikipediaArticleManager mWikipediaArticleManager = WikipediaArticleManager.getInstance();

    public WikipediaView() {
        createUI();
        initListeners();
    }

    private void createUI() {
        mListView = new ListView<>();
        mListView.itemsProperty().bind(mWikipediaArticleManager.allItemsProperty());
        mListView.setCellFactory((ListView<WikipediaArticle> param) -> new ArticleListCell());
        mListView.setPrefHeight(FxHelper.getUIScaled(300));
        mWebView = new WebView();
        mWebView.setZoom(FxHelper.getUIScaled(0.8));
        var masterDetailPane = new MasterDetailPane(Side.TOP, mListView, mWebView, true);
        masterDetailPane.setDividerPosition(0.5);

        setCenter(masterDetailPane);
    }

    private void initListeners() {
        mListView.getSelectionModel().selectedItemProperty().addListener((ov, oldValue, newValue) -> {
            mWikipediaArticleManager.setSelectedItem(newValue);
        });

        mWikipediaArticleManager.selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                Platform.runLater(() -> {
                    select(newValue);
                    mListView.getSelectionModel().select(newValue);
                    mListView.getFocusModel().focus(mListView.getItems().indexOf(newValue));
                    FxHelper.scrollToItemIfNotVisible(mListView, newValue);
                });
            } catch (Exception e) {

            }
        });
    }

    private void select(WikipediaArticle article) {
        WebEngine engine = mWebView.getEngine();

        if (article == null) {
            engine.loadContent("");
        } else {
            Mapton.getEngine().panTo(article.getLatLon());
            final String url = String.format(Locale.ENGLISH,
                    "https://%s.m.wikipedia.org/wiki/%s",
                    mWikipediaArticleManager.getLocale().getLanguage(),
                    article.getTitle());
            Mapton.getLog().d(LOG_TAG, url);
            Mapton.getLog().d(LOG_TAG, article.getThumbnail());

            try {
                Document doc = Jsoup.connect(url).get();

                for (Element link : doc.select("a")) {
                    link.attr("href", link.attr("abs:href"));
                }

                for (Element link : doc.select("link")) {
                    link.attr("href", link.attr("abs:href"));
                }

                for (Element link : doc.select("img")) {
                    link.attr("src", link.attr("abs:src"));
                    link.attr("srcset", link.attr("abs:srcset"));
                }

                for (Element element : doc.getElementsByClass("header-container")) {
                    element.remove();
                }

                for (Element element : doc.getElementsByClass("page-actions-menu")) {
                    element.remove();
                }

                for (Element element : doc.select("#mw-mf-page-left")) {
                    element.remove();
                }

                engine.loadContent(doc.outerHtml());
//                System.out.println(doc.toString());
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }

        }
    }

    class ArticleListCell extends ListCell<WikipediaArticle> {

        private final BorderPane mBorderPane = new BorderPane();
        private final Label mDescLabel = new Label();
        private final ImageView mImageView = new ImageView();
        private final int mMaxSize = FxHelper.getUIScaled(50);
        private final StackPane mStackPane = new StackPane();
        private final Label mTitleLabel = new Label();

        public ArticleListCell() {
            createUI();
        }

        @Override
        protected void updateItem(WikipediaArticle article, boolean empty) {
            super.updateItem(article, empty);
            FxHelper.runLater(() -> {
                if (article == null || empty) {
                    clearContent();
                } else {
                    addContent(article);
                }
            });
        }

        private void addContent(WikipediaArticle article) {
            setText(null);

            mTitleLabel.setText(article.getTitle());

            String distanceString;
            final Double distance = article.getDistance();

            if (distance >= 10000) {
                distanceString = String.format("%.0f km", distance / 1000);
            } else if (distance >= 1000) {
                distanceString = String.format("%.1f km", distance / 1000);
            } else {
                distanceString = String.format("%d m", distance.intValue());
            }

            if (StringUtils.isBlank(article.getDescription())) {
                mDescLabel.setText(distanceString);
            } else {
                mDescLabel.setText(String.format("%s, %s", distanceString, article.getDescription()));
            }

            String thumbnail = article.getThumbnail();
            if (StringUtils.isBlank(thumbnail)) {
                mStackPane.setPadding(new Insets(0, mMaxSize, 0, 0));//Already scaled, don't scale again!
                mImageView.setImage(null);
            } else {
                Image image = mImageCache.computeIfAbsent(thumbnail, k -> new Image(article.getThumbnail(), mMaxSize, mMaxSize, true, true, true));
                String dim;
                final String lowerCaseThumbnail = thumbnail.toLowerCase();
                if (StringUtils.containsIgnoreCase(lowerCaseThumbnail, ".jpg/")) {
                    dim = StringUtils.substringBetween(lowerCaseThumbnail, ".jpg/", "px-");
                } else if (StringUtils.containsIgnoreCase(lowerCaseThumbnail, ".png/")) {
                    dim = StringUtils.substringBetween(lowerCaseThumbnail, ".png/", "px-");
                } else {
                    dim = StringUtils.substringBetween(lowerCaseThumbnail, ".svg/", "px-");
                }

                try {
                    double baseDim = 50.0;
                    double imgDim = Double.valueOf(dim);
                    double pad = (baseDim - imgDim) / 2;
                    if (pad > 0) {
                        mStackPane.setPadding(new Insets(0, pad, 0, pad));
                    } else {
                        mStackPane.setPadding(Insets.EMPTY);
                    }
                } catch (NullPointerException | NumberFormatException e) {
                    Mapton.getLog().e(LOG_TAG, "Error while creating padding");
                    Mapton.getLog().v(LOG_TAG, article.getTitle());
                    Mapton.getLog().v(LOG_TAG, article.getThumbnail());
                }

                mImageView.setImage(image);
            }

            setGraphic(mBorderPane);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
            mImageView.setImage(null);
            mStackPane.setPadding(Insets.EMPTY);
        }

        private void createUI() {
            String fontFamily = Font.getDefault().getFamily();
            double fontSize = FxHelper.getScaledFontSize();

            mTitleLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.2));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 0.9));
            mDescLabel.setTextFill(Color.DARKGRAY.darker());

            VBox centerBox = new VBox(mTitleLabel, mDescLabel);
            centerBox.setAlignment(Pos.CENTER_LEFT);

            mStackPane.getChildren().add(mImageView);
            mBorderPane.setLeft(mStackPane);
            mBorderPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, FxHelper.getUIScaledInsets(0, 0, 0, 8));
        }
    }
}
