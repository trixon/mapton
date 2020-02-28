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
package org.mapton.poi_nb;

import org.controlsfx.control.action.Action;
import org.mapton.api.MDict;
import org.mapton.api.MToolMap;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.nbp.Almond;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MToolMap.class)
public class PoiTool implements MToolMap {

    @Override
    public Action getAction() {
        FxActionSwing action = new FxActionSwing(MDict.POI.toString(), () -> {
            Almond.openAndActivateTopComponent("PoiTopComponent");
        });

        return action;
    }

    @Override
    public String getParent() {
        return String.format("- %s -", Dict.SYSTEM.toString());
    }
}
