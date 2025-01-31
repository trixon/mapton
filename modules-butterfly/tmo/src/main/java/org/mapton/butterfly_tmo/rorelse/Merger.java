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
package org.mapton.butterfly_tmo.rorelse;

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_tmo.api.RorelseManager;
import org.mapton.butterfly_topo.api.TopoManager;

/**
 *
 * @author Patrik Karlström
 */
public class Merger {

    private final TopoManager mTopoManager = TopoManager.getInstance();
    private final RorelseManager mRorelseManager = RorelseManager.getInstance();
    private final ButterflyManager mButterflyManager = ButterflyManager.getInstance();

    public Merger() {
    }

    public void merge() {
        var tempTmoOperator = "TMO-MERGER";
        var prefix = "TMO_";
        var butterfly = mButterflyManager.getButterfly();
        butterfly.topo().getControlPoints().removeIf(p -> StringUtils.equals(p.getOperator(), tempTmoOperator));
        butterfly.topo().getControlPointsObservations().removeIf(o -> StringUtils.equals(o.getOperator(), tempTmoOperator));

        var topoObservations = new ArrayList<BTopoControlPointObservation>();
        butterfly.tmo().getRorelseObservations().forEach(r -> {
            var o = new BTopoControlPointObservation();
            o.setName(prefix + r.getName());
            o.setDate(r.getDate());
            o.setMeasuredZ(r.getVarde());

            topoObservations.add(o);
        });

        var topoPoints = new ArrayList<BTopoControlPoint>();

        butterfly.tmo().getRorelse().forEach(r -> {
            var p = new BTopoControlPoint();
            p.setName(prefix + r.getName());
            p.setGroup(prefix + r.getPlacering());
            p.setCategory(prefix + "Dubb");
            p.setDimension(BDimension._1d);
            p.setFrequency(99);
//            p.setDateRolling(LocalDate.now());
//            p.setDateZero(LocalDate.now());

            p.setZeroX(r.getX());
            p.setZeroY(r.getY());
            var obs = topoObservations.stream()
                    .filter(o -> StringUtils.equals(o.getName(), p.getName()))
                    .toList();

            if (!obs.isEmpty()) {
                var first = obs.getFirst();
                var last = obs.getLast();

                p.setZeroZ(first.getMeasuredZ());
                p.setDateLatest(last.getDate());

            }
//                    .findFirst().orElse(0.0);

            p.setOperator(tempTmoOperator);
            p.setComment(r.getAnmärkning());
            p.setMeta("");
            p.setAlarm1Id("");
            p.setAlarm2Id("");
            p.setNumOfDecXY(3);
            p.setNumOfDecZ(3);
            p.setOrigin(prefix + "TMO");

            switch (r.getStatus()) {
                case "Aktiv" ->
                    p.setStatus("S1");
                case "Finns ej" ->
                    p.setStatus("S3");
                case "Inaktiv" ->
                    p.setStatus("S5");
                default ->
                    p.setStatus("S0");
            }

            p.setButterfly(butterfly);
            topoPoints.add(p);
        });

        mButterflyManager.calculateLatLons(topoPoints);
        butterfly.topo().getControlPointsObservations().addAll(topoObservations);
        butterfly.topo().getControlPoints().addAll(topoPoints);

        mTopoManager.load(butterfly);
    }
}
