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
import org.mapton.poi_trv_ti.TrafficInformationManager.Service;
import org.openide.modules.OnStart;
import org.openide.util.Exceptions;
import se.trixon.almond.util.SystemHelper;
import se.trixon.trv_ti.TrafficInformation;

/**
 *
 * @author Patrik Karlström
 */
@OnStart
public class Downloader implements Runnable {

    private static final Duration FREQ_1_WEEK = Duration.hours(168);
    private static final Duration FREQ_2_MINUTES = Duration.minutes(2);
    private static final Duration FREQ_2_HOURS = Duration.hours(2);
    private static final Duration FREQ_30_MINUTES = Duration.minutes(30);
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private final TrafficInformation mTrafficInformation = mManager.getTrafficInformation();

    public Downloader() {
    }

    @Override
    public void run() {
        SystemHelper.runLaterDelayed(10 * 1000, () -> {
            new DownloadJob(mManager.getFile(Service.CAMERA), FREQ_1_WEEK, () -> {
                try {
                    mTrafficInformation.road().getCameraResults(null, null, mManager.getFile(Service.CAMERA));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            new DownloadJob(mManager.getFile(Service.TRAFFIC_SAFETY_CAMERA), FREQ_1_WEEK, () -> {
                try {
                    mTrafficInformation.road().getTrafficSafetyCameraResults(null, null, mManager.getFile(Service.TRAFFIC_SAFETY_CAMERA));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            new DownloadJob(mManager.getFile(Service.WEATHER_STATION), FREQ_30_MINUTES, () -> {
                try {
                    mTrafficInformation.road().getWeatherStationResults(null, null, mManager.getFile(Service.WEATHER_STATION));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });

            new DownloadJob(mManager.getFile(Service.PARKING), FREQ_2_HOURS, () -> {
                try {
                    mTrafficInformation.road().getParkingResults(null, null, mManager.getFile(Service.PARKING));
                } catch (IOException | InterruptedException | JAXBException ex) {
                    Exceptions.printStackTrace(ex);
                }
            });
        });
    }
}
