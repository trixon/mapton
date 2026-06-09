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
package org.mapton.butterfly_meteo;

import com.sun.jna.platform.KeyboardUtils;
import java.awt.event.KeyEvent;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BMeasurementReport;
import org.mapton.butterfly_core.api.BMeasurementTab;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_format.types.BMeteoPointObservation;
import org.mapton.butterfly_meteo.chart.ChartAggregate;
import org.mapton.butterfly_meteo.chart.MeteoChartBuilder;
import org.mapton.butterfly_meteo.table.StandardMeasurementPopulator;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeteoManager extends BaseManager<BMeteoPoint> {

    //    private final MultiChartAggregate mMultiChartAggregate = new MultiChartAggregate();
    private final ChartAggregate mChartAggregate = new ChartAggregate();
    private final MeteoChartBuilder mChartBuilder = new MeteoChartBuilder();
    private final MeteoLayerOptions mLayerOptions = MeteoLayerOptions.getInstance();
    private final MeteoPropertiesBuilder mPropertiesBuilder = new MeteoPropertiesBuilder();
    private final StandardMeasurementPopulator mStandardMeasurementPopulator = new StandardMeasurementPopulator();

    public static MeteoManager getInstance() {
        return Holder.INSTANCE;
    }

    private MeteoManager() {
        super(BMeteoPoint.class);
    }

    @Override
    public List<String> getObjectAnnotation(BMeteoPoint p) {
        if (mLayerOptions.isPlotAnnotation()) {
            var ext = p.extOrNull();
            return List.of(
                    p.getName(),
                    ext.getDateLatest() != null ? ext.getDateLatest().toLocalDate().toString() : "-"
            );
        } else {
            return null;
        }
    }

    @Override
    public Object getObjectChart(BMeteoPoint selectedObject) {
        if (KeyboardUtils.isPressed(KeyEvent.VK_SHIFT)) {
            return mChartBuilder.build(selectedObject);
        } else {
            return mChartAggregate.build(selectedObject);
        }
    }

    @Override
    public Object getObjectMeasurements(BMeteoPoint p) {
        if (p == null) {
            return null;
        } else {
            mStandardMeasurementPopulator.populate(p);
            var tabs = List.of(new BMeasurementTab("Standard", mStandardMeasurementPopulator.getTableView()));
            var measurementReport = new BMeasurementReport(p, tabs);
            return measurementReport;
        }
    }

    @Override
    public Object getObjectProperties(BMeteoPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.meteo().getMeteoPoints());
            initObjectToItemMap();
            var nameToObservations = new LinkedHashMap<String, ArrayList<BMeteoPointObservation>>();
            for (var o : butterfly.meteo().getMeteoPointsObservations()) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            for (var p : butterfly.meteo().getMeteoPoints()) {
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
    }

    @Override
    protected void applyTemporalFilter() {
        var measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = new ArrayList<BMeteoPoint>();

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
            //p.ext().calculateObservations(timeFilteredObservations);

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            timeFilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BMeteoPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final MeteoManager INSTANCE = new MeteoManager();
    }
}
