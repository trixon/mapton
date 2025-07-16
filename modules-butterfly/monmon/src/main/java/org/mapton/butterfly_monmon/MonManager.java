/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_monmon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import javafx.collections.ListChangeListener;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MonManager extends BaseManager<BMonmon> {

    private final MonPropertiesBuilder mPropertiesBuilder = new MonPropertiesBuilder();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public static MonManager getInstance() {
        return Holder.INSTANCE;
    }

    private MonManager() {
        super(BMonmon.class);
        initListeners();
    }

    @Override
    public Object getObjectProperties(BMonmon selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getMonmons());
            initObjectToItemMap();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BMonmon> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initListeners() {
        mTopoManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            updateStats();
        });
    }

    private void updateStats() {
        var now = LocalDateTime.now();
        for (var mon : getAllItems()) {
            mon.setStationPoint(mTopoManager.getItemForKey(mon.getStationName()));
            var list14 = mon.getControlPoint().ext().getObservationsAllRaw().stream()
                    .filter(o -> o.getDate().isAfter(now.minusDays(14))).toList();

            mon.getMeasCount()[14] = list14.size();
            mon.getMeasCount()[7] = (int) list14.stream().filter(o -> o.getDate().isAfter(now.minusDays(7))).count();
            mon.getMeasCount()[1] = (int) list14.stream().filter(o -> o.getDate().isAfter(now.minusDays(1))).count();
        }
    }

    private static class Holder {

        private static final MonManager INSTANCE = new MonManager();
    }
}
