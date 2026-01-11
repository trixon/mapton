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
package org.mapton.butterfly_rock_earthquake.updater;

import java.io.IOException;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MUpdater;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MUpdater.class)
public class EarthquakeUpdater extends MUpdater.ByFile {

    private final EarthquakeGenerator mGenerator = EarthquakeGenerator.getInstance();

    public EarthquakeUpdater() {
        setFile(mGenerator.getTrackerFile());
        setComment("QuakeML");
        setCategory("Earthquakes");
        setAutoUpdate(true);

        setRunnable(() -> {
            try {
                mGenerator.update(mPrint);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        setAutoUpdateInterval(FREQ_10_MINUTES);
        initAutoUpdater();
    }

    @Override
    public String getName() {
        return "Earthquakes USGS";
    }

    @Override
    public boolean isAutoUpdateEnabled() {
        return MSimpleObjectStorageManager.getInstance().getBoolean(AutoUpdateProvider.class, false);
    }
}
