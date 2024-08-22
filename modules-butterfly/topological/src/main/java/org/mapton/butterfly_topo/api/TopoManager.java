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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.math3.util.FastMath;
import org.mapton.api.MSimpleObjectStorageManager;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_topo.TopoChartBuilder;
import org.mapton.butterfly_topo.TopoPropertiesBuilder;
import org.mapton.butterfly_topo.sos.ScalePlot3dHSosd;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoManager extends BaseManager<BTopoControlPoint> {

    private final TopoChartBuilder mChartBuilder = new TopoChartBuilder();
    private double mMinimumZscaled = 0.0;
    private final TopoPropertiesBuilder mPropertiesBuilder = new TopoPropertiesBuilder();

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
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BTopoControlPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
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
                    .collect(Collectors.toCollection(ArrayList::new));

            p.ext().setObservationsTimeFiltered(timeFilteredObservations);
            p.ext().calculateObservations(timeFilteredObservations);

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);
            timeFilteredObservations.forEach(o -> {
                CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
            });
        });

        var mScale3dH = MSimpleObjectStorageManager.getInstance().getInteger(ScalePlot3dHSosd.class, 500);

        mMinimumZscaled = Double.MAX_VALUE;
        for (var p : timeFilteredItems) {
            try {
                mMinimumZscaled = FastMath.min(mMinimumZscaled, p.getZeroZ() + mScale3dH * p.ext().deltaZero().getDeltaZ());
            } catch (Exception e) {
                //nvm
            }
        }

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
