/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.ww_eox.style;

import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import org.mapton.worldwind.api.MapStyle;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapStyle.class)
public class TerrainLightMapStyle extends MapStyle {

    public TerrainLightMapStyle() {
        setName(String.format("%s (%s)", Dict.MAP_TYPE_TERRAIN.toString(), Dict.LIGHT.toString().toLowerCase()));
        setSuppliers("EOX & ESA");
        setLayers(new String[]{
            "Terrain Light background layer by EOX"
        });
    }
}
