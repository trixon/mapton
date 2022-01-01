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
package org.mapton.poi_trv_ti.updater;

import org.mapton.api.MPoiManager;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MUpdater;
import org.mapton.poi_trv_ti.AutoUpdateProvider;
import org.mapton.poi_trv_ti.TrafficInformationManager;
import se.trixon.trv_traffic_information.TrafficInformation;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseUpdater extends MUpdater.ByFile {

    protected final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    protected final TrafficInformation mTrafficInformation = mManager.getTrafficInformation();

    public BaseUpdater() {
        setAutoUpdate(true);
        setCategory("Trv Traffic Information");
    }

    @Override
    public boolean isAutoUpdateEnabled() {
        return MSimpleObjectStorageManager.getInstance().getBoolean(AutoUpdateProvider.class, false);
    }

    public void refreshPoiManager() {
        MPoiManager.getInstance().refresh();
    }

}
