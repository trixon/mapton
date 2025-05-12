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
package org.mapton.butterfly_topo.chart;

import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_format.types.BDimension;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartPart.class)
public class MultiChartPart1D extends MultiChartPart {

    public MultiChartPart1D() {
        super(BDimension._1d);
    }

    @Override
    public String getName() {
        return "1D";
    }

}
