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

import java.util.Comparator;
import java.util.concurrent.Callable;
import javax.swing.JTabbedPane;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public class MultiChartAggregate {

    private final JTabbedPane mTabbedPane;

    public MultiChartAggregate() {
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
                Lookup.getDefault().lookupAll(BMultiChartPart.class).stream()
                        .filter(c -> StringUtils.equalsIgnoreCase(c.getCategory(), BTopoControlPoint.class.getName()))
                        .sorted(Comparator.comparing(BMultiChartPart::getName))
                        .forEachOrdered(multiChartComponent -> {
                            try {
                                var chartBuilder = new MultiChartBuilder(
                                        multiChartComponent.getName(),
                                        multiChartComponent.getAxisLabel(),
                                        multiChartComponent.getDecimalPattern()
                                );

                                var chartPanel = chartBuilder.build(p, multiChartComponent).call();
                                var tabTitle = "%s (%d)".formatted(multiChartComponent.getName(), chartBuilder.getPointSize());
                                mTabbedPane.add(tabTitle, chartPanel);
                            } catch (Exception ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        });

                if (prevIndex > -1) {
                    mTabbedPane.setSelectedIndex(prevIndex);
                }

                return mTabbedPane;
            }
        };

        return callable;
    }

}
