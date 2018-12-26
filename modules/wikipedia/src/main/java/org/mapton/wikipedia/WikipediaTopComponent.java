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

import java.util.HashMap;
import java.util.Locale;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.Scene;
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
import javafx.scene.web.WebView;
import org.apache.commons.lang.StringUtils;
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

    private final Font mDefaultFont = Font.getDefault();
    private final HashMap<String, Image> mImageCache = new HashMap<>();
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
        mListView.setCellFactory((ListView<MWikipediaArticle> param) -> new ArticleListCell());
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
                mImageCache.clear();
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
            System.out.println(article.getThumbnail());
            mWebView.getEngine().load(url);
        }
    }

    class ArticleListCell extends ListCell<MWikipediaArticle> {

        private final BorderPane mBorderPane = new BorderPane();
        private final Label mDescLabel = new Label();
        private final ImageView mImageView = new ImageView();
        private final int mMaxSize = 50;
        private final StackPane mStackPane = new StackPane();
        private final Label mTitleLabel = new Label();

        public ArticleListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MWikipediaArticle article, boolean empty) {
            super.updateItem(article, empty);

            if (article == null || empty) {
                clearContent();
            } else {
                addContent(article);
            }
        }

        private void addContent(MWikipediaArticle article) {
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
                mStackPane.setPadding(new Insets(0, mMaxSize, 0, 0));
                mImageView.setImage(null);
            } else {
                Image image = mImageCache.computeIfAbsent(thumbnail, k -> new Image(article.getThumbnail(), mMaxSize, mMaxSize, true, true, false));
                String dim = StringUtils.substringBetween(thumbnail.toLowerCase(), ".jpg/", "px-");
                try {
                    double baseDim = 50.0;
                    double imgDim = Double.valueOf(dim);
                    double pad = (baseDim - imgDim) / 2;
                    if (pad > 0) {
                        mStackPane.setPadding(new Insets(0, pad, 0, pad));
                    } else {
                        mStackPane.setPadding(Insets.EMPTY);
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Error while creating padding");
                    System.out.println(article.getTitle());
                    System.out.println(article.getThumbnail());
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
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mTitleLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 1.2));
            mDescLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 0.9));
            mDescLabel.setTextFill(Color.DARKGRAY.darker());

            VBox centerBox = new VBox(mTitleLabel, mDescLabel);
            centerBox.setAlignment(Pos.CENTER_LEFT);

            mStackPane.getChildren().add(mImageView);
            mBorderPane.setLeft(mStackPane);
            mBorderPane.setCenter(centerBox);
            BorderPane.setMargin(centerBox, new Insets(0, 0, 0, 8));
        }
    }
}
