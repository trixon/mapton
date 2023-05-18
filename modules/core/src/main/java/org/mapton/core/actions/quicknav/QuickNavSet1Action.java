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
package org.mapton.core.actions.quicknav;

import java.awt.event.ActionEvent;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.quicknav.QuickNavSet1Action"
)
@ActionRegistration(
        displayName = "#CTL_QuickNav1Set"
)
@ActionReferences({
    @ActionReference(path = "Menu/Navigate/QuickNav", position = 210, separatorBefore = 200),
    @ActionReference(path = "Shortcuts", name = "DS-1")
})
@NbBundle.Messages("CTL_QuickNav1Set=Set 1")
public final class QuickNavSet1Action extends QuickNavAction {

    public QuickNavSet1Action() {
        super("1");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        set();
    }
}
