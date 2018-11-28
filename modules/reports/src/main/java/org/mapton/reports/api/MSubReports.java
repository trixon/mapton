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
package org.mapton.reports.api;

import static j2html.TagCreator.*;
import j2html.tags.ContainerTag;
import java.util.Comparator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.web.WebView;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class MSubReports extends MReport {

    private BorderPane mBorderPane;
    private Comparator<MSubReport> mComparator;
    private ListView<MSubReport> mListView;
    private WebView mWebView;

    public MSubReports() {
        mComparator = Comparator.comparing(MSubReport::getGroup)
                .thenComparing(Comparator.comparing(MSubReport::getName));
    }

    public Comparator<MSubReport> getComparator() {
        return mComparator;
    }

    public ListView<MSubReport> getListView() {
        return mListView;
    }

    @Override
    public String getName() {
        return Dict.SYSTEM.toString();
    }

    @Override
    public Node getNode() {
        if (mBorderPane == null) {
            createUI();
        }

        return mBody;
    }

    public abstract void subInit();

    private void createUI() {
        mWebView = new WebView();
        mListView = new ListView<>();
        mBorderPane = new BorderPane(mWebView);
        mBorderPane.setLeft(mListView);
        mNotificationPane.setContent(mBorderPane);
        mListView.setCellFactory((ListView<MSubReport> param) -> new SubReportListCell());

        mListView.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends MSubReport> c) -> {
            loadReport();
        });

        subInit();
    }

    private void loadReport() {
        MSubReport systemReport = mListView.getSelectionModel().getSelectedItem();
        if (systemReport == null) {
            mWebView.getEngine().loadContent("");

        } else {
            mMaskerPane.setVisible(true);
            mWebView.getEngine().loadContent("");

            new Thread(() -> {
                ContainerTag html = html(
                        head(
                                title(systemReport.getName())
                        ),
                        body(
                                h1(systemReport.getName()),
                                hr(),
                                systemReport.getContent()
                        ));

                Platform.runLater(() -> {
                    mWebView.getEngine().loadContent(html.render());
                });

                mMaskerPane.setVisible(false);
            }).start();
        }
    }

    class SubReportListCell extends ListCell<MSubReport> {

        private final BorderPane mBorderPane = new BorderPane();
        private Font mDefaultFont = Font.getDefault();
        private final Label mGroupLabel = new Label();
        private final Label mNameLabel = new Label();

        public SubReportListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MSubReport report, boolean empty) {
            super.updateItem(report, empty);

            if (report == null || empty) {
                clearContent();
            } else {
                addContent(report);
            }
        }

        private void addContent(MSubReport report) {
            setText(null);

            mNameLabel.setText(report.getName());
            mGroupLabel.setText(report.getGroup());

            setGraphic(mBorderPane);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = mDefaultFont.getFamily();
            double fontSize = mDefaultFont.getSize();

            mGroupLabel.setFont(Font.font(fontFamily, FontWeight.NORMAL, fontSize * 0.8));
            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize * 1));

            VBox mainBox = new VBox(mGroupLabel, mNameLabel);
            mainBox.setAlignment(Pos.CENTER_LEFT);

            mBorderPane.setCenter(mainBox);
            BorderPane.setMargin(mainBox, new Insets(8));
        }
    }
}
