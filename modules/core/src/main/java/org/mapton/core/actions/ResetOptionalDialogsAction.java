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
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.dialogs.NbMessage;
import se.trixon.almond.nbp.dialogs.NbOptionalDialog;
import se.trixon.almond.util.Dict;

@ActionID(
        category = "Tools",
        id = "org.mapton.core.actions.ResetOptionalDialogsAction"
)
@ActionRegistration(
        displayName = "#CTL_ResetOptionalDialogsAction"
)
@ActionReference(path = "Menu/Tools", position = 1151)
@Messages("CTL_ResetOptionalDialogsAction=Reset d&ialogs")
public final class ResetOptionalDialogsAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        NbOptionalDialog.resetAll();
        NbMessage.information(Dict.INFORMATION.toString(), Dict.OPERATION_COMPLETED.toString());
    }
}
