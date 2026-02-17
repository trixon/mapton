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
package org.mapton.butterfly_structural.tilt.chart;

import java.util.concurrent.Callable;
import java.util.function.Function;
import javax.swing.JTabbedPane;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;

/**
 *
 * @author Patrik Karlström
 */
public class ChartAggregate {

    private final ChartBuilderDelta mBuilderDeltaAvg = new ChartBuilderDelta(true, null);
    private final ChartBuilderDeltaSplit mBuilderDeltaSplit = new ChartBuilderDeltaSplit();
    private final ChartBuilderTrend mBuilderTrendR;
    private final ChartBuilderTrend mBuilderTrendX;
    private final ChartBuilderTrend mBuilderTrendY;
    private final JTabbedPane mTabbedPane;

    public ChartAggregate() {
        final Function<BXyzPointObservation, Double> funcX = (var o) -> o.ext().getDeltaX();
        final Function<BXyzPointObservation, Double> funcY = (var o) -> o.ext().getDeltaY();
        final Function<BXyzPointObservation, Double> funcR = (var o) -> o.ext().getDelta2d();
        mBuilderTrendX = new ChartBuilderTrend("T", funcX);
        mBuilderTrendY = new ChartBuilderTrend("L", funcY);
        mBuilderTrendR = new ChartBuilderTrend("R", funcR);

        mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    }

    public synchronized Callable<JTabbedPane> build(BStructuralTiltPoint p) {
        if (p == null) {
            return null;
        }

        var prevIndex = mTabbedPane.getSelectedIndex();
        var callable = (Callable<JTabbedPane>) () -> {
            synchronized (mTabbedPane) {
                mTabbedPane.removeAll();
                if (p.ext().getObservationsTimeFiltered().size() > 1) {
                    mTabbedPane.add("Delta", mBuilderDeltaSplit.build(p).call());
                    mTabbedPane.add("Delta (avg)", mBuilderDeltaAvg.build(p).call());
                    mTabbedPane.add("Trend T", mBuilderTrendX.build(p).call());
                    mTabbedPane.add("Trend L", mBuilderTrendY.build(p).call());
                    mTabbedPane.add("Trend R", mBuilderTrendR.build(p).call());

                    if (prevIndex > -1) {
                        mTabbedPane.setSelectedIndex(prevIndex);
                    }
                }

                return mTabbedPane;
            }
        };

        return callable;
    }

}
