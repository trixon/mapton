/* 
 * Copyright 2018 Patrik Karlström.
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
package org.mapton.core.tool;

import org.controlsfx.control.action.Action;
import org.mapton.api.MTool;
import org.openide.awt.Actions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MTool.class)
public class LogSystemTool implements MTool {

    @Override
    public Action getAction() {
        FxActionSwing action = new FxActionSwing(Dict.LOG_SYSTEM.toString(), () -> {
            Actions.forID("View", "org.netbeans.core.actions.LogAction").actionPerformed(null);
            Actions.forID("Window", "org.netbeans.core.io.ui.IOWindowAction").actionPerformed(null);
        });

        return action;
    }

    @Override
    public String getParent() {
        return String.format("%s/%s", Dict.SYSTEM.toString(), Dict.LOGGING.toString());
    }
}
