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
package org.mapton.butterfly_rock_convergence.chart;

import java.util.concurrent.Callable;
import javax.swing.JPanel;
import org.mapton.butterfly_core.api.BChartSplit;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.rock.BRockConvergence;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDeltaSplit extends BChartSplit {

    private final ChartBuilderDelta mCompleteChartBuilder;
    private final ChartBuilderDelta mLatestChartBuilder;

    public ChartBuilderDeltaSplit(BDimension dimension) {
        mCompleteChartBuilder = new ChartBuilderDelta(null, dimension);
        mLatestChartBuilder = new ChartBuilderDelta(7, dimension);
    }

    public synchronized Callable<JPanel> build(BRockConvergence p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<JPanel>) () -> {
            return createSplitPanel(
                    mLatestChartBuilder.build(p).call(),
                    mCompleteChartBuilder.build(p).call()
            );
        };

        return callable;
    }
}
