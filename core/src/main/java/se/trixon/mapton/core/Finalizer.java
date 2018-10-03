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
import org.openide.util.Lookup;
import se.trixon.mapton.api.MEngine;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
@OnStop
public class Finalizer implements Runnable {

    private final MOptions mOptions = MOptions.getInstance();

    @Override
    public void run() {
        final MEngine engine = Mapton.getEngine();

        Mapton.execute(() -> {
            mOptions.setMapZoom(engine.getZoom());
            mOptions.setMapCenter(engine.getCenter());
        });

        Lookup.getDefault().lookupAll(MEngine.class).forEach((MEngine mapEngine) -> {
            if (mapEngine.isInitialized()) {
                Mapton.execute(mapEngine::onClosing);
            }
        });
    }

}
