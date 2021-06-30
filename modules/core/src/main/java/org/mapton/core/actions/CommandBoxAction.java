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
package org.mapton.core.actions;

import java.awt.event.ActionEvent;
import org.mapton.core.api.BaseAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.CommandBoxAction"
)
@ActionRegistration(
        displayName = "#CTL_CommandBoxAction"
)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-A"),
    @ActionReference(path = "Menu/Tools", position = 9)
})
@NbBundle.Messages({
    "CTL_CommandBoxAction=Comm&ands"
})
public final class CommandBoxAction extends BaseAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        if (isMapActive()) {
            toggleTopComponent("CommandBoxTopComponent");
        }
    }
}
