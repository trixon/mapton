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
package org.mapton.butterfly_topo.grade.vertical.graphics;

import org.mapton.butterfly_topo.grade.horizontal.*;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum GradeVRendererItem {
    HOR_INDICATOR("H %s".formatted(Dict.INDICATORS.toLower()), Integer.MAX_VALUE),
    VER_INDICATOR("V %s".formatted(Dict.INDICATORS.toLower()), Integer.MAX_VALUE),
    NAME(Dict.NAME.toString(), Integer.MAX_VALUE);
    private final String mName;
    private final int mPlotLimit;

    private GradeVRendererItem(String name, int plotLimit) {
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
