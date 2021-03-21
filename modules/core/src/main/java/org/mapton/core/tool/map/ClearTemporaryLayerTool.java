/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.core.tool.map;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.action.Action;
import org.mapton.api.MToolMapCommand;
import org.mapton.core.actions.ClearTemporaryLayerAction;
import org.openide.awt.Actions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MToolMapCommand.class)
public class ClearTemporaryLayerTool extends MToolMapCommand {

    @Override
    public Action getAction() {
        Action action = new Action(ClearTemporaryLayerAction.getName(), evt -> {
            SwingHelper.runLater(() -> {
                Actions.forID("Mapton", "org.mapton.core.actions.ClearTemporaryLayerAction").actionPerformed(null);
            });
        });

        return action;
    }

    @Override
    public KeyCodeCombination getKeyCodeCombination() {
        return new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN);
    }
}
