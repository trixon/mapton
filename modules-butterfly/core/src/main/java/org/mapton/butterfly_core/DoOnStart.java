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
package org.mapton.butterfly_core;

import java.util.prefs.BackingStoreException;
import org.mapton.api.MCooTrans;
import org.mapton.api.MOptions;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.PrefsHelper;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class DoOnStart implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        var preferences = NbPreferences.forModule(MCooTrans.class);

        try {
            PrefsHelper.putIfAbsent(preferences, "map.coo_trans", "SWEREF99 12 00");
            int startCounter = PrefsHelper.inc(mOptions.getPreferences(), MOptions.KEY_APP_START_COUNTER);

            if (startCounter == 1) {
                var key = "laf";
                var defaultLAF = "com.formdev.flatlaf.FlatLightLaf";
                NbPreferences.root().node("laf").put(key, defaultLAF);
                mOptions.put(MOptions.KEY_UI_LAF_DARK, false);
            }
        } catch (BackingStoreException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
