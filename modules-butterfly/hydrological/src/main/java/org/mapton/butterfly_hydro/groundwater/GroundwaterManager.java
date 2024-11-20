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
package org.mapton.butterfly_hydro.groundwater;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPointObservation;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GroundwaterManager extends BaseManager<BHydroGroundwaterPoint> {

    private final GroundwaterPropertiesBuilder mPropertiesBuilder = new GroundwaterPropertiesBuilder();
    private final GroundwaterChartBuilder mChartBuilder = new GroundwaterChartBuilder();

    public static GroundwaterManager getInstance() {
        return Holder.INSTANCE;
    }

    private GroundwaterManager() {
        super(BHydroGroundwaterPoint.class);
    }

    @Override
    public Object getObjectChart(BHydroGroundwaterPoint selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BHydroGroundwaterPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {

        try {
            initAllItems(butterfly.hydro().getGroundwaterPoints());
            initObjectToItemMap();

            var nameToObservations = new LinkedHashMap<String, ArrayList<BHydroGroundwaterPointObservation>>();
            for (var o : butterfly.hydro().getGroundwaterPointsObservations()) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            for (var p : butterfly.hydro().getGroundwaterPoints()) {
                var observations = nameToObservations.getOrDefault(p.getName(), new ArrayList<>());
                if (!observations.isEmpty()) {
                    p.setDateLatest(observations.getLast().getDate());
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
        var timeFilteredItems = new ArrayList<BHydroGroundwaterPoint>();

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

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BHydroGroundwaterPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GroundwaterManager INSTANCE = new GroundwaterManager();
    }
}
