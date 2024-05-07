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
package org.mapton.butterfly_geo_extensometer;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPoint;
import org.mapton.butterfly_format.types.geo.BGeoExtensometerPointObservation;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoManager extends BaseManager<BGeoExtensometer> {

    private final ExtensoChartBuilder mChartBuilder = new ExtensoChartBuilder();
    private final ExtensoPropertiesBuilder mPropertiesBuilder = new ExtensoPropertiesBuilder();
    private double mMinimumZscaled = 0.0;

    public static ExtensoManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExtensoManager() {
        super(BGeoExtensometer.class);
        initListeners();
    }

    @Override
    public Object getObjectChart(BGeoExtensometer selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BGeoExtensometer selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            var geotechnical = butterfly.geotechnical();
            var extensometers = geotechnical.getExtensometers();
            var extensometersPoints = geotechnical.getExtensometersPoints();
            var extensometersPointsObservations = geotechnical.getExtensometersPointsObservations();

            initAllItems(extensometers);
            initObjectToItemMap();

            var nameToPoint = extensometersPoints.stream().collect(Collectors.toMap(BGeoExtensometerPoint::getName, Function.identity()));
            var nameToObservations = new LinkedHashMap<String, ArrayList<BGeoExtensometerPointObservation>>();
            for (var o : extensometersPointsObservations) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }

            extensometers.forEach(ext -> {
                for (var pointName : StringUtils.split(ext.getSensors(), ",")) {
                    var p = nameToPoint.get(pointName);
                    ext.getPoints().add(p);

                    var observations = nameToObservations.getOrDefault(pointName, new ArrayList<>());
                    if (!observations.isEmpty()) {
                        p.setDateLatest(observations.getLast().getDate());
                    }

                    p.ext().setDateLatest(p.getDateLatest());
                    p.ext().setObservationsAllRaw(observations);
                    p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));
                    var dateLatest = ext.getPoints().stream().map(pp -> pp.getDateLatest()).max(Comparator.naturalOrder()).orElse(null);

                    ext.setDateLatest(dateLatest);
                    for (var o : p.ext().getObservationsAllRaw()) {
                        if (o.isZeroMeasurement()) {
                            p.ext().setStoredZeroDateTime(o.getDate());
                            break;
                        }
                    }
                }
            });

            var dates = new TreeSet<LocalDateTime>();
            extensometers.forEach(ext -> {
                ext.getPoints().forEach(p -> {
                    dates.addAll(p.ext().getObservationsAllRaw().stream().map(o -> o.getDate()).toList());
                });
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
        var timeFilteredItems = new ArrayList<BGeoExtensometer>();

        p:
        for (var extenso : getFilteredItems()) {
            if (extenso.getDateLatest() == null || extenso.ext().hasNoObservations()) {
                timeFilteredItems.add(extenso);
            } else {
                for (var point : extenso.getPoints()) {
                    for (var o : point.ext().getObservationsAllRaw()) {
                        if (getTemporalManager().isValid(o.getDate())) {
                            timeFilteredItems.add(extenso);
                            continue p;
                        }
                    }
                }
            }
        }

        getTimeFilteredItemsMap().clear();

        timeFilteredItems.stream().forEach(ext -> {
            getTimeFilteredItemsMap().put(ext.getName(), ext);
            for (var p : ext.getPoints()) {
                var timeFilteredObservations = p.ext().getObservationsAllRaw().stream()
                        .filter(o -> getTemporalManager().isValid(o.getDate()))
                        .collect(Collectors.toCollection(ArrayList::new));

                p.ext().setObservationsTimeFiltered(timeFilteredObservations);
                p.ext().calculateObservations(timeFilteredObservations);
            }
        });

        mMinimumZscaled = Double.MAX_VALUE;
        for (var p : timeFilteredItems) {
            try {
                mMinimumZscaled = FastMath.min(mMinimumZscaled, p.getZeroZ());
                //mMinimumZscaled = FastMath.min(mMinimumZscaled, p.getZeroZ() + TopoLayerBundle.SCALE_FACTOR_Z * p.ext().deltaZero().getDelta());
            } catch (Exception e) {
            }
        }

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BGeoExtensometer> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initListeners() {
    }

    private static class Holder {

        private static final ExtensoManager INSTANCE = new ExtensoManager();
    }
}
