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

import org.mapton.core_nb.api.BaseAction;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core_nb.actions.ClearIndicatorLayerAction"
)
@ActionRegistration(
        displayName = "#CTL_ClearIndicatorLayerAction"
)
@ActionReference(path = "Shortcuts", name = "DS-I")
@NbBundle.Messages("CTL_ClearIndicatorLayerAction=Clear indicators")
public final class ClearIndicatorLayerAction extends BaseAction implements ActionListener {

    public static String getName() {
        return Bundle.CTL_ClearIndicatorLayerAction();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Mapton.getGlobalState().put(MKey.INDICATOR_LAYER_LOAD, null);
        Mapton.notification(MKey.NOTIFICATION_INFORMATION, getName(), Dict.OPERATION_COMPLETED.toString());
    }
}
