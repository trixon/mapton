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
package org.mapton.butterfly_topo_convergence.pair;

import gov.nasa.worldwind.render.Ellipsoid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import org.mapton.api.MOptions;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePairObservation;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo_convergence.ConvergenceAttributeManager;
import org.mapton.butterfly_topo_convergence.api.ConvergenceGroupManager;
import org.mapton.butterfly_topo_convergence.pair.chart.ConvergencePairChartBuilder;
import org.mapton.worldwind.api.WWHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairManager extends BaseManager<BTopoConvergencePair> {

    private final ConvergencePairChartBuilder mChartBuilder = new ConvergencePairChartBuilder();
    private final ConvergenceGroupManager mGroupManager = ConvergenceGroupManager.getInstance();
    private final ConvergencePairPropertiesBuilder mPropertiesBuilder = new ConvergencePairPropertiesBuilder();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public static ConvergencePairManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergencePairManager() {
        super(BTopoConvergencePair.class);

        initListeners();
    }

    @Override
    public Object getMapIndicator(BTopoConvergencePair pair) {
        if (pair == null) {
            return null;
        }

        var offset = ConvergenceGroupManager.getInstance().getOffset();
        var pos1 = PairHelper.getPosition(pair.getP1(), offset);
        var pos2 = PairHelper.getPosition(pair.getP2(), offset);
        var radius = PairHelper.NODE_SIZE * 1.5;
        var e1 = new Ellipsoid(pos1, radius, radius, radius);
        var e2 = new Ellipsoid(pos2, radius, radius, radius);
        var attrs = ConvergenceAttributeManager.getInstance().getIndicatorAttributes();

        final String nameKey = "nodeName";

        e1.setValue(nameKey, pair.getP1().getName());
        e2.setValue(nameKey, pair.getP2().getName());

        List.of(e1, e2).forEach(e -> {
            var leftClickRunnable = (Runnable) () -> {
                Mapton.getGlobalState().put(ConvergencePairChartBuilder.class.getName() + "node", e.getValue(nameKey));
            };
            e.setValue(WWHelper.KEY_RUNNABLE_LEFT_CLICK, leftClickRunnable);
            e.setAttributes(attrs);
        });

        return new Ellipsoid[]{e1, e2};
    }

    @Override
    public Object getObjectChart(BTopoConvergencePair selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BTopoConvergencePair selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        //Do nothing here, use listener for convergence groups...
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoConvergencePair> items) {
    }

    private void initListeners() {
        mGroupManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoConvergenceGroup> c) -> {
            load();
        });
    }

    private void load() {
        var cooTrans = MOptions.getInstance().getMapCooTrans();
        var pairs = new ArrayList<BTopoConvergencePair>();
        var offset = mGroupManager.getOffset();

        for (var group : mGroupManager.getTimeFilteredItems()) {
            var existingPairs = new HashSet<String>();
            for (var p1 : group.ext2().getControlPoints()) {
                for (var p2 : group.ext2().getControlPoints()) {
                    if (p1 == p2 || existingPairs.contains("%s-%s".formatted(p2.getName(), p1.getName()))) {
                        continue;
                    }

                    existingPairs.add("%s-%s".formatted(p1.getName(), p2.getName()));
                    try {
                        var pair = new BTopoConvergencePair(group, p1, p2, offset);
                        var point = cooTrans.toWgs84(pair.getZeroY(), pair.getZeroX());
                        pair.setLat(point.getY());
                        pair.setLon(point.getX());

                        pairs.add(pair);
                    } catch (NullPointerException e) {
                        System.err.println("NPE  ConvergencePairManager 1 " + p1.getName() + " " + p2.getName());
                    }
                }
            }
        }

        for (var pair : pairs) {
            var observations = new ArrayList<BTopoConvergencePairObservation>();
            var dateToObservation1 = new HashMap<LocalDateTime, BXyzPointObservation>();
            var dateToObservation2 = new HashMap<LocalDateTime, BXyzPointObservation>();

            var pp1 = mTopoManager.getAllItemsMap().get(pair.getP1().getName());
            var pp2 = mTopoManager.getAllItemsMap().get(pair.getP2().getName());

            pp1.ext().getObservationsTimeFiltered().forEach(o -> dateToObservation1.put(o.getDate(), o));
            pp2.ext().getObservationsTimeFiltered().forEach(o -> dateToObservation2.put(o.getDate(), o));

            for (var entry : dateToObservation1.entrySet()) {
                try {
                    var date = entry.getKey();
                    var o1 = entry.getValue();
                    var o2 = dateToObservation2.get(date);
                    if (o2 != null) {
                        var p1 = new Point3D(o1.getMeasuredX(), o1.getMeasuredY(), o1.getMeasuredZ());
                        var p2 = new Point3D(o2.getMeasuredX(), o2.getMeasuredY(), o2.getMeasuredZ());

                        var pairObservation = new BTopoConvergencePairObservation(pair, date, p1, p2);
                        observations.add(pairObservation);
                    }
                } catch (NullPointerException e) {
                    System.err.println("NPE  ConvergencePairManager 2 " + entry.getKey());
                }
            }

            observations.sort((o1, o2) -> o1.getDate().compareTo(o2.getDate()));
            pair.setObservations(observations);
        }

        pairs.sort((o1, o2) -> {
            if (o1.getObservations().isEmpty() || o2.getObservations().isEmpty()) {
                return 0;
            } else {
                var d1 = o1.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();
                var d2 = o2.getObservations().getLast().getDeltaDeltaDistanceComparedToFirst();

                d1 = Math.abs(d1);
                d2 = Math.abs(d2);

                return Double.compare(d2, d1);
            }
        });

        initAllItems(pairs);
    }

    private static class Holder {

        private static final ConvergencePairManager INSTANCE = new ConvergencePairManager();
    }
}
