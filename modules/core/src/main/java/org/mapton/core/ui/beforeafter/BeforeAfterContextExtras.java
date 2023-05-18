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
package org.mapton.core.ui.beforeafter;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.awt.Actions;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;

/**
 *
 * @author Patrik Karlström
 */
@NbBundle.Messages({
    "CTL_BeforeAfterMenu=&Send to 'Before and after'"
})
@ServiceProvider(service = MContextMenuItem.class)
public class BeforeAfterContextExtras extends MContextMenuItem {

    public BeforeAfterContextExtras() {
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return event -> {
            try {
                Mapton.getGlobalState().put(MKey.BEFORE_AFTER_IMAGE, Mapton.getEngine().getImageRenderer().call());
                Almond.openAndActivateTopComponent("BeforeAfterTopComponent");
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        };
    }

    @Override
    public String getName() {
        return Actions.cutAmpersand(Bundle.CTL_BeforeAfterMenu());
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

}
