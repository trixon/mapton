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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point3D;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePairObservation;
import org.mapton.butterfly_topo_convergence.group.ConvergenceGroupManager;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairManager extends BaseManager<BTopoConvergencePair> {

    private final ConvergenceGroupManager mGroupManager = ConvergenceGroupManager.getInstance();
//    private final ConvergencePropertiesBuilder mPropertiesBuilder = new ConvergencePropertiesBuilder();

    public static ConvergencePairManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergencePairManager() {
        super(BTopoConvergencePair.class);

        initListeners();
    }

    @Override
    public Object getObjectProperties(BTopoConvergencePair selectedObject) {
//        return mPropertiesBuilder.build(selectedObject);
        return selectedObject;
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
        getTimeFilteredItems().setAll(getFilteredItems());
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
        var pairs = new ArrayList<BTopoConvergencePair>();
        for (var group : mGroupManager.getTimeFilteredItems()) {
            var existingPairs = new HashSet<String>();
            for (var p1 : group.ext2().getControlPoints()) {
                for (var p2 : group.ext2().getControlPoints()) {
                    if (p1 == p2 || existingPairs.contains("%s-%s".formatted(p2.getName(), p1.getName()))) {
                        continue;
                    }

                    existingPairs.add("%s-%s".formatted(p1.getName(), p2.getName()));

                    var pair = new BTopoConvergencePair(group, p1, p2);
                    var first = new MLatLon(p1.getLat(), p1.getLon());
                    var second = new MLatLon(p2.getLat(), p2.getLon());
                    var d = first.distance(second);
                    var b = first.getBearing(second);
                    var mid = first.getDestinationPoint(b, d * .5);
                    pair.setLat(mid.getLatitude());
                    pair.setLon(mid.getLongitude());
                    pairs.add(pair);
                }
            }
        }

        for (var pair : pairs) {
            //TODO Calculate delta time series

            var dateToObservation1 = new HashMap<LocalDateTime, BTopoControlPointObservation>();
            var dateToObservation2 = new HashMap<LocalDateTime, BTopoControlPointObservation>();
            pair.getP1().ext().getObservationsAllCalculated().forEach(o -> dateToObservation1.put(o.getDate(), o));
            pair.getP2().ext().getObservationsAllCalculated().forEach(o -> dateToObservation2.put(o.getDate(), o));

            for (var entry : dateToObservation1.entrySet()) {
                var date = entry.getKey();
                var o1 = entry.getValue();
                var o2 = dateToObservation2.get(date);
                if (o2 != null) {
                    var p1 = new Point3D(o1.getMeasuredX(), o1.getMeasuredY(), o1.getMeasuredZ());
                    var p2 = new Point3D(o2.getMeasuredX(), o2.getMeasuredY(), o2.getMeasuredZ());

                    var pairObservation = new BTopoConvergencePairObservation(date, p1, p2);
                }
            }
        }

        initAllItems(pairs);
    }

    private static class Holder {

        private static final ConvergencePairManager INSTANCE = new ConvergencePairManager();
    }
}
