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
package org.mapton.core.ui.grid;

import javafx.scene.Scene;
import org.mapton.api.MLocalGrid;
import org.mapton.base.ui.grid.LocalGridView;
import se.trixon.almond.nbp.fx.FxDialogPanel;

/**
 *
 * @author Patrik Karlström
 */
public class LocalGridPanel extends FxDialogPanel {

    private LocalGridView mLocalGridView;

    public void load(MLocalGrid grid) {
        mLocalGridView.load(grid);
    }

    public void save(MLocalGrid grid) {
        mLocalGridView.save(grid);
    }

    @Override
    protected void fxConstructor() {
        mLocalGridView = new LocalGridView();
        setScene(new Scene(mLocalGridView));
    }

}
