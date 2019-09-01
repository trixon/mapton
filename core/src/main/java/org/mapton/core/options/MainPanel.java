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
package org.mapton.core.options;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.swing.UIManager;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import static org.mapton.api.MOptions.*;
import org.mapton.api.Mapton;
import org.mapton.core.ui.EngineBox;
import org.openide.LifecycleManager;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;
import se.trixon.almond.nbp.fx.FxPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;

final class MainPanel extends javax.swing.JPanel {

    private final MainOptionsPanelController controller;
    private final ResourceBundle mBundle = NbBundle.getBundle(MainPanel.class);
    private CheckBox mCrosshairCheckBox;
    private CheckBox mDarkThemeCheckBox;
    private EngineBox mEngineBox;
    private final FxPanel mFxPanel;
    private ColorPicker mIconColorPicker;
    private boolean mOldDark;
    private Color mOldIconColorBright;
    private Color mOldIconColorDark;
    private final MOptions mOptions = MOptions.getInstance();

    private CheckBox mPopoverCheckBox;

    MainPanel(MainOptionsPanelController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {

                mCrosshairCheckBox = new CheckBox(mBundle.getString("croshairCheckBox.text"));
                mPopoverCheckBox = new CheckBox(mBundle.getString("popoverCheckBox.text"));
                mDarkThemeCheckBox = new CheckBox(Dict.DARK_THEME.toString());
                mEngineBox = new EngineBox();
                mIconColorPicker = new ColorPicker();
                Label iconColorLabel = new Label(mBundle.getString("iconColorLabel.text"));

                VBox box = new VBox(8,
                        mCrosshairCheckBox,
                        mPopoverCheckBox,
                        mDarkThemeCheckBox,
                        new VBox(
                                iconColorLabel,
                                mIconColorPicker
                        ),
                        mEngineBox
                );

                box.setPadding(new Insets(16));

                return new Scene(box);
            }

            private void initListeners() {
                mDarkThemeCheckBox.selectedProperty().addListener((ObservableValue<? extends Boolean> ov, Boolean t, Boolean t1) -> {
                    mIconColorPicker.setValue(t1 ? mOldIconColorDark : mOldIconColorBright);
                });
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
        mCrosshairCheckBox.setSelected(mOptions.is(KEY_DISPLAY_CROSSHAIR));
        mPopoverCheckBox.setSelected(mOptions.isPreferPopover());
        mOldDark = mOptions.is(KEY_UI_LAF_DARK, DEFAULT_UI_LAF_DARK);
        mDarkThemeCheckBox.setSelected(mOldDark);

        mOldIconColorBright = mOptions.getIconColorBright();
        mOldIconColorDark = mOptions.getIconColorDark();

        mIconColorPicker.setValue(mOldDark ? mOldIconColorDark : mOldIconColorBright);
    }

    private void storeFX() {
        mOptions.put(KEY_DISPLAY_CROSSHAIR, mCrosshairCheckBox.isSelected());
        mOptions.setPreferPopover(mPopoverCheckBox.isSelected());
        boolean newDark = mDarkThemeCheckBox.isSelected();
        mOptions.put(KEY_UI_LAF_DARK, newDark);

        Color newColor = mIconColorPicker.getValue();
        boolean colorChanged = FxHelper.colorToHexInt(newColor) != FxHelper.colorToHexInt(mOptions.getIconColor());
        mOptions.setIconColor(newColor);

        if (mOldDark != newDark || colorChanged) {
            String laf;
            if (newDark) {
                laf = "com.bulenkov.darcula.DarculaLaf";
            } else {
                laf = UIManager.getSystemLookAndFeelClassName();
            }

            NbPreferences.root().node("laf").put("laf", laf);

            Action restartAction = new Action(Dict.RESTART.toString(), (eventHandler) -> {
                LifecycleManager.getDefault().markForRestart();
                LifecycleManager.getDefault().exit();
            });

            Mapton.notification(MKey.NOTIFICATION_WARNING, mBundle.getString("actionRequired"), mBundle.getString("restartRequired"), restartAction);
        }
    }
}
