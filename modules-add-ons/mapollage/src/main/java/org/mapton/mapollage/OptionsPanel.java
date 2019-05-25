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
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import se.trixon.almond.nbp.fx.FxPanel;

final class OptionsPanel extends javax.swing.JPanel {

    private final OptionsController controller;
    private final FxPanel mFxPanel;
    private final Options mOptions = Options.getInstance();

    OptionsPanel(OptionsController controller) {
        this.controller = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
            }

            private Scene createScene() {
                VBox box = new VBox();

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
