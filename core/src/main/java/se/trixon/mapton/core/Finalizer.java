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
package se.trixon.mapton.core;

import org.openide.modules.OnStop;
import se.trixon.mapton.core.api.MapEngine;
import se.trixon.mapton.core.api.Mapton;
import se.trixon.mapton.core.api.MaptonOptions;

/**
 *
 * @author Patrik Karlström
 */
@OnStop
public class Finalizer implements Runnable {

    private final MaptonOptions mOptions = MaptonOptions.getInstance();

    @Override
    public void run() {
        final MapEngine engine = Mapton.getEngine();
        try {
            mOptions.setMapZoom(engine.getZoom());
            mOptions.setMapCenter(engine.getCenter());
        } catch (Exception e) {
            //nvm
        }
    }

}
