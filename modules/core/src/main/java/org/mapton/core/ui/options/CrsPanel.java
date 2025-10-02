/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.core.ui.options;

import impl.org.controlsfx.skin.ListSelectionViewSkin;
import java.awt.BorderLayout;
import java.util.Locale;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import org.apache.commons.lang3.Strings;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.mapton.api.MCrsManager;
import org.mapton.api.MOptions;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.FilterableListSelectionView;
import se.trixon.almond.util.fx.control.LogPanel;

/**
 *
 * @author Patrik Karlström
 */
final class CrsPanel extends javax.swing.JPanel {

    private final ResourceBundle mBundle;
    private final CrsOptionsPanelController mController;
    private final Form mForm;
    private final MCrsManager mManager = MCrsManager.getInstance();

    CrsPanel(CrsOptionsPanelController controller) {
        mBundle = NbBundle.getBundle(MOptions.class);
        mController = controller;
        mForm = new Form();

        setLayout(new BorderLayout());
        add(mForm, BorderLayout.CENTER);
    }

    void load() {
        Platform.runLater(() -> {
            mForm.load();
        });
    }

    void store() {
        Platform.runLater(() -> {
            mForm.store();
        });
    }

    boolean valid() {
        return true;
    }

    class CrsListCell extends ListCell<CoordinateReferenceSystem> {

        private final VBox mBox = new VBox();
        private final Label mDescLabel = new Label();
        private final Label mNameLabel = new Label();

        public CrsListCell() {
            createUI();
        }

        @Override
        protected void updateItem(CoordinateReferenceSystem crs, boolean empty) {
            super.updateItem(crs, empty);

            if (crs == null || empty) {
                clearContent();
            } else {
                addContent(crs);
            }
        }

        private void addContent(CoordinateReferenceSystem crs) {
            setText(null);

            mNameLabel.setText(crs.getName().toString());
            try {
                mDescLabel.setText(crs.getDomainOfValidity().getDescription().toString(Locale.getDefault()));
            } catch (NullPointerException e) {
                mDescLabel.setText("");
            }

            setGraphic(mBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            String fontFamily = Font.getDefault().getFamily();
            double fontSize = FxHelper.getScaledFontSize();
            mNameLabel.setFont(Font.font(fontFamily, FontWeight.BOLD, fontSize));
            mDescLabel.setFont(Font.font(fontFamily, FontPosture.ITALIC, fontSize));

            mBox.setSpacing(FxHelper.getUIScaled(4));
            mBox.getChildren().setAll(mNameLabel, mDescLabel);
        }
    }

    class Form extends FxPanel {

        private FilterableListSelectionView<CoordinateReferenceSystem> mListSelectionView;
        private final LogPanel mLogPanel = new LogPanel();
        private final Label mSourceCountLabel = new Label();
        private ListView<CoordinateReferenceSystem> mSourceListView;
        private final Label mTargetCountLabel = new Label();
        private ListView<CoordinateReferenceSystem> mTargetListView;

        public Form() {
            initFx();
            setPreferredSize(null);
        }

        @Override
        protected void fxConstructor() {
            setScene(new Scene(createUI()));
            initListeners();
        }

        private Parent createUI() {
            mListSelectionView = new FilterableListSelectionView<>();
            mListSelectionView.setCellFactory(list -> new CrsListCell());
            mListSelectionView.setFilterSourceHeader(new Label(Dict.AVAILABLE.toString()));
            mListSelectionView.setFilterTargetHeader(new Label(Dict.ACTIVE_ALT.toString()));
            mListSelectionView.setSourceFooter(mSourceCountLabel);
            mListSelectionView.setTargetFooter(mTargetCountLabel);
            mListSelectionView.setFilterSourcePredicate(crs -> {
                var filterText = mListSelectionView.getFilterTextSource();
                return filter(crs, filterText);
            });
            mListSelectionView.setFilterTargetPredicate(crs -> {
                var filterText = mListSelectionView.getFilterTextTarget();
                return filter(crs, filterText);
            });

            mSourceCountLabel.setAlignment(Pos.BASELINE_RIGHT);
            mTargetCountLabel.setAlignment(Pos.BASELINE_RIGHT);

            var root = new GridPane();
            root.addRow(0, mListSelectionView);
            root.addRow(1, mLogPanel);

            FxHelper.autoSizeRegionHorizontal(
                    mListSelectionView,
                    mLogPanel
            );

            FxHelper.autoSizeRegionVertical(
                    mListSelectionView
            );

            mLogPanel.setPrefHeight(FxHelper.getUIScaled(140));

            return root;
        }

        private boolean filter(CoordinateReferenceSystem crs, String filterText) {
            var dov = "";
            if (crs.getDomainOfValidity() != null) {
                dov = crs.getDomainOfValidity().getDescription().toString(Locale.getDefault());
            }

            return Strings.CI.contains(crs.toWKT(), filterText)
                    || Strings.CI.contains(crs.getName().toString(), filterText)
                    || Strings.CI.contains(dov, filterText);
        }

        @SuppressWarnings("unchecked")
        private void initListeners() {
            var skin = (ListSelectionViewSkin) mListSelectionView.getSkin();
            mSourceListView = skin.getSourceListView();
            mTargetListView = skin.getTargetListView();
            mSourceCountLabel.prefWidthProperty().bind(mSourceListView.widthProperty());
            mTargetCountLabel.prefWidthProperty().bind(mTargetListView.widthProperty());

            mSourceListView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
                preview(n);
            });
            mTargetListView.getSelectionModel().selectedItemProperty().addListener((p, o, n) -> {
                preview(n);
            });

            mListSelectionView.getSourceItems().addListener((ListChangeListener.Change<? extends CoordinateReferenceSystem> c) -> {
                updateCounters();
            });
            mListSelectionView.getTargetItems().addListener((ListChangeListener.Change<? extends CoordinateReferenceSystem> c) -> {
                updateCounters();
            });
        }

        private void load() {
            mListSelectionView.filterLoad(mManager.getAllSystems(), mManager.getSelectedSystems());
        }

        private void preview(CoordinateReferenceSystem crs) {
            try {
                var sb = new StringBuilder(CRS.toSRS(crs)).append("\n\n");
                sb.append(crs.toWKT()).append("\n\n");
                sb.append(crs.getDomainOfValidity());

                mLogPanel.setText(sb.toString());
            } catch (NullPointerException e) {
                //
            }
        }

        private void store() {
            mManager.save(mListSelectionView.getUnfilteredTargetItems());
        }

        private void updateCounters() {
            mSourceCountLabel.setText("%d/%d".formatted(
                    mListSelectionView.getSourceItems().size(),
                    mManager.getAllSystems().size()
            ));

            mTargetCountLabel.setText("%d/%d".formatted(
                    mListSelectionView.getTargetItems().size(),
                    mManager.getAllSystems().size()
            ));
        }
    }
}
