/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.poi_trv_ti;

import java.io.IOException;
import javafx.util.Duration;
import javax.xml.bind.JAXBException;
import static org.mapton.poi_trv_ti.TrafficInformationManager.Service.*;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import se.trixon.trv_ti.TrafficInformation;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Downloader implements Runnable {

    private final Duration mFreqCamera = Duration.hours(168);
    private final Duration mFreqTrafficeSafetyCamera = Duration.hours(168);
    private final Duration mFreqWeatherStation = Duration.minutes(30);
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private final TrafficInformation mTrafficInformation = mManager.getTrafficInformation();

    public Downloader() {
    }

    @Override
    public void run() {
        System.out.println("RUN");
        new Thread(() -> {
            new DownloadJob(mManager.getFile(CAMERA), mFreqCamera, () -> {
                try {
                    mTrafficInformation.road().getCameraResults(null, null, mManager.getFile(CAMERA));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            new DownloadJob(mManager.getFile(TRAFFIC_SAFETY_CAMERA), mFreqTrafficeSafetyCamera, () -> {
                try {
                    mTrafficInformation.road().getTrafficSafetyCameraResults(null, null, mManager.getFile(TRAFFIC_SAFETY_CAMERA));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            new DownloadJob(mManager.getFile(WEATHER_STATION), mFreqWeatherStation, () -> {
                try {
                    mTrafficInformation.road().getWeatherStationResults(null, null, mManager.getFile(WEATHER_STATION));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        }).start();
    }
}
