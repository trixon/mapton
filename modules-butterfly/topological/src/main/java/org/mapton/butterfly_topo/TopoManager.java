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

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.MTemporalRange;
import org.openide.util.Exceptions;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import org.mapton.butterfly_api.api.BaseManager;

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
        var timeFilteredItems = getFilteredItems().stream()
                .filter(o -> o.getDateLatest() == null ? true : getTemporalManager().isValid(o.getDateLatest()))
                .toList();

        timeFilteredItems.stream().forEach(p -> {
            var timefilteredObservations = p.ext().getObservationsRaw().stream()
                    .filter(o -> getTemporalManager().isValid(o.getDate()))
                    .toList();
            p.ext().setObservationsCalculated(new ArrayList<>(timefilteredObservations));
//            System.out.println(timefilteredObservations.size());
            if (false && !timefilteredObservations.isEmpty()) {
                var latestZero = timefilteredObservations.stream()
                        .filter(o -> o.isZeroMeasurement())
                        .reduce((first, second) -> second).get();

                if (latestZero == null) {
                    latestZero = timefilteredObservations.getFirst();
                }

                Double zX = latestZero.getMeasuredX();
                Double zY = latestZero.getMeasuredY();
                Double zZ = latestZero.getMeasuredZ();

                for (int i = 0; i < timefilteredObservations.size(); i++) {
                    var o = timefilteredObservations.get(i);
//                    var prev = timefilteredObservations.get(i - 1);
                    Double x = o.getMeasuredX();
                    Double y = o.getMeasuredY();
                    Double z = o.getMeasuredZ();

                    if (ObjectUtils.allNotNull(x, zX)) {
                        o.ext().setDeltaX(x - zX);
                    }
                    if (ObjectUtils.allNotNull(y, zY)) {
                        o.ext().setDeltaY(y - zY);
                    }
                    if (ObjectUtils.allNotNull(z, zY)) {
                        o.ext().setDeltaY(y - zY);
                    }

                }

            }

            var replacements = timefilteredObservations.stream()
                    .filter(o -> o.isReplacementMeasurement())
                    .toList();

            //TODO: Recalculate deltas
            /*
ta reda på senaste noll
            ta reda på alla ersättningar
             */
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
