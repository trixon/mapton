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
package org.mapton.butterfly_core.api;

import gov.nasa.worldwind.layers.Layer;
import org.mapton.worldwind.api.LayerBundle;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BfLayerBundle extends LayerBundle {

    @Override
    public void setCategory(Layer layer, String category) {
        super.setCategory(layer, "%s/%s".formatted("Butterfly", category));
    }

}
