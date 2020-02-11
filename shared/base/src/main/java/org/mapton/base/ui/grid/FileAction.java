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
package org.mapton.base.ui.grid;

import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.action.Action;
import org.mapton.api.MDict;
import org.mapton.api.MLocalGridManager;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public abstract class FileAction {

    protected final FileChooser.ExtensionFilter mExtGrid = new FileChooser.ExtensionFilter("Mapton Grid (*.grid)", "*.grid");
    protected Color mIconColor = Mapton.options().getIconColorForBackground();
    protected final MLocalGridManager mManager = MLocalGridManager.getInstance();
    protected PopOver mPopOver;
    protected final String mTitle = MDict.GRIDS.toString();

    public FileAction(PopOver popOver) {
        mPopOver = popOver;
    }

    public abstract Action getAction(Node owner);
}
