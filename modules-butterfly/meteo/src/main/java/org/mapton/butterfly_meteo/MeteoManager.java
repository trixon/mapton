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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MLatLon;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_meteo.chart.MeteoChartBuilder;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeteoManager extends BaseManager<BMeteoPoint> {

    private final static String DISRUPTOR_NAME = Bundle.CTL_MeteoAction();

    private final MeteoChartBuilder mChartBuilder = new MeteoChartBuilder();
    private final MeteoOptions mOptions = MeteoOptions.getInstance();
    private final MeteoPropertiesBuilder mPropertiesBuilder = new MeteoPropertiesBuilder();

    public static MeteoManager getInstance() {
        return Holder.INSTANCE;
    }

    private MeteoManager() {
        super(BMeteoPoint.class);
    }

    @Override
    public List<String> getObjectAnnotation(BMeteoPoint p) {
        if (mOptions.isPlotAnnotation()) {
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
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BMeteoPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getMeteoPoints());
            initObjectToItemMap();

            butterfly.getMeteoPointsObservations().forEach(p -> {
//                var channels = butterfly.noise().getVibrationChannels().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getExternalId())).toList();
//                p.ext().setChannels(new ArrayList<>(channels));
//                var limits = butterfly.noise().getVibrationLimits().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getExternalId())).toList();
//                p.ext().setLimits(new ArrayList<>(limits));
//
//                var status = "S5";
//                for (var channel : channels) {
//                    if (DateHelper.isBetween(channel.getFrom(), channel.getUntil(), LocalDate.now())) {
//                        status = "S1";
//                        break;
//                    }
//                }
//                p.setStatus(status);

                var observations = butterfly.noise().getVibrationObservations().stream()
                        .filter(o -> o.getName().equalsIgnoreCase(p.getName()))
                        .collect(Collectors.toCollection(ArrayList::new));

//                if (!observations.isEmpty()) {
//                    p.ext().setDateFirst(observations.getFirst().getDate());
//                    p.setDateLatest(observations.getLast().getDate());
//                } else {
//                    p.ext().setDateFirst(LocalDateTime.MIN);
//                }
//                p.ext().setDateLatest(p.getDateLatest());
//                p.ext().setObservationsAllRaw(observations);
//                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));
            });

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
            p.ext().calculateObservations(timeFilteredObservations);

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            timeFilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        var latLonDisruptors = timeFilteredItems.stream().map(p -> new MLatLon(p.getLat(), p.getLon())).toList();
        mDisruptorManager.putLatLons(DISRUPTOR_NAME, latLonDisruptors);
        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BMeteoPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class BlastDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final MeteoManager INSTANCE = new MeteoManager();
    }
}
