/*
 * Copyright 2021 Patrik Karlström.
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

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import org.mapton.api.MOptions;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.fx.FxHelper;

final class BasicPanel extends javax.swing.JPanel {

    private final ResourceBundle mBundle;
    private final BasicOptionsPanelController mController;
    private final FxPanel mFxPanel;
    private ComboBox<String> mDecimalSymbolComboBox;
    private ComboBox<String> mCoordinateSeparatorComboBox;
    private final MOptions mOptions = MOptions.getInstance();

    BasicPanel(BasicOptionsPanelController controller) {
        mBundle = NbBundle.getBundle(MOptions.class);
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {
                var extrasSectionLabel = new Label(mBundle.getString("extras_copy_location"));
                extrasSectionLabel.setPadding(FxHelper.getUIScaledInsets(0, 0, 6, 0));

                var sectionFont = Font.font(Font.getDefault().getSize() * 1.5);
                extrasSectionLabel.setFont(sectionFont);

                int row = 0;
                var gp = new GridPane();
                gp.addRow(row++, extrasSectionLabel);

                var decimalSymbolLabel = new Label(mBundle.getString("decimal_symbol"));
                var coordinateSeparatorLabel = new Label(mBundle.getString("coordinate_separator"));

                gp.addRow(row++, decimalSymbolLabel, mDecimalSymbolComboBox = new ComboBox<>());
                gp.addRow(row++, coordinateSeparatorLabel, mCoordinateSeparatorComboBox = new ComboBox<>());

                GridPane.setColumnSpan(extrasSectionLabel, Integer.MAX_VALUE);
                gp.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 16));

                FxHelper.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 20),
                        decimalSymbolLabel,
                        coordinateSeparatorLabel
                );

                FxHelper.setMargin(FxHelper.getUIScaledInsets(4, 0, 4, 16),
                        mCoordinateSeparatorComboBox,
                        mDecimalSymbolComboBox
                );

                mDecimalSymbolComboBox.getItems().addAll(".", ",");
                mCoordinateSeparatorComboBox.getItems().addAll(".", ",", ";", "SPACE", "TAB");

                mDecimalSymbolComboBox.valueProperty().bindBidirectional(mOptions.decimalSymbolProperty());
                mCoordinateSeparatorComboBox.valueProperty().bindBidirectional(mOptions.coordinateSeparatorProperty());

                var scrollPane = new ScrollPane(gp);
                //TODO Remove scroll pane border

                return new Scene(gp);
            }

            private void initListeners() {
                ChangeListener<Object> changeListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
                    mController.changed();
                };

                mDecimalSymbolComboBox.valueProperty().addListener(changeListener);
                mCoordinateSeparatorComboBox.valueProperty().addListener(changeListener);

//                Lookup.getDefault().lookupResult(MEngine.class).addLookupListener((LookupEvent ev) -> {
//                    populateEngines();
//                });
            }
        };

        mFxPanel.initFx(null);
        mFxPanel.setPreferredSize(null);

        setLayout(new BorderLayout());
        add(mFxPanel, BorderLayout.CENTER);
    }

    void load() {
        Platform.runLater(() -> {
            loadFX();
        });
    }

    void store() {
        Platform.runLater(() -> {
            storeFX();
        });
    }

    boolean valid() {
        return true;
    }

    private void loadFX() {
    }

    private void storeFX() {
    }
}
