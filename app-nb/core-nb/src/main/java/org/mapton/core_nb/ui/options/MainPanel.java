/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.core_nb.ui.options;

import java.awt.BorderLayout;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javax.swing.UIManager;
import org.controlsfx.control.ToggleSwitch;
import org.controlsfx.control.action.Action;
import org.mapton.api.MDict;
import org.mapton.api.MEngine;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.openide.LifecycleManager;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

final class MainPanel extends javax.swing.JPanel {

    private final ResourceBundle mBundle = NbBundle.getBundle(MOptions.class);
    private final MainOptionsPanelController mController;
    private ToggleSwitch mCrosshairToggleSwitch;
    private ComboBox<String> mEngineComboBox;
    private final FxPanel mFxPanel;
    private ToggleSwitch mHomeIconToggleSwitch;
    private ColorPicker mIconBrightColorPicker;
    private ColorPicker mIconDarkColorPicker;
    private ToggleSwitch mNightModeToggleSwitch;
    private boolean mOldDisplayCrosshair;
    private boolean mOldDisplayHomeIcon;
    private String mOldEngine;
    private Color mOldIconColorBright;
    private Color mOldIconColorDark;
    private boolean mOldNightMode;
    private boolean mOldPopover;
    private final MOptions mOptions = MOptions.getInstance();
    private ToggleSwitch mPopoverToggleSwitch;

    MainPanel(MainOptionsPanelController controller) {
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();

                populateEngines();
            }

            private Scene createScene() {
                var lafSectionLabel = new Label(Dict.LOOK_AND_FEEL.toString());
                var mapSectionLabel = new Label(Dict.MAP.toString());
                lafSectionLabel.setPadding(FxHelper.getUIScaledInsets(0, 0, 6, 0));
                mapSectionLabel.setPadding(FxHelper.getUIScaledInsets(12, 0, 6, 0));

                var sectionFont = Font.font(Font.getDefault().getSize() * 1.5);
                lafSectionLabel.setFont(sectionFont);
                mapSectionLabel.setFont(sectionFont);

                int row = 0;
                var gp = new GridPane();
                gp.addRow(row++, lafSectionLabel);

                var popoverLabel = new Label(mBundle.getString("popover"));
                var nightModeLabel = new Label(Dict.NIGHT_MODE.toString());
                var iconColorLabel = new Label(mBundle.getString("iconColor"));
                var iconColorNightModeLabel = new Label(mBundle.getString("iconColorNightMode"));

                gp.addRow(row++, popoverLabel, mPopoverToggleSwitch = new ToggleSwitch());
                gp.addRow(row++, nightModeLabel, mNightModeToggleSwitch = new ToggleSwitch());
                gp.addRow(row++, iconColorLabel, mIconDarkColorPicker = new ColorPicker());
                gp.addRow(row++, iconColorNightModeLabel, mIconBrightColorPicker = new ColorPicker());

                var mapEngineLabel = new Label(MDict.MAP_ENGINE.toString());
                var crosshairLabel = new Label(mBundle.getString("crosshair"));
                var homeIconLabel = new Label(mBundle.getString("homeIcon"));

                gp.addRow(row++, mapSectionLabel);
                gp.addRow(row++, mapEngineLabel, mEngineComboBox = new ComboBox<>());
                gp.addRow(row++, crosshairLabel, mCrosshairToggleSwitch = new ToggleSwitch());
                gp.addRow(row++, homeIconLabel, mHomeIconToggleSwitch = new ToggleSwitch());

                GridPane.setColumnSpan(lafSectionLabel, Integer.MAX_VALUE);
                GridPane.setColumnSpan(mapSectionLabel, Integer.MAX_VALUE);
                gp.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 16));

                FxHelper.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 20),
                        popoverLabel,
                        nightModeLabel,
                        iconColorLabel,
                        iconColorNightModeLabel,
                        mapEngineLabel,
                        crosshairLabel,
                        homeIconLabel
                );

                FxHelper.setPadding(FxHelper.getUIScaledInsets(4, 0, 4, 0),
                        mPopoverToggleSwitch,
                        mNightModeToggleSwitch,
                        mCrosshairToggleSwitch,
                        mHomeIconToggleSwitch
                );

                FxHelper.setMargin(FxHelper.getUIScaledInsets(4, 0, 4, 16),
                        mIconDarkColorPicker,
                        mIconBrightColorPicker,
                        mEngineComboBox
                );

                mPopoverToggleSwitch.selectedProperty().bindBidirectional(mOptions.preferPopoverProperty());
                mNightModeToggleSwitch.selectedProperty().bindBidirectional(mOptions.nightModeProperty());
                mIconBrightColorPicker.valueProperty().bindBidirectional(mOptions.iconColorBrightProperty());
                mIconDarkColorPicker.valueProperty().bindBidirectional(mOptions.iconColorDarkProperty());

                mEngineComboBox.valueProperty().bindBidirectional(mOptions.engineProperty());
                mCrosshairToggleSwitch.selectedProperty().bindBidirectional(mOptions.displayCrosshairProperty());
                mHomeIconToggleSwitch.selectedProperty().bindBidirectional(mOptions.displayHomeIconProperty());

                var scrollPane = new ScrollPane(gp);
                //TODO Remove scroll pane border

                return new Scene(gp);
            }

            private void initListeners() {
                ChangeListener<Object> changeListener = (ObservableValue<? extends Object> observable, Object oldValue, Object newValue) -> {
                    mController.changed();
                };

                mPopoverToggleSwitch.selectedProperty().addListener(changeListener);
                mNightModeToggleSwitch.selectedProperty().addListener(changeListener);
                mCrosshairToggleSwitch.selectedProperty().addListener(changeListener);
                mHomeIconToggleSwitch.selectedProperty().addListener(changeListener);

                mEngineComboBox.valueProperty().addListener(changeListener);
                mIconBrightColorPicker.valueProperty().addListener(changeListener);
                mIconDarkColorPicker.valueProperty().addListener(changeListener);

                Lookup.getDefault().lookupResult(MEngine.class).addLookupListener((LookupEvent ev) -> {
                    populateEngines();
                });

            }
        };

        mFxPanel.initFx(null);
        mFxPanel.setPreferredSize(null);

        setLayout(new BorderLayout());
        add(mFxPanel, BorderLayout.CENTER);
    }

    void cancel() {
        Platform.runLater(() -> {
            mOptions.preferPopoverProperty().set(mOldPopover);
            mOptions.nightModeProperty().set(mOldNightMode);
            mOptions.iconColorBrightProperty().set(mOldIconColorBright);
            mOptions.iconColorDarkProperty().set(mOldIconColorDark);

            mOptions.displayCrosshairProperty().set(mOldDisplayCrosshair);
            mOptions.displayHomeIconProperty().set(mOldDisplayHomeIcon);
            mOptions.engineProperty().set(mOldEngine);
        });
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
        mOldPopover = mOptions.isPreferPopover();
        mOldNightMode = mOptions.isNightMode();
        mOldDisplayHomeIcon = mOptions.isDisplayHomeIcon();
        mOldDisplayCrosshair = mOptions.isDisplayCrosshair();

        mOldEngine = mOptions.getEngine();

        mOldIconColorBright = mOptions.getIconColorBright();
        mOldIconColorDark = mOptions.getIconColorDark();
    }

    private void populateEngines() {
        List<? extends String> engines = Lookup.getDefault().lookupAll(MEngine.class)
                .stream()
                .sorted((MEngine o1, MEngine o2) -> o1.getName().compareTo(o2.getName()))
                .map(MEngine::getName)
                .collect(Collectors.toList());
        mEngineComboBox.setItems(FXCollections.observableArrayList(engines));
        mEngineComboBox.getSelectionModel().select(Mapton.getEngine().getName());
    }

    private void storeFX() {
        boolean newNightMode = mOptions.isNightMode();
        boolean newIconColorBright = newNightMode && !mOldIconColorBright.equals(mOptions.getIconColorBright());
        boolean newIconColorDark = !newNightMode && !mOldIconColorDark.equals(mOptions.getIconColorDark());

        if (mOldNightMode != newNightMode || newIconColorBright || newIconColorDark) {
            String laf = newNightMode ? "com.bulenkov.darcula.DarculaLaf" : UIManager.getSystemLookAndFeelClassName();
            NbPreferences.root().node("laf").put("laf", laf);

            Action restartAction = new Action(Dict.RESTART.toString(), (eventHandler) -> {
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            });

            Mapton.notification(MKey.NOTIFICATION_WARNING, mBundle.getString("actionRequired"), mBundle.getString("restartRequired"), restartAction);
        }
    }
}
