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
package org.mapton.butterfly_core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.mapton.butterfly_core.api.BaseManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(
        category = "Mapton",
        id = "org.mapton.butterfly_core.actions.ManagerToggleSelectionAction"
)
@ActionRegistration(
        displayName = "#CTL_ManagerToggleSelectionAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Navigate", position = 1000),
    @ActionReference(path = "Shortcuts", name = "SO-T")
})
@NbBundle.Messages("CTL_ManagerToggleSelectionAction=Växla senaste objekt")
public final class ManagerToggleSelectionAction implements ActionListener {

    public ManagerToggleSelectionAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BaseManager.restoreManagerAndItem();
    }

}
