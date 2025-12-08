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
package org.mapton.butterfly_remote.insar;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MLatLon;
import org.mapton.api.MOptions;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_core.api.ButterflyManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPointObservation;
import org.mapton.butterfly_remote.insar.chart.ChartAggregate;
import org.mapton.butterfly_remote.insar.chart.InsarChartBuilder;
import org.mapton.butterfly_remote.insar.chart.MultiChartAggregate;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.CollectionHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class InsarManager extends BaseManager<BRemoteInsarPoint> {

    private final static String DISRUPTOR_NAME = Bundle.CTL_InsarAction();

    private final ChartAggregate mChartAggregate = new ChartAggregate();
    private final InsarChartBuilder mChartBuilder = new InsarChartBuilder();
    private Runnable mFilterPopoverPopulateRunnable;
    private final MultiChartAggregate mMultiChartAggregate = new MultiChartAggregate();
    private final InsarPropertiesBuilder mPropertiesBuilder = new InsarPropertiesBuilder();

    public static InsarManager getInstance() {
        return Holder.INSTANCE;
    }

    private InsarManager() {
        super(BRemoteInsarPoint.class);
    }

    @Override
    public Object getObjectChart(BRemoteInsarPoint selectedObject) {
        if (KeyboardUtils.isPressed(KeyEvent.VK_SHIFT)) {
            return mChartBuilder.build(selectedObject);
        } else {
            boolean isCtrlPressed = KeyboardUtils.isPressed(KeyEvent.VK_CONTROL);
            if (isCtrlPressed) {
                return mMultiChartAggregate.build(selectedObject);
            } else {
                return mChartAggregate.build(selectedObject);
            }
        }
    }

    @Override
    public Object getObjectProperties(BRemoteInsarPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        var remote = ButterflyManager.getInstance().getButterfly().remote();
        setClearables(
                remote.getInsarPoints(),
                remote.getInsarPointsObservations(),
                remote.getNameToInsarPoint()
        );
        var autoLoad = MSimpleObjectStorageManager.getInstance().getBoolean(AutoLoadInsarSOSB.class, AutoLoadInsarSOSB.DEFAULT_VALUE);
        if (mFirstLoad && autoLoad) {
            load2(butterfly);
        }
    }

    public void putLatLons(List<? extends BRemoteInsarPoint> list) {
        var cooTrans = MOptions.getInstance().getMapCooTrans();
        for (var p : list) {
            var latLon = p.<MLatLon>getValue("MLATLON");
            var pp = cooTrans.fromWgs84(latLon.getLatitude(), latLon.getLongitude());
            var coordinate = new Coordinate(pp.getY(), pp.getX());
            var point = mGeometryFactory.createPoint(coordinate);
            p.setValue("POINT", point);
        }

//        putGeometries(disruptorName, geometries);
    }

    public void setFilterPopoverPopulateRunnable(Runnable filterPopoverPopulateRunnable) {
        mFilterPopoverPopulateRunnable = filterPopoverPopulateRunnable;
    }

    @Override
    protected void applyTemporalFilter() {
        var measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = new ArrayList<BRemoteInsarPoint>();

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
                    .collect(Collectors.toCollection(ArrayList::new));

            p.ext().setObservationsTimeFiltered(timeFilteredObservations);
            p.ext().calculateObservations(timeFilteredObservations);

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            timeFilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        var pointDisruptors = timeFilteredItems.stream().map(p -> p.<Point>getValue("POINT")).toList();
        mDisruptorManager.putGeometries(DISRUPTOR_NAME, pointDisruptors);
//        Thread.ofVirtual().start(() -> {

//
//            var latLonDisruptors = timeFilteredItems.stream().map(p -> new MLatLon(p.getLat(), p.getLon())).toList();
//            var latLonDisruptors = timeFilteredItems.stream().map(p -> p.<MLatLon>getValue("MLATLON")).toList();
//            mDisruptorManager.putLatLons(DISRUPTOR_NAME, latLonDisruptors);
//            FxHelper.runLater(() -> {
//            });
//        });
        setItemsTimeFiltered(timeFilteredItems);

    }

    @Override
    protected void load(ArrayList<BRemoteInsarPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void load2(Butterfly butterfly) {
        mFirstLoad = false;
        clear();
        SystemHelper.runGcDelayed(50);

        Runnable task = () -> {
            butterfly.loadManual();
            ButterflyManager.getInstance().calculateLatLons(butterfly.remote().getInsarPoints());
            putLatLons(butterfly.remote().getInsarPoints());

            FxHelper.runLater(() -> {
                try {
                    initAllItems(butterfly.remote().getInsarPoints());
                    initObjectToItemMap();

                    var nameToObservations = new LinkedHashMap<String, ArrayList<BRemoteInsarPointObservation>>();
                    for (var o : butterfly.remote().getInsarPointsObservations()) {
                        nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
                    }

                    for (var p : butterfly.remote().getInsarPoints()) {
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
                            o.setMeasuredZ(p.getZeroZ() + o.getMeasuredZ() / 1000d);
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
            });
            mFilterPopoverPopulateRunnable.run();
            SystemHelper.runGcDelayed(50);
        };

        SystemHelper.runLaterDelayed(1000, task);
//        Thread.ofVirtual().start(task);
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class BlastDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final InsarManager INSTANCE = new InsarManager();
    }
}
