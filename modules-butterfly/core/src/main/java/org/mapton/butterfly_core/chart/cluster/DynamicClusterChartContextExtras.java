/*
 * Copyright 2026 Patrik Karlström.
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

import java.time.LocalDateTime;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import org.mapton.api.MContextMenuItem;
import org.mapton.api.MKey;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.rock.BRockBlast;
import org.openide.awt.Actions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@NbBundle.Messages({
    "CTL_DynamicClusterChartMenu=&Dynamic cluster chart"
})
@ServiceProvider(service = MContextMenuItem.class)
public class DynamicClusterChartContextExtras extends MContextMenuItem {

    private final DynamicClusterMultiChartAggregate mChartAggregate = new DynamicClusterMultiChartAggregate();

    public DynamicClusterChartContextExtras() {
    }

    @Override
    public EventHandler<ActionEvent> getAction() {
        return event -> {
            var p = new BRockBlast();
            p.setName("Klusterdiagram");
            p.setDateLatest(LocalDateTime.now());
            p.ext().setDateFirst(LocalDateTime.now());
            p.setButterfly(ButterflyManager.getInstance().getButterfly());
            p.setLat(getLatitude());
            p.setLon(getLongitude());
            Mapton.getGlobalState().put(MKey.CHART, mChartAggregate.build(p));
        };
    }

    @Override
    public String getName() {
        return Actions.cutAmpersand(Bundle.CTL_DynamicClusterChartMenu());
    }

    @Override
    public ContextType getType() {
        return ContextType.EXTRAS;
    }

}
