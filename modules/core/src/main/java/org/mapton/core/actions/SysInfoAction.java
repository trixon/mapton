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
import java.awt.event.ActionListener;
import org.mapton.core.api.MaptonNb;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbSystemInformation;
import se.trixon.almond.util.Dict;

@ActionID(
        category = "Tools",
        id = "org.mapton.core.actions.SysInfoAction"
)
@ActionRegistration(
        displayName = "#CTL_SysInfoAction"
)
@ActionReference(path = "Menu/Tools", position = 1150, separatorBefore = 1149)
@Messages("CTL_SysInfoAction=S&ystem information")
public final class SysInfoAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        new Thread(() -> {
            MaptonNb.progressStart(Dict.SYSTEM_INFORMATION.toString());
            new NbSystemInformation().displayDialog();
            MaptonNb.progressStop(Dict.SYSTEM_INFORMATION.toString());
        }).start();
    }
}
