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
package org.mapton.butterfly_topo;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_api.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPointObservation;
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
                p.ext().setObservationsRaw(nameToTopoControlPointObservations.get(p.getName()));
                for (var o : p.ext().getObservationsRaw()) {
                    o.ext().setControlPoint(p);
                }
            }

            var dates = new TreeSet<>(getAllItems().stream()
                    .map(o -> o.getDateLatest())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }
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
                for (var o : p.ext().getObservationsRaw()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(p);
                        continue p;
                    }
                }
            }
        }

        timeFilteredItems.stream().forEach(p -> {
            var timefilteredObservations = p.ext().getObservationsRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .toList();
            p.ext().setObservationsFiltered(new ArrayList<>(timefilteredObservations));

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);

            if (!timefilteredObservations.isEmpty()) {
                var latestZero = timefilteredObservations.stream()
                        .filter(o -> o.isZeroMeasurement())
                        .reduce((first, second) -> second).orElse(timefilteredObservations.getFirst());

                Double zX = latestZero.getMeasuredX();
                Double zY = latestZero.getMeasuredY();
                Double zZ = latestZero.getMeasuredZ();
                var rX = 0.0;
                var rY = 0.0;
                var rZ = 0.0;

                for (int i = 0; i < timefilteredObservations.size(); i++) {
                    var o = timefilteredObservations.get(i);
                    CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
                    BTopoControlPointObservation prev = null;
                    if (i > 0) {
                        prev = timefilteredObservations.get(i - 1);
                    }
                    Double x = o.getMeasuredX();
                    Double y = o.getMeasuredY();
                    Double z = o.getMeasuredZ();

                    if (ObjectUtils.allNotNull(x, zX)) {
                        o.ext().setDeltaX(x - zX);
                    }
                    if (ObjectUtils.allNotNull(y, zY)) {
                        o.ext().setDeltaY(y - zY);
                    }
                    if (ObjectUtils.allNotNull(z, zZ)) {
                        o.ext().setDeltaZ(z - zZ);
                    }

                    if (o.isReplacementMeasurement() && prev != null) {
                        var mX = o.getMeasuredX();
                        var pX = prev.getMeasuredX();
                        if (ObjectUtils.allNotNull(mX, pX)) {
                            rX = rX + mX - pX;
                            o.ext().setDeltaX(o.ext().getDeltaX() + rX);
                        }

                        var mY = o.getMeasuredY();
                        var pY = prev.getMeasuredY();
                        if (ObjectUtils.allNotNull(mY, pY)) {
                            rY = rY + mY - pY;
                            o.ext().setDeltaY(o.ext().getDeltaY() + rY);
                        }

                        var mZ = o.getMeasuredZ();
                        var pZ = prev.getMeasuredZ();
                        if (ObjectUtils.allNotNull(mZ, pZ)) {
                            rZ = rZ + mZ - pZ;
                            o.ext().setDeltaZ(o.ext().getDeltaZ() + rZ);
                        }
                    }
                }
            }
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
