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
package org.mapton.mapollage;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import static org.mapton.mapollage.Options.*;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.LocaleComboBox;

final class OptionsPanel extends javax.swing.JPanel {

    private final OptionsController controller;
    private ResourceBundle mBundle;
    private final Font mDefaultFont = Font.getDefault();
    private final FxPanel mFxPanel;
    private LocaleComboBox mLocaleComboBox;
    private final Options mOptions = Options.getInstance();
    private Spinner<Integer> mThumbnailBorderSizeSpinner;
    private Spinner<Integer> mThumbnailSizeSpinner;
    private Insets mTopInsets;

    OptionsPanel(OptionsController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                mBundle = SystemHelper.getBundle(OptionsPanel.class, "Bundle");
                setScene(createScene());
            }

            private void addTopPadding(Region... regions) {
                for (Region region : regions) {
                    region.setPadding(mTopInsets);
                }
            }

            private Scene createScene() {
                mTopInsets = new Insets(8, 0, 0, 0);
                mLocaleComboBox = new LocaleComboBox();
                mThumbnailBorderSizeSpinner = new Spinner(0, 20, 2, 1);
                mThumbnailSizeSpinner = new Spinner(100, 1200, 250, 10);

                String fontFamily = mDefaultFont.getFamily();
                double fontSize = mDefaultFont.getSize();

                Font font = Font.font(fontFamily, FontPosture.ITALIC, fontSize * 1.3);

                Label calendarLanguageLabel = new Label(Dict.CALENDAR_LANGUAGE.toString());
                Label placemarkLabel = new Label(Dict.PLACEMARK.toString());
                Label thumbnailLabel = new Label(Dict.THUMBNAIL.toString());
                Label borderSizeLabel = new Label(mBundle.getString("OptionsPanel.borderSizeLabel"));

                placemarkLabel.setFont(font);
                mThumbnailSizeSpinner.setEditable(true);
                mThumbnailBorderSizeSpinner.setEditable(true);

                FxHelper.autoCommitSpinners(
                        mThumbnailBorderSizeSpinner,
                        mThumbnailSizeSpinner
                );

                GridPane gridPane = new GridPane();
                gridPane.addColumn(0,
                        calendarLanguageLabel,
                        mLocaleComboBox,
                        placemarkLabel,
                        thumbnailLabel,
                        mThumbnailSizeSpinner,
                        borderSizeLabel,
                        mThumbnailBorderSizeSpinner
                );

                addTopPadding(
                        placemarkLabel,
                        borderSizeLabel
                );

                return new Scene(gridPane);
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
        // TODO check whether form is consistent and complete
        return true;
    }

    private void loadFX() {
        mLocaleComboBox.setLocale(mOptions.getLocale());
        mThumbnailSizeSpinner.getValueFactory().setValue(mOptions.getInt(KEY_THUMBNAIL_SIZE, DEFAULT_THUMBNAIL_SIZE));
        mThumbnailBorderSizeSpinner.getValueFactory().setValue(mOptions.getInt(KEY_THUMBNAIL_BORDER_SIZE, DEFAULT_THUMBNAIL_BORDER_SIZE));

    }

    private void storeFX() {
        mOptions.setLocale(mLocaleComboBox.getLocale());
        mOptions.put(KEY_THUMBNAIL_SIZE, mThumbnailSizeSpinner.getValue());
        mOptions.put(KEY_THUMBNAIL_BORDER_SIZE, mThumbnailBorderSizeSpinner.getValue());
    }

}
