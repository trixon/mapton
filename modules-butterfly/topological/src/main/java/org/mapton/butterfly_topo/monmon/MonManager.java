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
package org.mapton.butterfly_topo.monmon;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoMonmon;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MonManager extends BaseManager<BTopoMonmon> {

    private final MonLayerOptions mMonLayerOptions = MonLayerOptions.getInstance();
    private final MonPropertiesBuilder mPropertiesBuilder = new MonPropertiesBuilder();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public static MonManager getInstance() {
        return Holder.INSTANCE;
    }

    private MonManager() {
        super(BTopoMonmon.class);
        initListeners();
        Mapton.getGlobalState().addListener(gsce -> {
            load2(gsce.getValue());
        }, TopoManager.KEY_TOPO_POINTS_LOADED);
    }

    @Override
    public Object getObjectProperties(BTopoMonmon selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
    }

    @Override
    protected void applyTemporalFilter() {
        var timeFilteredItems = new ArrayList<BTopoMonmon>();
        p:
        for (var p : getFilteredItems()) {
            keepLoadingProgressAlive();
            var cp = p.getControlPoint();
            if (cp.getDateLatest() == null || cp.ext().getObservationsAllRaw().isEmpty()) {
                timeFilteredItems.add(p);
            } else {
                for (var o : cp.ext().getObservationsAllRaw()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(p);
                        continue p;
                    }
                }
            }
        }

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BTopoMonmon> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initListeners() {
        mTopoManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            updateStats();
        });
    }

    private void load2(Butterfly butterfly) {
        try {
            var monmons = butterfly.topo().getMonmons().stream()
                    .peek(p -> {
                        p.setVisible(true);
                        p.setButterfly(butterfly);
                        p.setDateLatest(p.ext().getDateLatest());
                        if (p.isChild()) {
                            p.setStationPoint(mTopoManager.getItemForKey(p.getStationName()));
                        }
                    })
                    .filter(m -> {
                        return m != null && ObjectUtils.allNotNull(
                                m.getZeroX(),
                                m.getZeroY(),
                                m.getZeroZ()
                        );
                    })
                    .collect(Collectors.toCollection(ArrayList<BTopoMonmon>::new));

            initAllItems(monmons);
            initObjectToItemMap();

            var dates = new TreeSet<LocalDateTime>();
            getAllItems().stream().forEachOrdered(p -> {
//                dates.addAll(p.ext().getObservationsAllRaw().stream().map(o -> o.getDate()).toList());
                dates.add(p.ext().getObservationRawFirstDate().atStartOfDay());
                dates.add(p.ext().getObservationRawLastDate().atStartOfDay());
            });

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
                boolean layerBundleEnabled = isLayerBundleEnabled();
                updateTemporal(!layerBundleEnabled);
                updateTemporal(layerBundleEnabled);
            }

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        var sortedStations = getAllItems().stream()
                .filter(m -> m.isParent())
                .sorted((o1, o2) -> o1.getName().compareTo(o2.getName()))
                .toList();

        for (int i = 0; i < sortedStations.size(); i++) {
            var p = sortedStations.get(i);
            mMonLayerOptions.putAttributes(p.getName(), MonAttributeManager.getInstance().getStationConnectorAttribute(i));
        }
    }

    private void updateStats() {
        var now = LocalDateTime.now();
        for (var mon : getAllItems()) {
            var list14 = mon.ext().getObservationsAllRaw().stream()
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
