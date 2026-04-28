/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_structural.tilt.chart;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_core.api.BKey;
import org.mapton.butterfly_core.api.BMultiChartPart;
import org.mapton.butterfly_core.api.BMultiChartPartCluster;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_structural.tilt.TiltManager;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = BMultiChartPart.class)
public class ClusterMultiChartPart extends BMultiChartPartCluster {

    private final Function<BXyzPointObservation, Double> mFunction = o -> o.ext().getDeltaZ();

    public ClusterMultiChartPart() {
    }

    @Override
    public String getAxisLabel() {
        return "mm/m";
    }

    @Override
    public String getDecimalPattern() {
        return "0.0";
    }

    @Override
    public BaseManager getManager() {
        return TiltManager.getInstance();
    }

    @Override
    public String getName() {
        return "Lutningsgivare";
    }

    @Override
    public ArrayList<BStructuralTiltPoint> getPoints(MLatLon latLon, LocalDate firstDate, LocalDate date, LocalDate lastDate) {
        var pointList = TiltManager.getInstance().getTimeFilteredItems().stream()
                .filter(p -> {
                    return hasValidGeometry(latLon, BCoordinatrix.toLatLon(p), getDefaultDistance());
                })
                .filter(p -> {
                    var observationCount = p.ext().getObservationsTimeFiltered().stream()
                            .filter(o -> mFunction.apply(o) != null)
                            .count();
                    if (observationCount > 1) {
                        p.setValue(BKey.CLUSTER_CHART_FUNCTION, mFunction);
                        return true;
                    } else {
                        return false;
                    }
                })
                .collect(Collectors.toCollection(ArrayList::new));
        return pointList;
    }
}
