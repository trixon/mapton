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
package org.mapton.core_nb.tool.map;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import org.controlsfx.control.action.Action;
import org.mapton.api.MToolMap;
import org.mapton.api.MToolMapCommand;
import org.mapton.core_nb.actions.ClearIndicatorLayerAction;
import org.openide.awt.Actions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MToolMap.class)
public class ClearIndicatorsTool extends MToolMapCommand {

    @Override
    public Action getAction() {
        Action action = new Action(
                FxHelper.createTitleAndKeyCode(ClearIndicatorLayerAction.getName(), KeyCode.I, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN) + "<",
                evt -> {
                    SwingHelper.runLaterDelayed(0, () -> {
                        Actions.forID("Mapton", "org.mapton.core_nb.actions.ClearIndicatorLayerAction").actionPerformed(null);
                    });
                });

        return action;
    }
}
