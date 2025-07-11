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
package org.mapton.butterfly_topo.graphics;

import org.mapton.butterfly_core.api.GraphicRenderItemLimitProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GraphicItem implements GraphicRenderItemLimitProvider {
    LABEL("\t%s".formatted(Dict.LABEL.toString()), Integer.MAX_VALUE),
    PIN(Dict.PIN.toString(), Integer.MAX_VALUE),
    CLUSTER_DEFORMATION("Kluster, deformation", Integer.MAX_VALUE),
    CLUSTER_DEFORMATION_PLANE_ALTITUDES("Kluster, deformation plan på olika höjder", Integer.MAX_VALUE),
    CLUSTER_DEFORMATION_PLANE_ALTITUDES_LABEL("\tEtikett: Avstånd och bäring", Integer.MAX_VALUE),
    //CLUSTER_DEFORMATION_TIN("Kluster, yta", Integer.MAX_VALUE),
    BEARING(Dict.BEARING.toString(), Integer.MAX_VALUE),
    CIRCLE_1D("1d-delta", Integer.MAX_VALUE),
    CIRCLE_2D("2d-delta", Integer.MAX_VALUE),
    CIRCLE_3D("3d-delta", Integer.MAX_VALUE),
    CIRCLE_VERTICAL_DIRECTION("Färgskala, vertikalrörelse", Integer.MAX_VALUE),
    TRACE_1D(SDict.TRACE_1D.toString(), 10_000),
    VECTOR_1D(SDict.VECTOR_1D.toString(), 100),
    VECTOR_1D_ALARM("\t— %s (%s)".formatted(SDict.VECTOR_1D.toString(), SDict.ALARM_LEVEL.toLower()), 100),
    VECTOR_1D_LABEL("\t— %s (%s)".formatted(SDict.VECTOR_1D.toString(), Dict.LABEL.toLower()), 100),
    VECTOR_1D_TREND("\t— %s (%s)".formatted(SDict.VECTOR_1D.toString(), Dict.TRENDS.toLower()), 100),
    //    TRACE_2D(SDict.TRACE_2D.toString()),
    //    VECTOR_2D(SDict.VECTOR_2D.toString()),
    TRACE_3D(SDict.TRACE_3D.toString(), Integer.MAX_VALUE),
    VECTOR_3D(SDict.VECTOR_3D.toString(), Integer.MAX_VALUE),
    MEASUREMENTS(Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower()), Integer.MAX_VALUE),
    MEASUREMENTS_PER_MONTH(Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower() + "/" + Dict.Time.MONTH.toLower()), 10_000),
    ALARM_LEVEL(SDict.ALARM_LEVEL.toString(), Integer.MAX_VALUE),
    ALARM_CONSUMPTION("Larmförbrukning", Integer.MAX_VALUE),
    ALARM_CONSUMPTION_TRACE_1("\t— spår höjd", Integer.MAX_VALUE),
    ALARM_CONSUMPTION_TRACE_2("\t— spår plan", Integer.MAX_VALUE),
    TRACE_ALARM_LEVEL("%s (%s)".formatted(SDict.ALARM_LEVEL.toString(), SDict.TRACE.toLower()), Integer.MAX_VALUE),
    TREND_1D_PIE("Trend, 1d (paj)", Integer.MAX_VALUE),
    TREND_1D_STACK("Trend, 1d (stapel)", Integer.MAX_VALUE),
    TREND_2D_PIE("Trend, 2d (paj)", Integer.MAX_VALUE),
    TREND_2D_STACK("Trend, 2d (intervall)", Integer.MAX_VALUE),
    FREQ_BUFFER("Frekvens, hög (buffer)", Integer.MAX_VALUE);
    private final String mName;
    private final int mPlotLimit;

    private GraphicItem(String name, int plotLimit) {
        mName = name;
        mPlotLimit = plotLimit;
    }

    @Override
    public String getName() {
        return mName;
    }

    @Override
    public int getPlotLimit() {
        return mPlotLimit;
    }

    @Override
    public String toString() {
        return getName();
    }
}
