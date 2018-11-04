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
package org.mapton.core.options;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.layout.VBox;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxPanel;
import org.mapton.api.MOptions;
import org.mapton.core.ui.EngineBox;

final class MainPanel extends javax.swing.JPanel {

    private final MainOptionsPanelController controller;
    private final ResourceBundle mBundle = NbBundle.getBundle(MainPanel.class);
    private final MOptions mOptions = MOptions.getInstance();
    private EngineBox mEngineBox;
    private final FxPanel mFxPanel;

    private CheckBox mPopoverCheckBox;

    MainPanel(MainOptionsPanelController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
            }

            private Scene createScene() {

                mPopoverCheckBox = new CheckBox(mBundle.getString("popoverCheckBox.text"));
                mEngineBox = new EngineBox();
                VBox box = new VBox(mPopoverCheckBox, mEngineBox);

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
        mPopoverCheckBox.setSelected(mOptions.isPreferPopover());
    }

    private void storeFX() {
        mOptions.setPreferPopover(mPopoverCheckBox.isSelected());
    }
}