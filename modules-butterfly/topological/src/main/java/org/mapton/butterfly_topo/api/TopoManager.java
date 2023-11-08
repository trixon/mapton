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

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_api.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPointObservation;
import org.mapton.butterfly_topo.TopoChartBuilder;
import org.mapton.butterfly_topo.TopoPropertiesBuilder;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoManager extends BaseManager<BTopoControlPoint> {

    private final TopoChartBuilder mChartBuilder = new TopoChartBuilder();
    private final TopoPropertiesBuilder mPropertiesBuilder = new TopoPropertiesBuilder();

    public static TopoManager getInstance() {
        return TopoManagerHolder.INSTANCE;
    }

    private TopoManager() {
        super(BTopoControlPoint.class);
    }

    @Override
    public Object getObjectChart(BTopoControlPoint selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BTopoControlPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
        for (var item : getAllItems()) {
            getAllItemsMap().put(item.getName(), item);
        }
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getTopoControlPoints());
            initObjectToItemMap();

            var nameToTopoControlPointObservations = new LinkedHashMap<String, ArrayList<BTopoControlPointObservation>>();
            for (var o : butterfly.getTopoControlPointsObservations()) {
                nameToTopoControlPointObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            for (var p : butterfly.getTopoControlPoints()) {
                p.ext().setObservationsAllRaw(nameToTopoControlPointObservations.get(p.getName()));
                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setControlPoint(p));
            }

            var dates = new TreeSet<>(getAllItems().stream()
                    .map(o -> o.getDateLatest())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }

            getAllItems().stream().forEach(p -> {
                var calculatedObservations = new ArrayList<>(p.ext().getObservationsAllRaw());
                p.ext().setObservationsAllCalculated(calculatedObservations);
                p.ext().calculateObservations(calculatedObservations);
            });

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        var measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = new ArrayList<BTopoControlPoint>();
        p:
        for (var p : getFilteredItems()) {
            if (p.getDateLatest() == null) {
                timeFilteredItems.add(p);
            } else {
                for (var o : p.ext().getObservationsAllCalculated()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(p);
                        continue p;
                    }
                }
            }
        }

        timeFilteredItems.stream().forEach(p -> {
            var timefilteredObservations = p.ext().getObservationsAllRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .toList();
            p.ext().setObservationsTimeFiltered(new ArrayList<>(timefilteredObservations));

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            p.ext().calculateObservations(timefilteredObservations);
            timefilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BTopoControlPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class TopoManagerHolder {

        private static final TopoManager INSTANCE = new TopoManager();
    }
}
