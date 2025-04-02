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
package org.mapton.butterfly_acoustic.vibration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class VibrationManager extends BaseManager<BAcousticVibrationPoint> {

    private final VibrationChartBuilder mChartBuilder = new VibrationChartBuilder();
    private final VibrationPropertiesBuilder mPropertiesBuilder = new VibrationPropertiesBuilder();

    public static VibrationManager getInstance() {
        return Holder.INSTANCE;
    }

    private VibrationManager() {
        super(BAcousticVibrationPoint.class);
    }

    @Override
    public Object getObjectChart(BAcousticVibrationPoint selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BAcousticVibrationPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.noise().getVibrationPoints());
            initObjectToItemMap();

            butterfly.noise().getVibrationPoints().forEach(p -> {
                var channels = butterfly.noise().getVibrationChannels().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getExternalId())).toList();
                p.ext().setChannels(new ArrayList<>(channels));
                var limits = butterfly.noise().getVibrationLimits().stream().filter(c -> c.getPointId().equalsIgnoreCase(p.getExternalId())).toList();
                p.ext().setLimits(new ArrayList<>(limits));

                var status = "S5";
                for (var channel : channels) {
                    if (DateHelper.isBetween(channel.getFrom(), channel.getUntil(), LocalDate.now())) {
                        status = "S1";
                        break;
                    }
                }
                p.setStatus(status);

                var observations = butterfly.noise().getVibrationObservations().stream()
                        .filter(o -> o.getName().equalsIgnoreCase(p.getName()))
                        .collect(Collectors.toCollection(ArrayList::new));

                if (!observations.isEmpty()) {
                    p.ext().setDateFirst(observations.getFirst().getDate());
                    p.setDateLatest(observations.getLast().getDate());
                } else {
                    p.ext().setDateFirst(LocalDateTime.MIN);
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
        var timeFilteredItems = new ArrayList<BAcousticVibrationPoint>();

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

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BAcousticVibrationPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final VibrationManager INSTANCE = new VibrationManager();
    }
}
