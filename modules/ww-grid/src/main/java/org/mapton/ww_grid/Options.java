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
package org.mapton.ww_grid;

import org.openide.util.NbPreferences;
import se.trixon.almond.util.OptionsBase;

/**
 *
 * @author Patrik Karlström
 */
public class Options extends OptionsBase {

    public static final String KEY_GLOBAL_CLAMP_TO_GROUND = "global_clamp_to_ground";
    public static final String KEY_GLOBAL_EQUATOR = "global_equator";
    public static final String KEY_GLOBAL_LATITUDES = "global_latitudes";
    public static final String KEY_GLOBAL_LONGITUDES = "global_longitudes";
    public static final String KEY_GLOBAL_PLOT = "global_plot";
    public static final String KEY_GLOBAL_POLAR_ANTARCTIC = "global_polar_antarctic";
    public static final String KEY_GLOBAL_POLAR_ARCTIC = "global_polar_arctic";
    public static final String KEY_GLOBAL_TROPIC_CANCER = "global_tropic_cancer";
    public static final String KEY_GLOBAL_TROPIC_CAPRICORN = "global_tropic_capricorn";
    public static final String KEY_LOCAL_GRIDS = "local_grids";
    public static final String KEY_LOCAL_PLOT = "local_plot";

    public static Options getInstance() {
        return Holder.INSTANCE;
    }

    private Options() {
        setPreferences(NbPreferences.forModule(Options.class));
    }

    private static class Holder {

        private static final Options INSTANCE = new Options();
    }
}
