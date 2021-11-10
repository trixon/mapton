/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.poi_trv_ti.updater;

import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import org.mapton.api.MUpdater;
import org.mapton.poi_trv_ti.TrafficInformationManager.Service;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MUpdater.class)
public class CameraUpdater extends BaseUpdater {

    public CameraUpdater() {
        setFile(mManager.getFile(Service.CAMERA));
        setComment("Conditions & flow");

        setRunnable(() -> {
            try {
                mTrafficInformation.road().getCameraResults(null, null, mManager.getFile(Service.CAMERA));
                refreshPoiManager();
            } catch (IOException | InterruptedException | JAXBException ex) {
                mPrint.err(ex.getMessage());
                Exceptions.printStackTrace(ex);
                if (ex instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
            }
        });

        setAutoUpdateInterval(FREQ_1_WEEK);
        initAutoUpdater();
    }

    @Override
    public String getName() {
        return "Camera";
    }
}
