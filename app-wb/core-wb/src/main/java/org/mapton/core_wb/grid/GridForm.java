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
package org.mapton.core_wb.grid;

import javafx.geometry.Insets;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.mapton.api.MActivatable;

public final class GridForm extends VBox implements MActivatable {

    private GlobalGridView mGlobalGridView;
    private LocalGridView mLocalGridView;

    public GridForm() {
        createUI();
    }

    @Override
    public void activate() {
        mLocalGridView.activate();
    }

    private void createUI() {
        mGlobalGridView = new GlobalGridView();
        mLocalGridView = new LocalGridView();

        mGlobalGridView.setPadding(new Insets(8));
        setSpacing(8);

        getChildren().setAll(
                mGlobalGridView,
                mLocalGridView
        );

        VBox.setVgrow(mLocalGridView, Priority.ALWAYS);
    }
}
