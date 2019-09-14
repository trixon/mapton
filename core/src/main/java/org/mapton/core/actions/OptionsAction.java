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
package org.mapton.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import org.mapton.api.MOptions2;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.fx.FxDialogPanel;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.DelayedResetRunner;
import se.trixon.almond.util.swing.SwingHelper;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.OptionsAction"
)
@ActionRegistration(
        displayName = "#CTL_OptionsAction"
)
@ActionReference(path = "Shortcuts", name = "D-COMMA")
@Messages("CTL_OptionsAction=Options")
public final class OptionsAction implements ActionListener {

    private DelayedResetRunner mDelayedResetRunner;
    private OptionsPanel optionsPanel = new OptionsPanel();

    public OptionsAction() {
        optionsPanel.initFx(() -> {
        });
        optionsPanel.setPreferredSize(SwingHelper.getUIScaledDim(800, 500));

        //Use this one to trap double triggers
        mDelayedResetRunner = new DelayedResetRunner(10, () -> {
            DialogDescriptor d = new DialogDescriptor(optionsPanel, Dict.OPTIONS.toString());
            optionsPanel.setDialogDescriptor(d);

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                MOptions2.getInstance().save();
            } else {
                MOptions2.getInstance().discardChanges();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mDelayedResetRunner.reset();
    }

    class OptionsPanel extends FxDialogPanel {

        public OptionsPanel() {
        }

        @Override
        protected void fxConstructor() {
            setScene(createScene());
        }

        private Scene createScene() {
            return new Scene(new BorderPane(MOptions2.getInstance().getPreferencesFxView()));
        }
    }
}
