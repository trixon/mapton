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
import javafx.scene.paint.Color;
import javax.swing.UIManager;
import org.controlsfx.control.action.Action;
import org.mapton.api.MKey;
import org.mapton.api.MOptions2;
import org.mapton.api.Mapton;
import org.mapton.core.ui.options.OptionsPanel;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.LifecycleManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
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

    private final DelayedResetRunner mDelayedResetRunner;
    private MOptions2 mOptions2 = MOptions2.getInstance();
    private OptionsPanel optionsPanel = new OptionsPanel();

    public OptionsAction() {
        optionsPanel.initFx(() -> {
        });
        optionsPanel.setPreferredSize(SwingHelper.getUIScaledDim(800, 500));

        //Use DelayedResetRunner to trap double triggers
        mDelayedResetRunner = new DelayedResetRunner(20, () -> {
            boolean oldNightMode = mOptions2.general().isNightMode();
            Color oldIconColorBright = mOptions2.general().getIconColorBright();
            Color oldIconColorDark = mOptions2.general().getIconColorDark();

            DialogDescriptor d = new DialogDescriptor(optionsPanel, Dict.OPTIONS.toString());
            optionsPanel.setDialogDescriptor(d);

            if (DialogDescriptor.OK_OPTION == DialogDisplayer.getDefault().notify(d)) {
                MOptions2.getInstance().save();

                boolean newNightMode = mOptions2.general().isNightMode();
                boolean newIconColorBright = newNightMode && !oldIconColorBright.equals(mOptions2.general().getIconColorBright());
                boolean newIconColorDark = !newNightMode && !oldIconColorDark.equals(mOptions2.general().getIconColorDark());

                if (oldNightMode != newNightMode || newIconColorBright || newIconColorDark) {
                    String laf;
                    if (newNightMode) {
                        laf = "com.bulenkov.darcula.DarculaLaf";
                    } else {
                        laf = UIManager.getSystemLookAndFeelClassName();
                    }

                    NbPreferences.root().node("laf").put("laf", laf);

                    Action restartAction = new Action(Dict.RESTART.toString(), (eventHandler) -> {
                        LifecycleManager.getDefault().markForRestart();
                        LifecycleManager.getDefault().exit();
                    });

                    Mapton.notification(MKey.NOTIFICATION_WARNING, NbBundle.getMessage(OptionsPanel.class, "actionRequired"), NbBundle.getMessage(OptionsPanel.class, "restartRequired"), restartAction);
                }
            } else {
                MOptions2.getInstance().discardChanges();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        mDelayedResetRunner.reset();
    }

}
