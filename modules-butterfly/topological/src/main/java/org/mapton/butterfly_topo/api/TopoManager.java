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
package org.mapton.butterfly_topo.api;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.math3.util.FastMath;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_core.api.TrendHelper;
import org.mapton.butterfly_core.api.TrendHelper.Trend;
import org.mapton.butterfly_core.api.sos.ScalePlot3dHSosi;
import org.mapton.butterfly_format.Butterfly;
import static org.mapton.butterfly_format.types.BDimension._1d;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.TopoPropertiesBuilder;
import org.mapton.butterfly_topo.TopoTrendsBuilder;
import org.mapton.butterfly_topo.chart.ChartAggregate;
import org.mapton.butterfly_topo.chart.MultiChartAggregate;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoManager extends BaseManager<BTopoControlPoint> {

    public static final String KEY_TOPO_POINTS_LOADED = "TopoPointsLoaded";
    public static final String KEY_TRENDS_H = "trendsH";
    public static final String KEY_TRENDS_P = "trendsP";
    private final ChartAggregate mChartAggregate = new ChartAggregate();
    private int mLoadTrends = 0;
    private double mMinimumZscaled = 0.0;
    private final MultiChartAggregate mMultiChartAggregate = new MultiChartAggregate();
    private final TopoPropertiesBuilder mPropertiesBuilder = new TopoPropertiesBuilder();
    private final TopoTrendsBuilder mTrendsBuilder = new TopoTrendsBuilder();

    public static TopoManager getInstance() {
        return TopoManagerHolder.INSTANCE;
    }

    private TopoManager() {
        super(BTopoControlPoint.class);
    }

    public double getMinimumZscaled() {
        return mMinimumZscaled;
    }

    @Override
    public Object getObjectChart(BTopoControlPoint selectedObject) {
        boolean isCtrlPressed = KeyboardUtils.isPressed(KeyEvent.VK_CONTROL);
        if (isCtrlPressed) {
            return mMultiChartAggregate.build(selectedObject);
        } else {
            return mChartAggregate.build(selectedObject);
        }
    }

    @Override
    public Object getObjectProperties(BTopoControlPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectTrends(BTopoControlPoint selectedObject) {
        return mTrendsBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.topo().getControlPoints());
            initObjectToItemMap();

            var nameToObservations = new LinkedHashMap<String, ArrayList<BTopoControlPointObservation>>();
            for (var o : butterfly.topo().getControlPointsObservations()) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            for (var p : butterfly.topo().getControlPoints()) {
                var observations = nameToObservations.getOrDefault(p.getName(), new ArrayList<>());
                if (!observations.isEmpty()) {
                    p.ext().setDateFirst(observations.getFirst().getDate());
                    p.setDateLatest(observations.getLast().getDate());
                } else {
                    p.ext().setDateFirst(LocalDateTime.MIN);
                }

                p.ext().setDateLatest(p.getDateLatest());
                p.ext().setObservationsAllRaw(observations);
                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));
                for (var o : p.ext().getObservationsAllRaw()) {
                    if (o.isZeroMeasurement()) {
                        p.ext().setStoredZeroDateTime(o.getDate());
                        break;
                    }
                }
            }

            var origins = getAllItems()
                    .stream().map(p -> p.getOrigin())
                    .collect(Collectors.toCollection(TreeSet::new))
                    .stream()
                    .collect(Collectors.toCollection(ArrayList<String>::new));
            setValue("origins", origins);

            var dates = new TreeSet<LocalDateTime>();
            getAllItems().stream().forEachOrdered(p -> {
                dates.addAll(p.ext().getObservationsAllRaw().stream().map(o -> o.getDate()).toList());
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

//        for (var p : butterfly.topo().getControlPoints()) {
//            try {
//                populateTrends(p);
//            } catch (Exception e) {
//                //System.err.println(e);
//            }
//        }
        Mapton.getGlobalState().put(KEY_TOPO_POINTS_LOADED, ButterflyManager.getInstance().getButterfly());
    }

    @Override
    protected void applyTemporalFilter() {
        var measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = new ArrayList<BTopoControlPoint>();

        p:
        for (var p : getFilteredItems()) {
            if (p.getDateLatest() == null || p.ext().getObservationsAllRaw().isEmpty()) {
                timeFilteredItems.add(p);
            } else {
                for (var o : p.ext().getObservationsAllRaw()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(p);
                        continue p;
                    }
                }
            }
        }

        getTimeFilteredItemsMap().clear();
        timeFilteredItems.stream().forEach(p -> {
            getTimeFilteredItemsMap().put(p.getName(), p);
            var timeFilteredObservations = p.ext().getObservationsAllRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .filter(o -> {
                        switch (p.getDimension()) {
                            case _1d -> {
                                return ObjectUtils.allNotNull(o.getMeasuredZ());
                            }
                            case _2d -> {
                                return ObjectUtils.allNotNull(o.getMeasuredX(), o.getMeasuredY());
                            }
                            case _3d -> {
                                return ObjectUtils.allNotNull(o.getMeasuredX(), o.getMeasuredY(), o.getMeasuredZ());
                            }
                            default ->
                                throw new AssertionError();
                        }
                    })
                    .collect(Collectors.toCollection(ArrayList::new));

            p.ext().setObservationsTimeFiltered(timeFilteredObservations);
            p.ext().calculateObservations(timeFilteredObservations);

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            timeFilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        var mScale3dH = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dHSosi.class, 500);

        mMinimumZscaled = Double.MAX_VALUE;
        for (var p : timeFilteredItems) {
            try {
                mMinimumZscaled = FastMath.min(mMinimumZscaled, p.getZeroZ() + mScale3dH * p.ext().deltaZero().getDeltaZ());
            } catch (Exception e) {
                //nvm
            }
        }

        if (mLoadTrends++ < 3) {
            for (var p : timeFilteredItems) {
                try {
                    populateTrends(p);
                } catch (Exception e) {
                    //System.err.println(e);
                }
            }
        }

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BTopoControlPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void populateTrend(BTopoControlPoint p, String key, LocalDateTime startDate, LocalDateTime endDate) {
        switch (p.getDimension()) {
            case _1d ->
                populateTrendH(p, key, startDate, endDate, (BXyzPointObservation o) -> o.ext().getDelta1d());
            case _2d ->
                populateTrendP(p, key, startDate, endDate, (BXyzPointObservation o) -> o.ext().getDelta2d());
            case _3d -> {
                populateTrendH(p, key, startDate, endDate, (BXyzPointObservation o) -> o.ext().getDelta1d());
                populateTrendP(p, key, startDate, endDate, (BXyzPointObservation o) -> o.ext().getDelta2d());
            }
        }
    }

    private void populateTrendH(BTopoControlPoint p, String key, LocalDateTime startDate, LocalDateTime endDate, Function<BXyzPointObservation, Double> function) {
        try {
            var trend = TrendHelper.createTrend(p, startDate, endDate, function);
            HashMap<String, Trend> map = (HashMap<String, Trend>) p.getValue(KEY_TRENDS_H, new HashMap<String, Trend>());
            map.put(key, trend);
            p.setValue(KEY_TRENDS_H, map);
        } catch (Exception e) {
        }
    }

    private void populateTrendP(BTopoControlPoint p, String key, LocalDateTime startDate, LocalDateTime endDate, Function<BXyzPointObservation, Double> function) {
        var trend = TrendHelper.createTrend(p, startDate, endDate, function);
        HashMap<String, Trend> map = (HashMap<String, Trend>) p.getValue(KEY_TRENDS_P, new HashMap<String, Trend>());
        map.put(key, trend);
        p.setValue(KEY_TRENDS_P, map);
    }

    private void populateTrends(BTopoControlPoint p) {
        var startDateFirst = p.ext().getDateFirst();
        var startDateZero = p.getDateZero().atStartOfDay();
        var endDate = LocalDateTime.now();
        var startDateMinus6m = endDate.minusMonths(6);
        var startDateMinus3m = endDate.minusMonths(3);
        var startDateMinus1m = endDate.minusMonths(1);
        var startDateMinus1w = endDate.minusWeeks(1);

        populateTrend(p, "f", startDateFirst, endDate);
        populateTrend(p, "z", startDateZero, endDate);
        populateTrend(p, "6m", startDateMinus6m, endDate);
        populateTrend(p, "3m", startDateMinus3m, endDate);
        populateTrend(p, "1m", startDateMinus1m, endDate);
        populateTrend(p, "1w", startDateMinus1w, endDate);
    }

    private static class TopoManagerHolder {

        private static final TopoManager INSTANCE = new TopoManager();
    }
}
