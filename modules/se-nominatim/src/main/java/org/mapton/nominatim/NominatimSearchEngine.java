/*
 * Copyright 2018 Patrik Karlström.
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
import java.util.ArrayList;
import java.util.List;
import org.mapton.api.MBookmark;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.MSearchEngine;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MSearchEngine.class)
public class NominatimSearchEngine implements MSearchEngine {

    private final Nominatim mNominatim = Nominatim.getInstance();

    public NominatimSearchEngine() {
        init();
    }

    @Override
    public String getName() {
        return "Nominatim";
    }

    @Override
    public ArrayList<MBookmark> getResults(String searchString) {
        ArrayList<MBookmark> bookmarks = new ArrayList<>();
        try {
            final List<Address> addresses = mNominatim.search(searchString);
            for (Address address : addresses) {
                MBookmark bookmark = new MBookmark();
                bookmark.setName(address.getDisplayName());
                bookmark.setLatitude(address.getLatitude());
                bookmark.setLongitude(address.getLongitude());
                BoundingBox bb = address.getBoundingBox();
                MLatLonBox latLonBox = new MLatLonBox(
                        new MLatLon(bb.getSouth(), bb.getWest()),
                        new MLatLon(bb.getNorth(), bb.getEast())
                );
                bookmark.setLatLonBox(latLonBox);

                bookmarks.add(bookmark);
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }

        return bookmarks;
    }

    private void init() {
    }

}
