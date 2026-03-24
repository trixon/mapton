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
package org.mapton.butterfly_meteo.chart;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import org.mapton.butterfly_format.types.BMeteoPoint;

/**
 *
 * @author Patrik Karlström
 */
public class ChartBuilderDeltaSplit {

    private final ChartBuilderDelta mCompleteChartBuilder = new ChartBuilderDelta(false, null);
    private final ChartBuilderDelta mLatestChartBuilder = new ChartBuilderDelta(false, 7);

    public ChartBuilderDeltaSplit() {
    }

    public synchronized Callable<JPanel> build(BMeteoPoint p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<JPanel>) () -> {
            mLatestChartBuilder.build(p);
            var panel = new JPanel(new GridBagLayout());
            var gbc = new GridBagConstraints();
            var latestChartPanel = mLatestChartBuilder.build(p).call();
            var completeChartPanel = mCompleteChartBuilder.build(p).call();

            gbc.fill = GridBagConstraints.BOTH;
            gbc.weighty = 1.0;
            gbc.gridy = 0;

            gbc.weightx = 0.7;
            gbc.gridx = 0;
            panel.add(completeChartPanel, gbc);

            gbc.weightx = 0.3;
            gbc.gridx = 1;
            panel.add(latestChartPanel, gbc);

            return panel;
        };

        return callable;
    }
}
