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
package org.mapton.core.actions.quicknav;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.awt.event.ActionListener;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;

/**
 *
 * @author Patrik Karlström
 */
public abstract class QuickNavAction implements ActionListener {

    private final Gson mGson = new GsonBuilder().create();
    private final String mId;
    private final MOptions mOptions = MOptions.getInstance();
    private static final String PREFIX = "QuickNav_";

    public QuickNavAction(String id) {
        mId = id;
    }

    protected void get() {
        var quickNav = mGson.fromJson(mOptions.get(PREFIX + mId), QuickNav.class);

        if (quickNav != null) {
            Mapton.getEngine().panTo(new MLatLon(quickNav.lat, quickNav.lon), quickNav.zoom);
        }
    }

    protected void set() {
        var latLon = Mapton.getEngine().getCenter();
        var quickNav = new QuickNav(latLon.getLatitude(), latLon.getLongitude(), Mapton.getEngine().getZoom());
        mOptions.put(PREFIX + mId, mGson.toJson(quickNav));
    }

    public class QuickNav {

        private double lat;
        private double lon;
        private double zoom;

        public QuickNav(double lat, double lon, double zoom) {
            this.lat = lat;
            this.lon = lon;
            this.zoom = zoom;
        }
    }
}
