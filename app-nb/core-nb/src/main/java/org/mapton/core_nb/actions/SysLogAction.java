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
package org.mapton.core_nb.actions;

import java.awt.event.ActionEvent;
import org.mapton.core_nb.api.BaseAction;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core_nb.actions.SysLogAction"
)
@ActionRegistration(
        displayName = "#CTL_SysLogAction"
)
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "DS-4"),
    @ActionReference(path = "Menu/View", position = 1)
})
@NbBundle.Messages({
    "CTL_SysLogAction=&System log"
})
public final class SysLogAction extends BaseAction {

    @Override
    public void actionPerformed(ActionEvent e) {
        Actions.forID("View", "org.netbeans.core.actions.LogAction").actionPerformed(null);
        Actions.forID("Window", "org.netbeans.core.io.ui.IOWindowAction").actionPerformed(null);
    }
}
