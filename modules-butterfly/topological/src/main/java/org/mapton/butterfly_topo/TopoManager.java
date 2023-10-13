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
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_api.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.openide.util.Exceptions;
import se.trixon.almond.util.CollectionHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoManager extends BaseManager<BTopoControlPoint> {

    private final TopoPropertiesBuilder mTopoPropertiesBuilder = new TopoPropertiesBuilder();

    public static TopoManager getInstance() {
        return TopoManagerHolder.INSTANCE;
    }

    private TopoManager() {
        super(BTopoControlPoint.class);
    }

    @Override
    public Object getObjectProperties(BTopoControlPoint selectedObject) {
        return mTopoPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getTopoControlPoints());

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
        //TODO Is never measured valid or invalid?
        DateTimeFormatter measCountStatsDateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM");
        var timeFilteredItems = getFilteredItems().stream()
                .filter(o -> o.getDateLatest() == null ? true : getTemporalManager().isValid(o.getDateLatest()))
                .toList();

        timeFilteredItems.stream().forEach(p -> {
            var timefilteredObservations = p.ext().getObservationsRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .toList();
            p.ext().setObservationsCalculated(new ArrayList<>(timefilteredObservations));

            var measCountStats = new LinkedHashMap<String, Integer>();
            p.ext().setMeasurementCountStats(measCountStats);

            if (!timefilteredObservations.isEmpty()) {
                for (int i = 0; i < timefilteredObservations.size(); i++) {
                    var o = timefilteredObservations.get(i);
                    CollectionHelper.incInteger(measCountStats, o.getDate().format(measCountStatsDateTimeFormatter));
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
