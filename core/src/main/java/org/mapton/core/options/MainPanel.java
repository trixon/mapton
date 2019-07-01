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
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
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

final class MainPanel extends javax.swing.JPanel {

    private final MainOptionsPanelController controller;
    private final ResourceBundle mBundle = NbBundle.getBundle(MainPanel.class);
    private CheckBox mCrosshairCheckBox;
    private CheckBox mDarkThemeCheckBox;
    private EngineBox mEngineBox;
    private final FxPanel mFxPanel;
    private boolean mOldDark;
    private final MOptions mOptions = MOptions.getInstance();

    private CheckBox mPopoverCheckBox;

    MainPanel(MainOptionsPanelController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
            }

            private Scene createScene() {

                mCrosshairCheckBox = new CheckBox(mBundle.getString("croshairCheckBox.text"));
                mPopoverCheckBox = new CheckBox(mBundle.getString("popoverCheckBox.text"));
                mDarkThemeCheckBox = new CheckBox(mBundle.getString("darkThemeCheckBox.text"));
                mEngineBox = new EngineBox();

                VBox box = new VBox(8,
                        mCrosshairCheckBox,
                        mPopoverCheckBox,
                        mDarkThemeCheckBox,
                        mEngineBox
                );

                box.setPadding(new Insets(16));

                return new Scene(box);
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
    }

    private void storeFX() {
        mOptions.put(KEY_DISPLAY_CROSSHAIR, mCrosshairCheckBox.isSelected());
        mOptions.setPreferPopover(mPopoverCheckBox.isSelected());
        boolean newDark = mDarkThemeCheckBox.isSelected();
        mOptions.put(KEY_UI_LAF_DARK, newDark);

        if (mOldDark != newDark) {
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

            Mapton.notification(MKey.NOTIFICATION_INFORMATION, mBundle.getString("actionRequired"), mBundle.getString("restartRequired"), restartAction);
        }
    }
}
