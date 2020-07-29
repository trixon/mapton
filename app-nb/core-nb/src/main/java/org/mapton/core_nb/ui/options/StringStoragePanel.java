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
import javafx.application.Platform;
import javafx.scene.Scene;
import org.mapton.base.ui.string_storage.StringStorageTabPane;
import se.trixon.almond.nbp.fx.FxPanel;

final class StringStoragePanel extends javax.swing.JPanel {

    private final StringStorageOptionsPanelController mController;
    private final FxPanel mFxPanel;
    private StringStorageTabPane mStringStorageTabPane;

    StringStoragePanel(StringStorageOptionsPanelController controller) {
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {
                mStringStorageTabPane = new StringStorageTabPane();

                return new Scene(mStringStorageTabPane);
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
            mStringStorageTabPane.load();
        });
    }

    void store() {
        Platform.runLater(() -> {
            mStringStorageTabPane.store();
        });
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

}
