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
package org.mapton.api;

import org.geotools.referencing.CRS;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MCrsManager {

    private final CoordinateReferenceSystem mSourceCrs = DefaultGeographicCRS.WGS84;

    /*
    lista - alla, aktiva, tillgängliga
    spara och återställ aktiva
     */
    private MCrsManager() {
        init();
    }

    public static MCrsManager getInstance() {
        return Holder.INSTANCE;
    }

    private void init() {
        for (var supportedAuthority : CRS.getSupportedAuthorities(false)) {
            for (var supportedCode : CRS.getSupportedCodes(supportedAuthority)) {
                try {
                    var crs = CRS.decode(String.format("%s:%s", supportedAuthority, supportedCode));
                } catch (FactoryException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }

        }
    }

    private static class Holder {

        private static final MCrsManager INSTANCE = new MCrsManager();
    }
}
