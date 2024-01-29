/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.pair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Pair3Manager extends PairManagerBase {

    public static final Double MIN_RADIAL_DISTANCE = 0.0;
    public static final Double MAX_RADIAL_DISTANCE = 10.0;
    private final Pair3PropertiesBuilder mPropertiesBuilder = new Pair3PropertiesBuilder();

    public static Pair3Manager getInstance() {
        return Holder.INSTANCE;
    }

    private Pair3Manager() {
        super(BTopoPointPair.class);
    }

    @Override
    public Object getObjectProperties(BTopoPointPair selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
    protected void applyTemporalFilter() {
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoPointPair> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void load() {
        var thread = new Thread(() -> {
            var pointToPoints = new TreeMap<String, HashSet<String>>();
            var sourcePoints = mTopoManager.getTimeFilteredItems().stream()
                    .filter(p -> p.getDimension() == BDimension._3d)
                    .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                    .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                    .toList();

            for (var p1 : sourcePoints) {
                if (ObjectUtils.anyNull(p1.getZeroX(), p1.getZeroY())) {
                    continue;
                }
                var point = new Point2D(p1.getZeroX(), p1.getZeroY());
                var n1 = p1.getName();
                for (var p2 : sourcePoints) {
                    if (ObjectUtils.anyNull(p2.getZeroX(), p2.getZeroY())) {
                        continue;
                    }
                    var n2 = p2.getName();
                    double distance = point.distance(p2.getZeroX(), p2.getZeroY());
                    if (p1 != p2 && distance > MIN_RADIAL_DISTANCE && distance < MAX_RADIAL_DISTANCE) {
                        if (!pointToPoints.computeIfAbsent(n2, k -> new HashSet<>()).contains(n1)) {//Skip A-B, B-A
                            pointToPoints.computeIfAbsent(n1, k -> new HashSet<>()).add(n2);
                        }
                    }
                }
            }

            var tiltPairs = new ArrayList<BTopoPointPair>();
            for (var entry : pointToPoints.entrySet()) {
                var n1 = entry.getKey();
                var p1 = mTopoManager.getItemForKey(n1);
                for (var n2 : entry.getValue()) {
                    var p2 = mTopoManager.getItemForKey(n2);
                    var pair = new BTopoPointPair(p1, p2);
                    if (pair.getCommonObservations().size() > 1
                            //                            && ( Math.abs(pair.getZQuota()) > 0.00001)) {
                            && (Math.abs(pair.getRQuota()) > 0.00001 || Math.abs(pair.getZQuota()) > 0.00001)) {
                        tiltPairs.add(pair);
                    }
                }
            }

            Collections.sort(tiltPairs, (o1, o2) -> Double.valueOf(Math.abs(o2.getRQuota())).compareTo(Math.abs(o1.getRQuota())));

            tiltPairs.forEach(t -> {
                var first = new MLatLon(t.getP1().getLat(), t.getP1().getLon());
                var second = new MLatLon(t.getP2().getLat(), t.getP2().getLon());
                var d = first.distance(second);
                var b = first.getBearing(second);
                var mid = first.getDestinationPoint(b, d * .5);
                t.setLat(mid.getLatitude());
                t.setLon(mid.getLongitude());
            });

            FxHelper.runLater(() -> {
                getAllItems().setAll(tiltPairs);
                getFilteredItems().setAll(tiltPairs);
                getTimeFilteredItems().setAll(tiltPairs);
            });
        });

        thread.start();
    }

    private static class Holder {

        private static final Pair3Manager INSTANCE = new Pair3Manager();
    }
}
