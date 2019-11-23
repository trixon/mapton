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
package org.mapton.core_nb.ui.grid;

import javafx.stage.FileChooser;
import org.controlsfx.control.action.Action;
import org.mapton.api.MDict;
import org.mapton.api.MLocalGridManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class GridFileAction {

    protected final FileChooser.ExtensionFilter mExtGrid = new FileChooser.ExtensionFilter("Mapton Grid (*.grid)", "*.grid");
    protected final MLocalGridManager mManager = MLocalGridManager.getInstance();
    protected final String mTitle = MDict.GRIDS.toString();

    public abstract Action getAction();
}
