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
package org.mapton.nominatim;

import fr.dudie.nominatim.model.Address;
import fr.dudie.nominatim.model.BoundingBox;
import java.io.IOException;
import java.util.List;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.Mapton;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;

/**
 *
 * @author Patrik Karlström
 */
public class NominatimQuickSearchProvider implements SearchProvider {

    private final Nominatim mNominatim = Nominatim.getInstance();

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        try {
            final List<Address> addresses = mNominatim.search(request.getText());
            for (Address address : addresses) {
                if (!response.addResult(() -> {
                    BoundingBox bb = address.getBoundingBox();
                    MLatLonBox latLonBox = new MLatLonBox(
                            new MLatLon(bb.getSouth(), bb.getWest()),
                            new MLatLon(bb.getNorth(), bb.getEast())
                    );
                    Mapton.getEngine().fitToBounds(latLonBox);
                }, address.getDisplayName())) {
                    break;
                }
            }
        } catch (IOException ex) {
            System.err.println(ex.getMessage());
        }
    }
}
