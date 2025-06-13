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

import java.util.concurrent.Callable;
import java.util.function.Function;
import javax.swing.JTabbedPane;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class ChartAggregate {

    private final ChartBuilderDelta mBuilderDelta = new ChartBuilderDelta(false);
    private final ChartBuilderDelta mBuilderDeltaAvg = new ChartBuilderDelta(true);
    private final ChartBuilderTrend mBuilderTrend1d;
    private final ChartBuilderTrend mBuilderTrend2d;
    private final JTabbedPane mTabbedPane;

    public ChartAggregate() {
        final Function<BXyzPointObservation, Double> func1d = (var o) -> o.ext().getDelta1d();
        final Function<BXyzPointObservation, Double> func2d = (var o) -> o.ext().getDelta2d();
        mBuilderTrend1d = new ChartBuilderTrend(BDimension._1d, func1d);
        mBuilderTrend2d = new ChartBuilderTrend(BDimension._2d, func2d);

        mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    }

    public synchronized Callable<JTabbedPane> build(BTopoControlPoint p) {
        if (p == null) {
            return null;
        }

        var prevIndex = mTabbedPane.getSelectedIndex();
        var callable = (Callable<JTabbedPane>) () -> {
            synchronized (mTabbedPane) {
                mTabbedPane.removeAll();
                mTabbedPane.add("Delta", mBuilderDelta.build(p).call());
                mTabbedPane.add("Delta (avg)", mBuilderDeltaAvg.build(p).call());
                if (p.getDimension() != BDimension._2d) {
                    mTabbedPane.add("Trend 1d", mBuilderTrend1d.build(p).call());
                }
                if (p.getDimension() != BDimension._1d) {
                    mTabbedPane.add("Trend 2d", mBuilderTrend2d.build(p).call());
                }

                if (prevIndex > -1) {
                    mTabbedPane.setSelectedIndex(prevIndex);
                }

                return mTabbedPane;
            }
        };

        return callable;
    }

}
