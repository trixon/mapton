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
package org.mapton.core;

import javafx.geometry.Point2D;
import org.apache.commons.lang3.math.NumberUtils;
import org.mapton.api.MCooTrans;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.mapton.core.cootrans.Wgs84DMS;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

public class CoordinateQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        var latLon = parseDecimal(request.getText());
        if (latLon != null) {
            if (!response.addResult(() -> {
                Mapton.getEngine().panTo(latLon);
            }, request.getText())) {
            }
        }
    }

    private MLatLon parseDecimal(String searchString) {
        MLatLon latLon = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");

        if (coordinate.length == 2) {
            try {
                final Double lat = NumberUtils.createDouble(coordinate[0]);
                final Double lon = NumberUtils.createDouble(coordinate[1]);
                Wgs84DMS dms = new Wgs84DMS();
                if (dms.isWithinWgs84Bounds(lon, lat)) {
                    latLon = new MLatLon(lat, lon);
                } else {
                    MCooTrans cooTrans = MOptions.getInstance().getMapCooTrans();
                    if (cooTrans.isWithinProjectedBounds(lat, lon)) {
                        Point2D p = cooTrans.toWgs84(lat, lon);
                        latLon = new MLatLon(p.getY(), p.getX());
                    }
                }
            } catch (Exception e) {
                // nvm
            }
        }

        return latLon;
    }
}
