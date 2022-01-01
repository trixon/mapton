/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.geonames;

import java.io.IOException;
import org.mapton.api.MUpdater;
import org.mapton.geonames.api.GeonamesManager;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MUpdater.class)
public class GeonamesUpdater extends MUpdater.ByFile {

    private final GeonamesGenerator mGenerator = GeonamesGenerator.getInstance();

    public GeonamesUpdater() {
        setFile(mGenerator.getCities1000zipFile());
        setComment("Search engine data from geonames.org");
        setCategory(Dict.SYSTEM.toString());

        setRunnable(() -> {
            try {
                mGenerator.update(mPrint);
                GeonamesManager.getInstance().init();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });

        setAutoUpdateInterval(FREQ_2_WEEKS);
        initAutoUpdater();
    }

    @Override
    public String getName() {
        return "GeoNames";
    }
}
