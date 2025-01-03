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

import org.mapton.butterfly_core.api.GraphicRenderItemLimitProvider;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GraphicRendererItem implements GraphicRenderItemLimitProvider {
    GROUP_DEFORMATION("Grupp, deformation", Integer.MAX_VALUE),
    BEARING(Dict.BEARING.toString(), Integer.MAX_VALUE),
    CIRCLE_1D("1d Delta", Integer.MAX_VALUE),
    CIRCLE_3D("3d Delta", Integer.MAX_VALUE),
    TRACE_1D(SDict.TRACE_1D.toString(), 10_000),
    VECTOR_1D(SDict.VECTOR_1D.toString(), 100),
    VECTOR_1D_ALARM("%s (%s)".formatted(SDict.VECTOR_1D.toString(), SDict.ALARM_LEVEL.toLower()), 100),
    //    TRACE_2D(SDict.TRACE_2D.toString()),
    //    VECTOR_2D(SDict.VECTOR_2D.toString()),
    TRACE_3D(SDict.TRACE_3D.toString(), Integer.MAX_VALUE),
    VECTOR_3D(SDict.VECTOR_3D.toString(), Integer.MAX_VALUE),
    MEASUREMENTS(Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower()), Integer.MAX_VALUE),
    MEASUREMENTS_PER_MONTH(Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower() + "/" + Dict.Time.MONTH.toLower()), 10_000),
    SPEED_1D("1d %s".formatted(Dict.SPEED.toLower()), Integer.MAX_VALUE),
    SPEED_1D_TRACE("1d %s (%s)".formatted(Dict.SPEED.toLower(), SDict.TRACE.toLower()), Integer.MAX_VALUE),
    ALARM_LEVEL(SDict.ALARM_LEVEL.toString(), Integer.MAX_VALUE),
    ALARM_CONSUMPTION("Larmförbrukning", Integer.MAX_VALUE),
    TRACE_ALARM_LEVEL("%s (%s)".formatted(SDict.ALARM_LEVEL.toString(), SDict.TRACE.toLower()), Integer.MAX_VALUE);
    private final String mName;
    private final int mPlotLimit;

    private GraphicRendererItem(String name, int plotLimit) {
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
