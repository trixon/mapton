/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.pair.horizontal;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GradeHLabelBy {
    NAME(LabelByCategories.ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelByCategories.ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MEAS_GRADE_H(LabelByCategories.MEAS, "GRADE H per mille", p -> {
        return MathHelper.convertDoubleToString(p.ext().getDiff().getZPerMille(), 1);
    });
    private final String mCategory;
    private final Function<BTopoGrade, String> mFunction;
    private final String mName;

    private GradeHLabelBy(String category, String name, Function<BTopoGrade, String> function) {
        mCategory = category;
        mName = name;
        mFunction = function;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getFullName() {
        if (StringUtils.isBlank(mCategory)) {
            return mName;
        } else {
            return "%s/%s".formatted(mCategory, mName);
        }
    }

    public String getLabel(BTopoGrade o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String HEIGHT_NAME = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.NAME.toLower());
        public static final String HEIGHT_VALUE = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.VALUE.toLower());
        public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
        public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
        public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());
        public static final String PLANE_NAME = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.NAME.toLower());
        public static final String PLANE_VALUE = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.VALUE.toLower());

    }

}
