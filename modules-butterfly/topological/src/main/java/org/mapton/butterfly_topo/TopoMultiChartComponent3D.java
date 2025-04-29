/*
 * Copyright 2025 Patrik Karlström.
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

import org.mapton.butterfly_core.api.BMultiChartComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartComponent.class)
public class TopoMultiChartComponent3D extends TopoMultiChartComponent {

    public TopoMultiChartComponent3D() {
        super((BTopoControlPoint p) -> {
            return p.getDimension() == BDimension._3d;
        });
    }

    @Override
    public String getName() {
        return ".Prismor";
    }

}
