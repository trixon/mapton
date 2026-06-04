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
package org.mapton.butterfly_topo.grade.vertical;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.stream.Collector;
import java.util.stream.IntStream;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVManager extends GradeManagerBase {

    public static final Double MAX_HORIZONTAL_DISTANCE = 10.0;
    public static final Double MAX_VERTICAL_DISTANCE = 50.0;
    public static final Double MIN_HORIZONTAL_DISTANCE = 0.0;
    public static final Double MIN_VERTICAL_DISTANCE = 0.5;
    private final GradeVPropertiesBuilder mPropertiesBuilder = new GradeVPropertiesBuilder();

    public static GradeVManager getInstance() {
        return Holder.INSTANCE;
    }

    private GradeVManager() {
        super(BTopoGrade.class);
    }

    @Override
    public Object getObjectProperties(BTopoGrade selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
    public void load() {
        var sourcePoints = mTopoManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() == BDimension._3d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                .peek(p -> {
                    BTopoGrade.getCachePointToObservations().computeIfAbsent(p, k -> BTopoGrade.createObservationMap(k));
                })
                .toList();

        Comparator<BTopoGrade> c1 = Comparator.comparingInt(o
                -> o.ext().getAlarmLevelPlane(Math.abs(o.ext().getDiff().getRQuota()))
        );
        Comparator<BTopoGrade> c2 = Comparator.comparingDouble(o
                -> Math.abs(o.ext().getDiff().getRQuota())
        );

        var fullComparator = c1.reversed().thenComparing(c2.reversed());
        var invertedComparator = fullComparator.reversed();
        int numOfPoints = sourcePoints.size();
        int limit = 1000;

        var gradesLim = IntStream.range(0, numOfPoints).parallel()
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, numOfPoints).mapToObj(j -> new int[]{i, j}))
                .collect(Collector.of(
                        () -> new PriorityQueue<BTopoGrade>(limit + 1, invertedComparator),
                        (queue, parIndex) -> {
                            var p1 = sourcePoints.get(parIndex[0]);
                            var p2 = sourcePoints.get(parIndex[1]);

                            if (ObjectUtils.anyNull(p1.getZeroX(), p1.getZeroY(), p1.getZeroZ(),
                                    p2.getZeroX(), p2.getZeroY(), p2.getZeroZ())) {
                                return;
                            }

                            var distanceR = Math.hypot(p1.getZeroX() - p2.getZeroX(), p1.getZeroY() - p2.getZeroY());
                            var distanceH = Math.abs(p2.getZeroZ() - p1.getZeroZ());
                            var validDistance = MathHelper.isBetween(MIN_HORIZONTAL_DISTANCE, MAX_HORIZONTAL_DISTANCE, distanceR)
                            && MathHelper.isBetween(MIN_VERTICAL_DISTANCE, MAX_VERTICAL_DISTANCE, distanceH);

                            if (validDistance) {
                                var grade = new BTopoGrade(BAxis.VERTICAL, p1, p2);
                                grade.calculate();

                                var isValid = grade.getCommonObservations().size() >= 2
                                && (Math.abs(grade.ext().getDiff().getRQuota()) >= 0.00001
                                || Math.abs(grade.ext().getDiff().getZQuota()) >= 0.00001);

                                if (isValid) {
                                    queue.offer(grade);
                                    if (queue.size() > limit) {
                                        queue.poll();
                                    }
                                }
                            }
                        },
                        (queue1, queue2) -> {
                            for (var element : queue2) {
                                queue1.offer(element);
                                if (queue1.size() > limit) {
                                    queue1.poll();
                                }
                            }
                            return queue1;
                        },
                        queue -> {
                            var result = new ArrayList<>(queue);
                            result.sort(fullComparator);
                            return result;
                        }
                ));

        gradesLim.forEach(g -> {
            var first = BCoordinatrix.toLatLon(g.getP1());
            var second = BCoordinatrix.toLatLon(g.getP2());
            var d = first.distance(second);
            var b = first.getBearing(second);
            var mid = first.getDestinationPoint(b, d * .5);
            g.setLat(mid.getLatitude());
            g.setLon(mid.getLongitude());
        });

        FxHelper.runLater(() -> {
            setItemsAll(gradesLim);
            setItemsFiltered(gradesLim);
            setItemsTimeFiltered(gradesLim);
        });
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoGrade> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GradeVManager INSTANCE = new GradeVManager();
    }
}
