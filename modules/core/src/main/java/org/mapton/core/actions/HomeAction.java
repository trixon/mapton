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
import javafx.application.Platform;
import org.mapton.api.Mapton;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.nbp.Almond;

@ActionID(
        category = "Mapton",
        id = "org.mapton.core.actions.HomeAction"
)
@ActionRegistration(
        displayName = "#CTL_HomeAction"
)
@ActionReference(path = "Shortcuts", name = "C-H")
@Messages("CTL_HomeAction=Home")
public final class HomeAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        Almond.requestActive("MapTopComponent");
        Platform.runLater(() -> {
            Mapton.getEngine().goHome();
        });
    }
}
