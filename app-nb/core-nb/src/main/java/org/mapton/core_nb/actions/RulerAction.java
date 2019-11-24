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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import org.mapton.core_nb.ui.RulerPanel;
import org.openide.DialogDescriptor;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import se.trixon.almond.util.Dict;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core_nb.actions.RulerAction"
)
@ActionRegistration(
        displayName = "Measure"
)
@ActionReference(path = "Shortcuts", name = "D-R")
public final class RulerAction extends BaseAction {

    private DialogDescriptor mDialogDescriptor;
    private RulerPanel mRulerPanel;

    public RulerAction() {
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //org.openide.DialogDisplayer.getDefault().notify(getDialogDescriptor());
        toggleTopComponent("RulerTopComponent");
    }

    private DialogDescriptor getDialogDescriptor() {
        if (mDialogDescriptor == null) {
            mRulerPanel = new RulerPanel();
            mDialogDescriptor = new DialogDescriptor(mRulerPanel, Dict.MEASURE.toString(), false, (ActionEvent e1) -> {
                System.out.println(e1);
            });
            mRulerPanel.setDialogDescriptor(mDialogDescriptor);
            mRulerPanel.initFx(() -> {
            });

            mRulerPanel.setPreferredSize(new Dimension(300, 300));
        }

        return mDialogDescriptor;
    }
}
