/* 
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core.action.swing;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle.Messages;

@ActionID(
        category = "Mapton",
        id = "se.trixon.mapton.core.action.OnlyMapAction"
)
@ActionRegistration(
        displayName = "#CTL_OnlyMapAction"
)
@ActionReference(path = "Shortcuts", name = "F12")
@Messages("CTL_OnlyMapAction=Map")
public final class OnlyMapAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Actions.forID("Window", "se.trixon.mapton.core.map.MapTopComponent").actionPerformed(null);
        Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
    }
}
