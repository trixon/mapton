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
package org.mapton.butterfly_tmo.api;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.tmo.BRorelse;
import org.mapton.butterfly_format.types.tmo.BRorelseObservation;
import org.mapton.butterfly_tmo.rorelse.RorelsePropertiesBuilder;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class RorelseManager extends BaseManager<BRorelse> {

    private final RorelsePropertiesBuilder mPropertiesBuilder = new RorelsePropertiesBuilder();

    public static RorelseManager getInstance() {
        return Holder.INSTANCE;
    }

    private RorelseManager() {
        super(BRorelse.class);
    }

    @Override
    public Object getObjectProperties(BRorelse selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.tmo().getRorelse());
            initObjectToItemMap();

            var nameToObservations = new LinkedHashMap<String, ArrayList<BRorelseObservation>>();
            for (var o : butterfly.tmo().getRorelseObservations()) {
                nameToObservations.computeIfAbsent(o.getName(), k -> new ArrayList<>()).add(o);
            }
            for (var p : butterfly.tmo().getRorelse()) {
                var observations = nameToObservations.getOrDefault(p.getName(), new ArrayList<>());
                if (!observations.isEmpty()) {
                    p.ext().setDateLatest(observations.getLast().getDate());
                }

//                p.ext().setDateLatest(p.getDateLatest());
                p.ext().setObservationsAllRaw(observations);
                p.ext().getObservationsAllRaw().forEach(o -> o.ext().setParent(p));
                for (var o : p.ext().getObservationsAllRaw()) {
//                    if (o.isZeroMeasurement()) {
//                        p.ext().setStoredZeroDateTime(o.getDate());
//                        break;
//                    }
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

////            var dates = new TreeSet<>(getAllItems().stream()
////                    .map(p -> p.getInstallationsdatum())
////                    .filter(d -> d != null)
////                    .collect(Collectors.toSet()));
//            var dates = new TreeSet<LocalDateTime>();
//            getAllItems().stream().forEachOrdered(p -> {
//                dates.addAll(p.ext().getObservationsAllRaw().stream().map(o -> o.getDate()).toList());
//            });
//
//            if (!dates.isEmpty()) {
//                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
//            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
//        var timeFilteredItems = getFilteredItems().stream()
//                .filter(o -> o.getDateTime() == null ? true : getTemporalManager().isValid(o.getDateTime()))
//                .toList();

        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BRorelse> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final RorelseManager INSTANCE = new RorelseManager();
    }
}
