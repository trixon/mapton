/*
 * Copyright 2023 Patrik Karlström.
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
            var engine = Mapton.getEngine();
//            if (quickNav.altitude != 0) {
//                engine.panTo(quickNav.altitude, new MLatLon(quickNav.lat, quickNav.lon));
//            } else {
//                engine.panTo(new MLatLon(quickNav.lat, quickNav.lon), quickNav.zoom);
//            }
            engine.setViewHeading(quickNav.heading);
            engine.setViewPitch(quickNav.pitch);
//            engine.setViewRoll(quickNav.roll);
            engine.panTo(quickNav.altitude, new MLatLon(quickNav.lat, quickNav.lon));
        }
    }

    protected void set() {
        var engine = Mapton.getEngine();
        var latLon = engine.getCenter();
        var quickNav = new QuickNav(
                latLon.getLatitude(),
                latLon.getLongitude(),
                engine.getZoom(),
                engine.getViewAltitude(),
                engine.getViewHeading(),
                engine.getViewPitch(),
                engine.getViewRoll()
        );
        mOptions.put(PREFIX + mId, mGson.toJson(quickNav));
    }

    public record QuickNav(double lat, double lon, double zoom, double altitude, double heading, double pitch, double roll) {

    }
}
