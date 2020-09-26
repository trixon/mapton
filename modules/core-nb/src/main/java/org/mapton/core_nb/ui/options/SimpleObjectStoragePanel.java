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
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.geometry.Side;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.mapton.api.MGenericLoader;
import org.mapton.api.MGenericSaver;
import org.mapton.base.ui.simple_object_storage.BooleanStorageTabPane;
import org.mapton.base.ui.simple_object_storage.StringStorageTabPane;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.fx.FxPanel;

final class SimpleObjectStoragePanel extends javax.swing.JPanel {

    private final SimpleObjectStorageOptionsPanelController mController;
    private final FxPanel mFxPanel;
    private TabPane mTabPane;
    private final ResourceBundle mBundle = NbBundle.getBundle(SimpleObjectStoragePanel.class);

    SimpleObjectStoragePanel(SimpleObjectStorageOptionsPanelController controller) {
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {
                mTabPane = new TabPane();
                mTabPane.setSide(Side.TOP);
                mTabPane.getTabs().setAll(
                        new Tab(mBundle.getString("string"), new StringStorageTabPane()),
                        new Tab(mBundle.getString("boolean"), new BooleanStorageTabPane())
                );

                mTabPane.getTabs().forEach(tab -> {
                    tab.setClosable(false);
                });

                return new Scene(mTabPane);
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
            mTabPane.getTabs().stream()
                    .map(tab -> tab.getContent())
                    .filter(content -> (content instanceof MGenericLoader))
                    .forEachOrdered(content -> {
                        ((MGenericLoader) content).load(null);
                    });
        });
    }

    void store() {
        Platform.runLater(() -> {
            mTabPane.getTabs().stream()
                    .map(tab -> tab.getContent())
                    .filter(content -> (content instanceof MGenericSaver))
                    .forEachOrdered(content -> {
                        ((MGenericSaver) content).save(null);
                    });
        });
    }

    boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }

}
