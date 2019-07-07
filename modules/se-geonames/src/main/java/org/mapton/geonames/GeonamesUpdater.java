/*
 * Copyright 2019 Patrik Karlström.
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
import java.util.concurrent.TimeUnit;
import org.mapton.api.MUpdater;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MUpdater.class)
public class GeonamesUpdater extends MUpdater.ByFile {

    private final GeonamesGenerator mGenerator = GeonamesGenerator.getInstance();

    public GeonamesUpdater() {
        setFile(mGenerator.getCities1000zipFile());
        setTooltipText("Search engine data from geonames.org");
        setRunnable(() -> {
            try {
                mGenerator.update(mPrint);
                GeonamesSearchEngine.init();
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        });
    }

    @Override
    public Long getAgeLimit() {
        return TimeUnit.DAYS.toMillis(14);
    }

    @Override
    public String getName() {
        return "GeoNames";
    }

    @Override
    public String getParent() {
        return null;
    }
}
