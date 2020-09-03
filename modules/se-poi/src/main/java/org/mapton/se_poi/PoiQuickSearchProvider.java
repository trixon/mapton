/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.mapton.se_poi;

import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MLatLon;
import org.mapton.api.MPoi;
import org.mapton.api.MPoiManager;
import org.mapton.api.Mapton;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

public class PoiQuickSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (MPoi poi : MPoiManager.getInstance().getAllItems()) {
            if (StringUtils.containsIgnoreCase(String.join("/", poi.getCategory(), poi.getName(), poi.getProvider()), request.getText())) {
                if (!response.addResult(() -> {
                    Mapton.getEngine().panTo(new MLatLon(poi.getLatitude(), poi.getLongitude()), poi.getZoom());
                }, poi.getName())) {
                    break;
                }
            }

        }
    }
}
