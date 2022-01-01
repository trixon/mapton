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
package org.mapton.core.ui.grid;

import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public final class GridView extends VBox {

    private GlobalGridView mGlobalGridView;
    private LocalGridsView mLocalGridView;

    public GridView() {
        createUI();
    }

    private void createUI() {
        mGlobalGridView = new GlobalGridView();
        mLocalGridView = new LocalGridsView();

        mGlobalGridView.setPadding(new Insets(8));
        setSpacing(8);

        getChildren().setAll(
                mGlobalGridView,
                mLocalGridView
        );

        VBox.setVgrow(mLocalGridView, Priority.ALWAYS);
    }
}
