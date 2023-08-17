/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.worldwind;

import java.awt.BorderLayout;
import javafx.scene.Scene;
import javax.swing.JPanel;
import se.trixon.almond.nbp.fx.FxPanel;

final class MapEnginePanel extends JPanel {

    private final MapEngineOptionsPanelController mController;
    private final FxPanel mFxPanel;

    MapEnginePanel(MapEngineOptionsPanelController controller) {
        mController = controller;
        mFxPanel = new FxPanel() {

            @Override
            protected void fxConstructor() {
                setScene(createScene());
                initListeners();
            }

            private Scene createScene() {
                return new Scene(new MapEngineView());
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
    }

    void store() {
    }

    boolean valid() {
        return true;
    }

}
