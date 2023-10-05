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
package org.mapton.butterfly_group;

import org.mapton.api.MKey;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.openide.modules.OnStart;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class DoOnStart implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        Mapton.getGlobalState().put(MKey.MAP_LOGO_URL, getClass().getResource("scior-logo.png"));

        var firstRun = mOptions.getPreferences().getInt(MOptions.KEY_APP_START_COUNTER, 1) == 1;
        if (firstRun) {
            var key = "laf";
            var defaultLAF = "com.formdev.flatlaf.FlatLightLaf";
            NbPreferences.root().node("laf").put(key, defaultLAF);
            mOptions.put(MOptions.KEY_UI_LAF_DARK, false);
        }
    }
}
