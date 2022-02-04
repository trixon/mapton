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
package org.mapton.core.ui.options;

import java.awt.BorderLayout;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import org.mapton.api.MOptions;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxPanel;

/**
 *
 * @author Patrik Karlström
 */
final class CrsPanel extends javax.swing.JPanel {

    private final CrsOptionsPanelController mController;
    private final FxPanel mFxPanel;
    private final MOptions mOptions = MOptions.getInstance();
    private final ResourceBundle mBundle;

    CrsPanel(CrsOptionsPanelController controller) {
        mBundle = NbBundle.getBundle(MOptions.class);
        mController = controller;
        mFxPanel = new FxPanel() {
            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {
                return new Scene(new Label("TODO"));
            }

            private void initListeners() {
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
