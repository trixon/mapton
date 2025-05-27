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
package org.mapton.butterfly_geo_extensometer.chart;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.Callable;
import javax.swing.JPanel;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoChartBuilderSplit {

    private final ExtensoChartBuilder mCompleteChartBuilder = new ExtensoChartBuilder(null);
    private final ExtensoChartBuilder mLatestChartBuilder = new ExtensoChartBuilder(7);

    public ExtensoChartBuilderSplit() {
    }

    public synchronized Callable<JPanel> build(BGeoExtensometer p) {
        if (p == null) {
            return null;
        }

        var callable = (Callable<JPanel>) () -> {
            mLatestChartBuilder.build(p);
            var panel = new JPanel(new GridBagLayout());
            var gbc = new GridBagConstraints();

            gbc.fill = GridBagConstraints.BOTH;
            gbc.weightx = 0.6;
            gbc.weighty = 1.0;
            gbc.gridx = 0;
            gbc.gridy = 0;
            panel.add(mCompleteChartBuilder.build(p).call(), gbc);

            gbc.weightx = 0.4;
            gbc.gridx = 1;
            panel.add(mLatestChartBuilder.build(p).call(), gbc);

            return panel;
        };

        return callable;
    }
}
