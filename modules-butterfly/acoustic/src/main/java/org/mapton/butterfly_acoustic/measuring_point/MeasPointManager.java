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
package org.mapton.butterfly_acoustic.measuring_point;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.acoustic.BAcousticMeasuringPoint;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MeasPointManager extends BaseManager<BAcousticMeasuringPoint> {

    private final MeasPointChartBuilder mChartBuilder = new MeasPointChartBuilder();
    private final MeasPointPropertiesBuilder mPropertiesBuilder = new MeasPointPropertiesBuilder();

    public static MeasPointManager getInstance() {
        return Holder.INSTANCE;
    }

    private MeasPointManager() {
        super(BAcousticMeasuringPoint.class);
    }

    @Override
    public Object getObjectChart(BAcousticMeasuringPoint selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BAcousticMeasuringPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.noise().getMeasuringPoints());
            initObjectToItemMap();

            butterfly.noise().getMeasuringPoints().forEach(p -> {
                var channels = butterfly.noise().getMeasuringChannels().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getId())).toList();
                p.ext().setChannels(new ArrayList<>(channels));
                var limits = butterfly.noise().getMeasuringLimits().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getId())).toList();
                p.ext().setLimits(new ArrayList<>(limits));

                var observations = butterfly.noise().getMeasuringObservations().stream()
                        .filter(o -> o.getName().equalsIgnoreCase(p.getName()))
                        .collect(Collectors.toCollection(ArrayList::new));

                if (!observations.isEmpty()) {
                    p.setDateLatest(observations.getLast().getDate());
                }
                p.ext().setDateLatest(p.getDateLatest());
                p.ext().setObservationsAllRaw(observations);
                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));
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
        var timeFilteredItems = new ArrayList<BAcousticMeasuringPoint>();

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

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BAcousticMeasuringPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final MeasPointManager INSTANCE = new MeasPointManager();
    }
}
