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
package org.mapton.butterfly_hydro.groundwater;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.controlpoint.BHydroControlPoint;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class GroundwaterManager extends BaseManager<BHydroControlPoint> {

    private final GroundwaterPropertiesBuilder mPropertiesBuilder = new GroundwaterPropertiesBuilder();

    public static GroundwaterManager getInstance() {
        return Holder.INSTANCE;
    }

    private GroundwaterManager() {
        super(BHydroControlPoint.class);
    }

    @Override
    public Object getObjectProperties(BHydroControlPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getHydroControlPoints());

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
        //TODO Is never measure valid or invalid?
        var timeFilteredItems = getFilteredItems().stream()
                .filter(o -> o.getDateLatest() == null ? true : getTemporalManager().isValid(o.getDateLatest()))
                .toList();

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BHydroControlPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final GroundwaterManager INSTANCE = new GroundwaterManager();
    }
}
