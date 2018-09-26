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
package se.trixon.mapton.ww_eox.style;

import org.openide.util.lookup.ServiceProvider;
import se.trixon.mapton.worldwind.api.MapStyle;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapStyle.class)
public class OpenStreetMapStyle extends MapStyle {

    public OpenStreetMapStyle() {
        setName("Open Street Map");
        setSuppliers("EOX & OpenStreepMap");
        setLayers(new String[]{
            "OpenStreetMap background layer by EOX"
        });
    }
}
