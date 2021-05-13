/*
 * Copyright 2021 Patrik Karlström.
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
package org.mapton.fo_geotiff;

import org.mapton.api.MCoordinateFileOpener;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MCoordinateFileOpener.class)
public class GeoTiffCoordinateFileOpener extends MCoordinateFileOpener {

    @Override
    public String getDescription() {
        return "Basic map plot";
    }

    @Override
    public String[] getExtensions() {
        return new String[]{"geotif", "geotiff", "tif", "tiff"};
    }

    @Override
    public String getName() {
        return "Generic Plot";
    }
}
