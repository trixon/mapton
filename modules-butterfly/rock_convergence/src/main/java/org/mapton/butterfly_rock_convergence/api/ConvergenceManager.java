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
package org.mapton.butterfly_rock_convergence.api;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BMeasurementMode;
import org.mapton.butterfly_format.types.rock.BRockConvergence;
import org.mapton.butterfly_format.types.rock.BRockConvergenceObservation;
import org.mapton.butterfly_format.types.rock.BRockConvergencePair;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_rock_convergence.ConvergencePropertiesBuilder;
import org.mapton.butterfly_rock_convergence.chart.ChartAggregate;
import org.mapton.butterfly_rock_convergence.chart.ConvergenceChartBuilder;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceManager extends BaseManager<BRockConvergence> {

    private final ChartAggregate mChartAggregate = new ChartAggregate();
    private final ConvergenceChartBuilder mChartBuilder = new ConvergenceChartBuilder();
    private Runnable mFilterReloadRunnable;
    private final ConvergencePropertiesBuilder mPropertiesBuilder = new ConvergencePropertiesBuilder();

    public static ConvergenceManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceManager() {
        super(BRockConvergence.class);

        Mapton.getGlobalState().addListener(gsce -> {
            load2(gsce.getValue());
        }, TopoManager.KEY_TOPO_POINTS_LOADED);
    }

    public void activatefilterPopoverLoad(Runnable r) {
        mFilterReloadRunnable = r;
    }

    @Override
    public Object getObjectChart(BRockConvergence selectedObject) {
        boolean isCtrlPressed = KeyboardUtils.isPressed(KeyEvent.VK_CONTROL);
        if (isCtrlPressed) {
            return mChartBuilder.build(selectedObject);
        } else {
            return mChartAggregate.build(selectedObject);
        }
    }

    @Override
    public Object getObjectProperties(BRockConvergence selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    public double getOffset() {
        var offset = 0d;
        for (var g : getTimeFilteredItems()) {
            var o = g.ext().getControlPointsWithoutAnchor().stream()
                    .map(p -> p.getZeroZ())
                    .mapToDouble(Double::doubleValue).min().orElse(0);
            offset = FastMath.min(o, offset);
        }

        if (offset < 0) {
            offset = offset * -1.0;
        }

        offset += 2;

        return offset;
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    public void load2(Butterfly butterfly) {
        try {
            initAllItems(butterfly.rock().getConvergence());
            initObjectToItemMap();
            var convergenceToObservations = butterfly.rock().getConvergenceObservations().stream()
                    .collect(Collectors.groupingBy(o -> o.getName()));

            var dates = new TreeSet<LocalDateTime>();
            for (var g : butterfly.rock().getConvergence()) {
                g.ext().getObservationsAllRaw().clear();
                var controlPoints = Arrays.stream(StringUtils.split(g.getRef(), ","))
                        .map(s -> butterfly.topo().getControlPointByName(s))
                        .filter(p -> p != null)
                        .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                        .collect(Collectors.toCollection(ArrayList::new));

                g.ext().setControlPoints(controlPoints);
                var pairToObservations = convergenceToObservations.get(g.getName()).stream()
                        .collect(Collectors.groupingBy(item -> item.getP1Name() + "::" + item.getP2Name(), Collectors.toCollection(ArrayList::new)));

                var pairs = new ArrayList<BRockConvergencePair>();
                int n = controlPoints.size();
                var offset = 10.0;//TODO Calculate it?
                var anchorPoint = g.ext().getAnchorPoint();
                i:
                for (int i = 0; i < n; i++) {
                    j:
                    for (int j = i + 1; j < n; j++) {
                        var pointI = controlPoints.get(i);
                        if (pointI == anchorPoint) {
                            continue i;
                        }

                        var pointJ = controlPoints.get(j);
                        if (pointJ == anchorPoint) {
                            continue j;
                        }
                        var pair = new BRockConvergencePair(g, pointI, pointJ, offset);
                        var observations = pairToObservations.getOrDefault(pair.getP1().getName() + "::" + pair.getP2().getName(), new ArrayList<>());
                        observations.sort(Comparator.comparing(BRockConvergenceObservation::getDate));
                        for (var o : observations) {
                            o.ext().setParent(g);
                            o.ext().setPair(pair);
                            dates.add(o.getDate());
                            o.ext().setDeltaX(o.getCalculatedConvergence1d());
                            o.ext().setDeltaY(o.getCalculatedConvergence2d());
                            o.ext().setDeltaZ(o.getCalculatedConvergence3d());
                            if (o.isZeroMeasurement()) {
                                g.ext().setStoredZeroDateTime(o.getDate());
                            }
                        }
                        pair.ext().setObservationsTimeFiltered(observations);
                        pairs.add(pair);
                    }
                }
                g.ext().getPairs().clear();
                g.ext().getPairs().addAll(pairs);
            }

            for (var g : butterfly.rock().getConvergence()) {
                var maxPoint = g.ext().getControlPointsWithoutAnchor().stream()
                        .max(Comparator.comparingDouble(BTopoControlPoint::getZeroZ));

                maxPoint.ifPresent(p -> {
                    g.setZeroX(p.getZeroX());
                    g.setZeroY(p.getZeroY());
                    g.setZeroZ(p.getZeroZ());
                    g.setLat(p.getLat());
                    g.setLon(p.getLon());
                });

                var maxObservationsPerDate = convergenceToObservations.get(g.getName()).stream()
                        .collect(Collectors.groupingBy(BRockConvergenceObservation::getDate))
                        .values()
                        .stream()
                        .flatMap(entry -> entry.stream()
                        .max(Comparator.comparingDouble(value -> Math.abs(value.getMeasuredZ())))
                        .stream())
                        .sorted(Comparator.comparing(BRockConvergenceObservation::getDate))
                        .collect(Collectors.toList());
                maxObservationsPerDate.forEach(o -> o.ext().setDeltaZ(o.ext().getDeltaZ()));
                g.ext().getObservationsAllRaw().addAll(maxObservationsPerDate);
                var minDate = maxObservationsPerDate.stream().map(gg -> gg.getDate()).min(LocalDateTime::compareTo);
                var maxDate = maxObservationsPerDate.stream().map(gg -> gg.getDate()).max(LocalDateTime::compareTo);
                g.ext().setDateFirst(minDate.orElse(LocalDateTime.MIN));
                maxDate.ifPresent(date -> g.setDateLatest(date));
                g.ext().setDateLatest(g.getDateLatest());
            }

            var origins = getAllItems()
                    .stream().map(p -> p.getOrigin())
                    .collect(Collectors.toCollection(TreeSet::new))
                    .stream()
                    .collect(Collectors.toCollection(ArrayList<String>::new));
            setValue("origins", origins);

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
                boolean layerBundleEnabled = isLayerBundleEnabled();
                updateTemporal(!layerBundleEnabled);
                updateTemporal(layerBundleEnabled);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        if (mFilterReloadRunnable != null) {
            mFilterReloadRunnable.run();
        }

        calculateDynamicFreq(butterfly);
    }

    @Override
    protected void applyTemporalFilter() {
        var timeFilteredItems = new ArrayList<BRockConvergence>();

        p:
        for (var g : getFilteredItems()) {
            if (g.getDateLatest() == null || g.ext().getObservationsAllRaw().isEmpty()) {
                timeFilteredItems.add(g);
            } else {
                for (var o : g.ext().getObservationsAllRaw()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(g);
                        continue p;
                    }
                }
            }
        }

        getTimeFilteredItemsMap().clear();
        timeFilteredItems.stream().forEach(g -> {
            getTimeFilteredItemsMap().put(g.getName(), g);
            var timeFilteredObservations = g.ext().getObservationsAllRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .collect(Collectors.toCollection(ArrayList::new));
            g.ext().setObservationsTimeFiltered(timeFilteredObservations);
        });

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BRockConvergence> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void calculateDynamicFreq(Butterfly butterfly) {
        var blasts = butterfly.rock().getBlasts().stream()
                .filter(b -> b.getDateLatest().isAfter(LocalDateTime.now().minusMonths(3)))
                .toList();

        g:
        for (var g : butterfly.rock().getConvergence()) {
            if (g.getMeasurementMode() != BMeasurementMode.MANUAL) {
                continue;
            }

            g.setFrequency(666);
            var dates = new TreeSet<LocalDateTime>();
            var gDate = g.ext().getObservationRawLast().getDate();
            for (var b : blasts) {
                var bDate = b.getDateLatest();
                var blasAfterMeas = bDate.isAfter(gDate);
                if (blasAfterMeas) {
                    var gll = BCoordinatrix.toLatLon(g);
                    var bll = BCoordinatrix.toLatLon(b);
                    if (gll.distance(bll) <= 40) {
                        dates.add(b.getDateLatest().plusHours(12));
//                        dates.add(b.getDateLatest().toLocalDate());
                    }
                }
            }

            if (!dates.isEmpty()) {
                var daysBetween = (int) ChronoUnit.DAYS.between(gDate, dates.first());
                g.setFrequency(Math.max(1, Math.abs(daysBetween)));

            }
        }
    }

    private static class Holder {

        private static final ConvergenceManager INSTANCE = new ConvergenceManager();
    }
}
