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
package org.mapton.butterfly_topo_convergence.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.util.FastMath;
import org.mapton.api.MTemporalRange;
import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BBaseControlPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPointObservation;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo_convergence.group.ConvergenceGroupPropertiesBuilder;
import org.mapton.butterfly_topo_convergence.group.chart.ConvergenceGroupChartBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupManager extends BaseManager<BTopoConvergenceGroup> {

    private final ConvergenceGroupChartBuilder mChartBuilder = new ConvergenceGroupChartBuilder();
    private final ConvergenceGroupPropertiesBuilder mPropertiesBuilder = new ConvergenceGroupPropertiesBuilder();

    public static ConvergenceGroupManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceGroupManager() {
        super(BTopoConvergenceGroup.class);

        Mapton.getGlobalState().addListener(gsce -> {
            load2(gsce.getValue());
        }, TopoManager.KEY_TOPO_POINTS_LOADED);
    }

    public void add(BTopoConvergenceGroup group) {
        var butterfly = group.getButterfly();
        butterfly.topo().getConvergenceGroups().add(group);
        load2(butterfly);
    }

    @Override
    public Object getObjectChart(BTopoConvergenceGroup selectedObject) {
        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BTopoConvergenceGroup selectedObject) {
        if (selectedObject != null) {
            System.out.println(selectedObject.ext2().getAnchorPoint());
            selectedObject.ext2().getProjected2dCoordinates();
        }
        return mPropertiesBuilder.build(selectedObject);
    }

    public double getOffset() {
        var offset = 0d;
        for (var g : getTimeFilteredItems()) {
            var o = g.ext2().getControlPoints().stream()
                    .map(p -> p.getZeroZ())
                    .mapToDouble(Double::doubleValue).min().orElse(0);
            offset = FastMath.min(o, offset);
        }

        if (offset < 0) {
            offset = offset * -1.0;
        }

        offset += 2;

        return offset;
    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    public void load2(Butterfly butterfly) {
        try {
            initAllItems(butterfly.topo().getConvergenceGroups());
            initObjectToItemMap();

            for (var g : butterfly.topo().getConvergenceGroups()) {
                g.ext().getObservationsAllRaw().clear();
                var controlPoints = Arrays.stream(StringUtils.split(g.getRef(), ","))
                        .map(s -> butterfly.topo().getControlPointByName(s))
                        .filter(p -> p != null)
                        .filter(p -> ObjectUtils.allNotNull(p.getZeroX(), p.getZeroY(), p.getZeroZ()))
                        //.filter(p -> !StringUtils.endsWithIgnoreCase(p.getName(), "P00"))
                        .filter(p -> {
                            g.ext2().getObservationsAllRaw().addAll(p.ext().getObservationsAllRaw());
                            return true;
                        })
                        .collect(Collectors.toCollection(ArrayList::new));

                g.ext2().setControlPoints(controlPoints);
                var uniqueObservations = new ArrayList<BTopoControlPointObservation>();
                var dateSet = new HashSet<LocalDateTime>();
                for (var o : g.ext2().getObservationsAllRaw()) {
                    if (!dateSet.contains(o.getDate())) {
                        dateSet.add(o.getDate());
                        uniqueObservations.add(o);
                    }
                }
                uniqueObservations.sort(Comparator.comparing(BBaseControlPointObservation::getDate));
                g.ext().setObservationsAllRaw(uniqueObservations);
                g.ext2().setObservationsAllRaw(uniqueObservations);
            }

            for (var g : butterfly.topo().getConvergenceGroups()) {
                var maxPoint = g.ext2().getControlPointsWithoutAnchor().stream()
                        .max(Comparator.comparingDouble(BTopoControlPoint::getZeroZ));

                maxPoint.ifPresent(p -> {
                    g.setZeroX(p.getZeroX());
                    g.setZeroY(p.getZeroY());
                    g.setZeroZ(p.getZeroZ());
                    g.setLat(p.getLat());
                    g.setLon(p.getLon());
                });

                var observations = g.ext().getObservationsAllRaw();
                if (!observations.isEmpty()) {
                    g.ext().setDateFirst(observations.getFirst().getDate());
                    g.setDateLatest(observations.getLast().getDate());
                } else {
                    g.ext().setDateFirst(LocalDateTime.MIN);
                }

                g.ext().setDateLatest(g.getDateLatest());
//                g.ext().setObservationsAllRaw(observations);
                g.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(g));
                for (var o : g.ext().getObservationsAllRaw()) {
                    if (o.isZeroMeasurement()) {
                        g.ext().setStoredZeroDateTime(o.getDate());
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
        var timeFilteredItems = new ArrayList<BTopoConvergenceGroup>();

        p:
        for (var g : getFilteredItems()) {
            if (g.getDateLatest() == null || g.ext().getObservationsAllRaw().isEmpty()) {
                timeFilteredItems.add(g);
            } else {
                for (var o : g.ext().getObservationsAllRaw()) {
                    if (getTemporalManager().isValid(o.getDate())) {
                        timeFilteredItems.add(g);
                        continue p;
                    }
                }
            }
        }

        getTimeFilteredItemsMap().clear();
        timeFilteredItems.stream().forEach(g -> {
            getTimeFilteredItemsMap().put(g.getName(), g);
            var timeFilteredObservations = g.ext().getObservationsAllRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .collect(Collectors.toCollection(ArrayList::new));

            g.ext().setObservationsTimeFiltered(timeFilteredObservations);
            //g.ext().calculateObservations(timeFilteredObservations);
        });

        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BTopoConvergenceGroup> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final ConvergenceGroupManager INSTANCE = new ConvergenceGroupManager();
    }
}
