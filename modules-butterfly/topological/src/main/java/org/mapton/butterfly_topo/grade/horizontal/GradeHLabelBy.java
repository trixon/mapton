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
package org.mapton.butterfly_topo.grade.horizontal;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.topo.BTopoGrade;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum GradeHLabelBy implements LabelBy.Operations {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MEAS_GRADE_H(LabelBy.CAT_MEAS, "mm/m", p -> {
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

    @Override
    public String getCategory() {
        return mCategory;
    }

    @Override
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

    @Override
    public String getName() {
        return mName;
    }
}
