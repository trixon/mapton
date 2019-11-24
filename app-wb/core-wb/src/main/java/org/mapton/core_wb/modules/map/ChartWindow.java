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
package org.mapton.core_wb.modules.map;

import javafx.scene.Node;
import org.mapton.base.ui.ChartView;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.Dict;
import se.trixon.windowsystemfx.Window;
import se.trixon.windowsystemfx.WindowSystemComponent;

@WindowSystemComponent.Description(
        iconBase = "",
        preferredId = "org.mapton.core_wb.modules.map.ChartWindow",
        parentId = "chart",
        position = 1
)
@ServiceProvider(service = Window.class)
public final class ChartWindow extends Window {

    private ChartView mChartView = new ChartView();

    public ChartWindow() {
        setName(Dict.CHART.toString());
    }

    @Override
    public Node getNode() {
        return mChartView;
    }
}
