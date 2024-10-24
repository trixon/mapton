/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.worldwind;

import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.modules.OnStart;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class DoOnStart implements Runnable {

    @Override
    public void run() {
        Mapton.getExecutionFlow().executeWhenReady(MKey.EXECUTION_FLOW_MAP_WW_INITIALIZED, () -> {
            Mapton.getGlobalState().addListener(gsce -> {
                OverlayManager.getInstance().populateOverlayLayers();
            }, MKey.DATA_SOURCES_WMS_SOURCES);

            OverlayManager.getInstance().populateOverlayLayers();
        });
    }

}
