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
import java.io.IOException;
import org.mapton.api.MLatLon;
import org.mapton.api.MWhatsHereEngine;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MWhatsHereEngine.class)
public class NominatimWhatsHereEngine implements MWhatsHereEngine {

    private final Nominatim mNominatim = Nominatim.getInstance();

    public NominatimWhatsHereEngine() {
    }

    @Override
    public String getName() {
        return "Nominatim";
    }

    @Override
    public String getResult(MLatLon latLon, int zoom) {
        Address address;
        try {
            address = mNominatim.getAddress(latLon, zoom);
            return address.getDisplayName();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return "";
        }
    }
}
