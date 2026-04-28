/*
 * Copyright 2026 Patrik Karlström.
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

import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.butterfly_core.chart.cluster.DynamicClusterMaxDistanceSosi;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BMultiChartPartCluster extends BMultiChartPart {

    @Override
    public String getCategory() {
        return BKey.CLUSTER_CHART;
    }

    public int getDefaultDistance() {
        var distance = MSimpleObjectStorageManager.getInstance().getInteger(DynamicClusterMaxDistanceSosi.class, DynamicClusterMaxDistanceSosi.DEFAULT_VALUE);

        return distance;
    }
}
