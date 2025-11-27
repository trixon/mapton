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
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BBase;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(
        category = "Mapton",
        id = "org.mapton.butterfly_core.actions.SwapSelectionAction"
)
@ActionRegistration(
        displayName = "#CTL_SwapSelectionAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/Navigate", position = 1000),
    @ActionReference(path = "Shortcuts", name = "SO-T")
})
@NbBundle.Messages("CTL_SwapSelectionAction=Växla senaste objekt")
public final class SwapSelectionAction implements ActionListener {

    private static BaseManager< BBase> sPrevManager;
    private static BaseManager< BBase> sCurrManager;
    private static BBase sCurrItem;
    private static BBase sPrevItem;

    public SwapSelectionAction() {
    }

    public static void store(BaseManager< ? extends BBase> manager, BBase item) {
        if (ObjectUtils.allNotNull(manager, item) && item != sCurrItem) {
            sPrevManager = sCurrManager;
            sPrevItem = sCurrItem;
            sCurrManager = (BaseManager<BBase>) manager;
            sCurrItem = item;
        }

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        restore();
    }

    private void restore() {
        if (ObjectUtils.allNotNull(sPrevManager, sPrevItem)) {
            sPrevManager.setSelectedItem(null);
            sPrevManager.setSelectedItem(sPrevItem);
        }
    }
}
