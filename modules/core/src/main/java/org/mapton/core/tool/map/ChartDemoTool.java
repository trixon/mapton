/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.core.tool.map;

import org.controlsfx.control.action.Action;
import org.mapton.api.MChartLine;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.openide.util.Exceptions;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxActionSwing;
import org.mapton.api.MToolMap;

/**
 *
 * @author Patrik Karlström
 */
public class ChartDemoTool implements MToolMap {

    @Override
    public Action getAction() {
        FxActionSwing action = new FxActionSwing(Dict.CHART.toString() + " Demo", () -> {
            new Thread(() -> {
                Mapton.getGlobalState().put(MKey.CHART_WAIT, null);

                MChartLine chartLine = new MChartLine(getAction().getText(), "ratio", "sin", "cos", "sin×cos");
                chartLine.setPlotSymbols(false);

                for (int g = 0; g < 401; g = g + 10) {
                    chartLine.getColumns().add(String.valueOf(g));
                    double phi = g * (Math.PI / 200);
                    chartLine.getValues()[0].add(Math.sin(phi));
                    chartLine.getValues()[1].add(Math.cos(phi));
                    chartLine.getValues()[2].add(Math.sin(phi) * Math.cos(phi));
                }

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }

                Mapton.getGlobalState().put(MKey.CHART, chartLine);
            }).start();
        });

        return action;
    }

    @Override
    public String getParent() {
        return Dict.SYSTEM.toString();
    }
}
