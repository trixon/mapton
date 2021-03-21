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
package org.mapton.addon.panopticon_nb;

import org.controlsfx.control.action.Action;
import org.mapton.addon.panopticon.api.Panopticon;
import org.mapton.api.MToolMap;
import org.mapton.api.MToolMapAddOn;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.fx.FxActionSwing;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MToolMap.class)
public class PanopticonTool extends MToolMapAddOn {

    public static final String NAME = Panopticon.NAME;

    @Override

    public Action getAction() {
        FxActionSwing action = new FxActionSwing(NAME, () -> {
            Almond.openAndActivateTopComponent("PanopticonTopComponent");
        });

        return action;
    }
}
