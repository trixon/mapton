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
package org.mapton.core_nb.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import org.apache.commons.lang3.SystemUtils;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.core_nb.ui.AppToolBar;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbSystemInformation;

@ActionID(
        category = "Tools",
        id = "org.mapton.core_nb.actions.SysInfoAction"
)
@ActionRegistration(
        displayName = "#CTL_SysInfoAction"
)
@ActionReference(path = "Menu/Tools", position = 1150)
@Messages("CTL_SysInfoAction=S&ystem information")
public final class SysInfoAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        ResourceBundle bundle = NbBundle.getBundle(AppToolBar.class);

        if (SystemUtils.IS_OS_WINDOWS) {
            Mapton.notification(MKey.NOTIFICATION_INFORMATION, bundle.getString("collecting_system_information"), bundle.getString("stay_alert"));
        }

        new Thread(() -> {
            new NbSystemInformation().displayDialog();
        }).start();
    }
}
