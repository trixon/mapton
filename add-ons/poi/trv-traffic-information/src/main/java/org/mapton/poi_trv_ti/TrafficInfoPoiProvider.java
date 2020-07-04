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

import java.util.ArrayList;
import java.util.ResourceBundle;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiProvider;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MPoiProvider.class)
public class TrafficInfoPoiProvider implements MPoiProvider {

    private ResourceBundle mBundle = NbBundle.getBundle(TrafficInfoPoiProvider.class);

    public TrafficInfoPoiProvider() {
    }

    @Override
    public String getName() {
        return mBundle.getString("name");
    }

    @Override
    public ArrayList<MPoi> getPois() {
        ArrayList<MPoi> pois = new ArrayList<>();

        MPoi poi = new MPoi();
        poi.setDescription("desc");
        poi.setCategory("cat");
        poi.setColor("00ff00");
        poi.setDisplayMarker(true);
        poi.setLatitude(57.0);
        poi.setLongitude(12.0);
        poi.setName("dummy");
        poi.setZoom(0.9);

        pois.add(poi);

        return pois;
    }
}
