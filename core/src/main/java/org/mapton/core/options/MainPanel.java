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
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import org.mapton.api.MOptions;
import org.mapton.core.ui.EngineBox;
import se.trixon.almond.nbp.fx.FxPanel;

final class MainPanel extends javax.swing.JPanel {

    private final MainOptionsPanelController controller;
    private EngineBox mEngineBox;
    private final FxPanel mFxPanel;
    private final MOptions mOptions = MOptions.getInstance();

    MainPanel(MainOptionsPanelController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
            }

            private Scene createScene() {

                mEngineBox = new EngineBox();

                VBox box = new VBox(8,
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
    }

    private void storeFX() {
    }
}
