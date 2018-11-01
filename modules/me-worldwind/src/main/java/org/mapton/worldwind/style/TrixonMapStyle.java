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
package org.mapton.worldwind.style;

import org.openide.util.lookup.ServiceProvider;
import org.mapton.worldwind.api.MapStyle;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = MapStyle.class)
public class TrixonMapStyle extends MapStyle {

    public TrixonMapStyle() {
        setName("Trixon");
        setSuppliers("Trixon");
        setLayers(new String[]{
            "trixon"
        });
    }
}
