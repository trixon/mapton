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
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbMessage;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.AboutMapsAction"
)
@ActionRegistration(
        displayName = "#CTL_AboutMapsAction"
)
@ActionReference(path = "Menu/Help", position = 1000, separatorBefore = 750)
@Messages("CTL_AboutMapsAction=About &maps")
public final class AboutMapsAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        NbMessage.information(
                Actions.cutAmpersand(Bundle.CTL_AboutMapsAction()),
                NbBundle.getMessage(AboutMapsAction.class, "about_maps")
        );
    }
}
