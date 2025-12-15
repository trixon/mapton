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
import javax.swing.JTabbedPane;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;

/**
 *
 * @author Patrik Karlström
 */
public class ChartAggregate {

    private final ChartBuilderDeltaSplit mBuilderDelta1dSplit;
    private final ChartBuilderDeltaSplit mBuilderDelta2dSplit;
    private final ChartBuilderDeltaSplit mBuilderDelta3dSplit;
    private final JTabbedPane mTabbedPane;

    public ChartAggregate() {
        mBuilderDelta1dSplit = new ChartBuilderDeltaSplit(BDimension._1d);
        mBuilderDelta2dSplit = new ChartBuilderDeltaSplit(BDimension._2d);
        mBuilderDelta3dSplit = new ChartBuilderDeltaSplit(BDimension._3d);

        mTabbedPane = new JTabbedPane(JTabbedPane.BOTTOM);
    }

    public synchronized Callable<JTabbedPane> build(BTopoConvergenceGroup p) {
        if (p == null) {
            return null;
        }

        var prevIndex = mTabbedPane.getSelectedIndex();
        var callable = (Callable<JTabbedPane>) () -> {
            synchronized (mTabbedPane) {
                mTabbedPane.removeAll();
                if (p.ext().getObservationsTimeFiltered().size() > 1) {
                    mTabbedPane.add("Delta 1d", mBuilderDelta1dSplit.build(p).call());
                    mTabbedPane.add("Delta 2d", mBuilderDelta2dSplit.build(p).call());
                    mTabbedPane.add("Delta 3d", mBuilderDelta3dSplit.build(p).call());

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
