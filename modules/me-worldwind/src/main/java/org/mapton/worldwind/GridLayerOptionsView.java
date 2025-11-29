/*
 * Copyright 2025 Patrik Karlström <patrik@trixon.se>.
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

import javafx.scene.layout.BorderPane;
import org.mapton.core.ui.grid.GridOptionsView;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public class GridLayerOptionsView extends BorderPane {

    private final GridOptionsView mGridOptionsView = new GridOptionsView();

    public GridLayerOptionsView() {
        mGridOptionsView.setPadding(FxHelper.getUIScaledInsets(8));
        setCenter(mGridOptionsView);
    }

}
