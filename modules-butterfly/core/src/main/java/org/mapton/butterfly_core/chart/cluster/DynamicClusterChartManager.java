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
package org.mapton.butterfly_core.chart.cluster;

import java.util.ArrayList;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BClusterChartPoint;

/**
 *
 * @author Patrik Karlström
 */
public class DynamicClusterChartManager extends BaseManager<BClusterChartPoint> {

    private final DynamicClusterMultiChartAggregate mChartAggregate = new DynamicClusterMultiChartAggregate();

    public static DynamicClusterChartManager getInstance() {
        return Holder.INSTANCE;
    }

    private DynamicClusterChartManager() {
        super(BClusterChartPoint.class);
    }

    @Override
    public Object getObjectChart(BClusterChartPoint selectedObject) {
        return mChartAggregate.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
    }

    @Override
    protected void applyTemporalFilter() {
    }

    @Override
    protected void load(ArrayList<BClusterChartPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final DynamicClusterChartManager INSTANCE = new DynamicClusterChartManager();
    }
}
