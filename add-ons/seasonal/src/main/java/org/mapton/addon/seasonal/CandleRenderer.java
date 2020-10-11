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
package org.mapton.addon.seasonal;

import gov.nasa.worldwind.layers.RenderableLayer;
import java.time.LocalDateTime;
import javax.swing.Timer;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.LayerBundleManager;

/**
 *
 * @author Patrik Karlström
 */
public class CandleRenderer extends BaseRenderer {

    public CandleRenderer(RenderableLayer layer) {
        super(layer);
        initAdvent();
    }

    @Override
    public void run() {
    }

    private void initAdvent() {
        LocalDateTime[] startTimes = {
            //LocalDateTime.parse("2020-10-01T00:00:00"),
            LocalDateTime.parse("2020-11-29T00:00:00"),
            LocalDateTime.parse("2020-12-06T00:00:00"),
            LocalDateTime.parse("2020-12-13T00:00:00"),
            LocalDateTime.parse("2020-12-20T00:00:00")
        };

        double[] lats;
        double[] lons;
        double[][] ll = Mapton.getGlobalState().get("org.mapton.addon.seasonal.candle");
        if (ll != null) {
            lats = ll[0];
            lons = ll[1];
        } else {
            var mapHome = MOptions.getInstance().getMapHome();
            var lat = mapHome.getLatitude();
            var lon = mapHome.getLongitude();
            var dist = 0.000675;
            lats = new double[]{lat, lat, lat, lat};
            lons = new double[]{lon - 1.5 * dist, lon - 0.5 * dist, lon + 0.5 * dist, lon + 1.5 * dist};
        }

        var candles = new Candle[4];
        for (int i = 0; i < candles.length; i++) {
            candles[i] = new Candle(
                    new MLatLon(lats[i], lons[i]),
                    startTimes[i],
                    120.0,
                    5.0
            );
        }

        var timer = new Timer(700, event -> {
            mLayer.removeAllRenderables();
            if (MSimpleObjectStorageManager.getInstance().getBoolean(SeasonalSOSB.class, true)) {
                for (var candle : candles) {
                    for (var renderable : candle.getRenderables()) {
                        mLayer.addRenderable(renderable);
                    }
                }
                LayerBundleManager.getInstance().redraw();
            }
        });

        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(startTimes[0].minusDays(3)) && now.isBefore(startTimes[startTimes.length - 1].plusDays(10))) {
            timer.start();
        }
    }
}
