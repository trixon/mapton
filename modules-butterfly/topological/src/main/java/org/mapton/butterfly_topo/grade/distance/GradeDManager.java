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
package org.mapton.butterfly_topo.grade.distance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javafx.scene.Node;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAxis;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import org.mapton.butterfly_topo.grade.GradeManagerBase;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GradeDManager extends GradeManagerBase {

    public static final Double MAX_2D_DISTANCE = 10.0;
    public static final Double MAX_RADIAL_DISTANCE = 50.0;
    public static final Double MIN_RADIAL_DISTANCE = 0.050;
    private final ManagerOptionsView mManagerOptionsView = new ManagerOptionsView();
    private final DistanceOptions mOptions = DistanceOptions.getInstance();
    private final DistancePropertiesBuilder mPropertiesBuilder = new DistancePropertiesBuilder();

    public static GradeDManager getInstance() {
        return Holder.INSTANCE;
    }

    private GradeDManager() {
        super(BTopoGrade.class);
    }

    @Override
    public Object getObjectProperties(BTopoGrade selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public Node getOptionsView() {
        return mManagerOptionsView.getUI();
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
    public void load() {
        var gradesLim = switch (mOptions.getDistanceMode()) {
            case _1d ->
                load1d();
            case _2d ->
                load2d();
            case _3d ->
                load3d();
        };

        gradesLim.forEach(g -> {
            var first = BCoordinatrix.toLatLon(g.getP1());
            var second = BCoordinatrix.toLatLon(g.getP2());
            var d = first.distance(second);
            var b = first.getBearing(second);
            var mid = first.getDestinationPoint(b, d * .5);
            g.setLat(mid.getLatitude());
            g.setLon(mid.getLongitude());
            if (mOptions.getDistanceMode() == BDimension._1d) {
                g.setValue("distanceMode", mOptions.getDistanceMode());
            }
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

    private ArrayList<BTopoGrade> load1d() {
        var dimensionToPointsMap = mTopoManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() != BDimension._2d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                .collect(Collectors.groupingBy(BTopoControlPoint::getDimension));

        var list1d = dimensionToPointsMap.getOrDefault(BDimension._1d, List.of());
        var list3d = dimensionToPointsMap.getOrDefault(BDimension._3d, List.of());

        if (list1d.isEmpty() || list3d.isEmpty()) {
            return new ArrayList<>();
        }

        for (var p : list1d) {
            BTopoGrade.getCachePointToObservations().computeIfAbsent(p, k -> BTopoGrade.createObservationMap(k));
        }

        for (var p : list3d) {
            BTopoGrade.getCachePointToObservations().computeIfAbsent(p, k -> BTopoGrade.createObservationMap(k));
        }

        var limit = 1000;

        Comparator<BTopoGrade> fullComparator = Comparator.comparingDouble(o -> o.ext().getDiff().getPartialDiffZAbs());

        return IntStream.range(0, list1d.size()).parallel()
                .boxed()
                .flatMap(i -> IntStream.range(0, list3d.size()).mapToObj(j -> new int[]{i, j}))
                .collect(Collector.of(
                        () -> new PriorityQueue<BTopoGrade>(limit + 1, fullComparator),
                        (queue, pairIndex) -> {
                            var p1 = list1d.get(pairIndex[0]);
                            var p2 = list3d.get(pairIndex[1]);

                            if (p1 == p2 || p1.getZeroZ() > p2.getZeroZ()) {
                                return;
                            }

                            var distance = Math.hypot(p1.getZeroX() - p2.getZeroX(), p1.getZeroY() - p2.getZeroY());
                            if (distance <= MAX_2D_DISTANCE) {
                                var grade = new BTopoGrade(BAxis.RESULTANT, p1, p2);
                                grade.calculate(getStartDate(), getEndDate());

                                if (grade.getCommonObservations().size() > 1) {
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
                            result.sort(fullComparator.reversed());
                            return result;
                        }
                ));
    }

    private ArrayList<BTopoGrade> load2d() {
        return load1d();
    }

    private ArrayList<BTopoGrade> load3d() {
        var sourcePoints = mTopoManager.getTimeFilteredItems().stream()
                .filter(p -> p.getDimension() != BDimension._2d)
                .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                .filter(p -> p.ext().getNumOfObservationsFiltered() >= 2)
                .toList();

        for (var p : sourcePoints) {
            BTopoGrade.getCachePointToObservations().computeIfAbsent(p, k -> BTopoGrade.createObservationMap(k));
        }

        int numOfPoints = sourcePoints.size();
        var limit = 1000;

        Comparator<BTopoGrade> fullComparator = Comparator.comparingDouble(o -> o.ext().getDiff().getPartialDiffDistanceAbs());

        return IntStream.range(0, numOfPoints).parallel()
                .boxed()
                .flatMap(i -> IntStream.range(i + 1, numOfPoints).mapToObj(j -> new int[]{i, j}))
                .collect(Collector.of(
                        () -> new PriorityQueue<BTopoGrade>(limit + 1, fullComparator),
                        (queue, pairIndex) -> {
                            var p1 = sourcePoints.get(pairIndex[0]);
                            var p2 = sourcePoints.get(pairIndex[1]);
                            var distance = Math.hypot(p1.getZeroX() - p2.getZeroX(), p1.getZeroY() - p2.getZeroY());

                            if (distance >= MIN_RADIAL_DISTANCE && distance <= MAX_RADIAL_DISTANCE) {
                                var grade = new BTopoGrade(BAxis.RESULTANT, p1, p2);
                                grade.calculate(getStartDate(), getEndDate());

                                if (grade.getCommonObservations().size() > 1) {
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
                            result.sort(fullComparator.reversed());
                            return result;
                        }
                ));
    }

    private static class Holder {

        private static final GradeDManager INSTANCE = new GradeDManager();
    }
}
