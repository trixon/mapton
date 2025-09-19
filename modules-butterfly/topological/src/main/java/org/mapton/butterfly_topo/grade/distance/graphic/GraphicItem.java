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
package org.mapton.butterfly_topo.grade.distance.graphic;

import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GraphicItem {
    POINTS(SDict.POINTS.toString(), Integer.MAX_VALUE),
    VECTOR_3D(SDict.VECTOR_3D.toString(), Integer.MAX_VALUE),
    VECTOR_3D_CONNECTOR("\t— anslutning", Integer.MAX_VALUE),
    TRACE_1D(SDict.TRACE_1D.toString(), 10_000),
    VERTICAL_INDICATOR("D %s".formatted(Dict.INDICATORS.toLower()), Integer.MAX_VALUE),
    NAME(Dict.NAME.toString(), Integer.MAX_VALUE);
    private final String mName;
    private final int mPlotLimit;

    private GraphicItem(String name, int plotLimit) {
        mName = name;
        mPlotLimit = plotLimit;
    }

    public String getName() {
        return mName;
    }

    public int getPlotLimit() {
        return mPlotLimit;
    }

    @Override
    public String toString() {
        return getName();
    }
}
