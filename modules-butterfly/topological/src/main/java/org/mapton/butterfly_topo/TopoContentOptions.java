/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_topo;

import com.dlsc.gemsfx.util.SessionManager;
import org.mapton.butterfly_core.api.BContentOptions;

/**
 *
 * @author Patrik Karlström
 */
public class TopoContentOptions extends BContentOptions {

    public static TopoContentOptions getInstance() {
        return Holder.INSTANCE;
    }

    private TopoContentOptions() {
        setPreferences(getPreferencesForPath());
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        super.initSession(sessionManager);
    }

    private static class Holder {

        private static final TopoContentOptions INSTANCE = new TopoContentOptions();
    }
}
