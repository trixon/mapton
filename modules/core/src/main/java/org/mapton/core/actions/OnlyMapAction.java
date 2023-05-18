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
package org.mapton.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.mapton.api.MOptions;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.swing.SwingHelper;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.OnlyMapAction"
)
@ActionRegistration(
        displayName = "#CTL_OnlyMapAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 9999),
    @ActionReference(path = "Shortcuts", name = "F12")
})
@Messages("CTL_OnlyMapAction=Toggle &map mode")
public final class OnlyMapAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        var options = MOptions.getInstance();
        options.setMapOnly(!options.isMapOnly());
        Almond.openAndActivateTopComponent("MapTopComponent");
        SwingHelper.runLaterDelayed(10, () -> {
            Actions.forID("Window", "org.netbeans.core.windows.actions.ShowEditorOnlyAction").actionPerformed(null);
        });
    }
}
