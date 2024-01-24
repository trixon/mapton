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
package org.mapton.butterfly_topo.tilt;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.TreeMap;
import javafx.collections.ListChangeListener;
import javafx.geometry.Point2D;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoTiltH;
import org.mapton.butterfly_topo.api.TopoManager;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHManager extends BaseManager<BTopoTiltH> {

    private final TiltHPropertiesBuilder mPropertiesBuilder = new TiltHPropertiesBuilder();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public static TiltHManager getInstance() {
        return Holder.INSTANCE;
    }

    private TiltHManager() {
        super(BTopoTiltH.class);
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            load();
        });
    }

    @Override
    public Object getObjectProperties(BTopoTiltH selectedObject) {
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
    protected void load(ArrayList<BTopoTiltH> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void load() {
        var thread = new Thread(() -> {
            var pointToPoints = new TreeMap<String, HashSet<String>>();
            var sourcePoints = mTopoManager.getTimeFilteredItems().stream()
                    .filter(p -> p.getDimension() != BDimension._2d)
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
                    if (p1 != p2 && distance > 0.05 && distance < 20.0) {
                        if (!pointToPoints.computeIfAbsent(n2, k -> new HashSet<>()).contains(n1)) {//Skip A-B, B-A
                            pointToPoints.computeIfAbsent(n1, k -> new HashSet<>()).add(n2);
                        }
                    }
                }
            }

            var tiltPairs = new ArrayList<BTopoTiltH>();
            for (var entry : pointToPoints.entrySet()) {
                var n1 = entry.getKey();
                var p1 = mTopoManager.getItemForKey(n1);
                for (var n2 : entry.getValue()) {
                    var p2 = mTopoManager.getItemForKey(n2);
                    var pair = new BTopoTiltH(p1, p2);
                    if (pair.getCommonObservations().size() > 1 && pair.getTilt() > 0.001) {
                        tiltPairs.add(pair);
                    }
                }
            }

            Collections.sort(tiltPairs, (o1, o2) -> o2.getTilt().compareTo(o1.getTilt()));

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

        private static final TiltHManager INSTANCE = new TiltHManager();
    }
}
