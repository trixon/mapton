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
package org.mapton.butterfly_tmo.grundvatten;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import org.mapton.butterfly_format.types.tmo.BGrundvattenObservation;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenManager extends BaseManager<BGrundvatten> {

    private final GrundvattenPropertiesBuilder mPropertiesBuilder = new GrundvattenPropertiesBuilder();

    public static GrundvattenManager getInstance() {
        return Holder.INSTANCE;
    }

    private GrundvattenManager() {
        super(BGrundvatten.class);
    }

    @Override
    public Object getObjectProperties(BGrundvatten selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.tmo().getGrundvatten());
            initObjectToItemMap();

            var nameToObservations = new LinkedHashMap<String, ArrayList<BGrundvattenObservation>>();
            for (var o : butterfly.tmo().getGrundvattenObservations()) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            for (var p : butterfly.tmo().getGrundvatten()) {
                p.ext().setObservationsAllRaw(nameToObservations.get(p.getName()));
                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));

                var grundvattenObservations = p.ext().getObservationsAllRaw();
                if (!grundvattenObservations.isEmpty()) {
                    p.ext().setDataLatest(grundvattenObservations.getFirst().getDate());
                }
            }

            var dates = new TreeSet<>(getAllItems().stream()
                    .map(p -> p.ext().getDateLatest())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }

            getAllItems().stream().forEach(p -> {
                var calculatedObservations = new ArrayList<>(p.ext().getObservationsAllRaw());
                p.ext().setObservationsAllCalculated(calculatedObservations);
                //TODO or not TODO? p.ext().calculateObservations(calculatedObservations);
            });

        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        var measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = new ArrayList<BGrundvatten>();

        p:
        for (var p : getFilteredItems()) {
            if (p.ext().getDateLatest() == null) {
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
            //p.ext().calculateObservations(timefilteredObservations);
            timefilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BGrundvatten> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GrundvattenManager INSTANCE = new GrundvattenManager();
    }
}
