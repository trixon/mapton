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
package org.mapton.butterfly_acoustic.measuring_point;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum MeasPointLabelBy {
    NAME(Strings.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(Strings.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MISC_GROUP(Strings.CAT_MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_TYPE(Strings.CAT_MISC, Dict.CATEGORY.toString(), p -> {
        return Objects.toString(p.getCategory(), "NODATA");
    }),
    //    MISC_SOIL(Strings.CAT_MISC, NbBundle.getMessage(MeasPointLabelBy.class, "soilMaterial"), p -> {
    //        return Objects.toString(p.getSoilMaterial(), "NODATA");
    //    }),
    MISC_Z(Strings.CAT_MISC, "Z", p -> {
        return MathHelper.convertDoubleToString(p.getZeroZ(), 1);
    });
    private final String mCategory;
    private final Function<BAcousticVibrationPoint, String> mFunction;
    private final String mName;

    private MeasPointLabelBy(String category, String name, Function<BAcousticVibrationPoint, String> function) {
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

    public String getLabel(BAcousticVibrationPoint o) {
        try {
            return mFunction.apply(o);
        } catch (Exception e) {
            return "ERROR %s <<<<<<<<".formatted(o.getName());
        }
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String CAT_MISC = Dict.MISCELLANEOUS.toString();
        public static final String CAT_ROOT = "";

    }
}
