/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo;

import gov.nasa.worldwind.layers.RenderableLayer;
import org.mapton.butterfly_core.api.BfLayerBundle;
import org.mapton.butterfly_topo.api.TopoManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TopoBaseLayerBundle extends BfLayerBundle {

    protected final RenderableLayer mLabelLayer = new RenderableLayer();
    protected final RenderableLayer mLayer = new RenderableLayer();
    protected final TopoManager mManager = TopoManager.getInstance();
    protected final RenderableLayer mPinLayer = new RenderableLayer();
    protected final RenderableLayer mSymbolLayer = new RenderableLayer();

    public TopoBaseLayerBundle() {
    }

}
