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
package org.mapton.butterfly_tmo.vaderstation;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.tmo.BVaderstation;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum VaderstationLabelBy {
    NAME(Strings.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(Strings.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MISC_DATE(Strings.CAT_MISC, Dict.DATE.toString(), p -> {
        var date = Objects.toString(DateHelper.toDateString(p.getInstallationsdatum()), "-");

        return date;
    }),
    MISC_AGE(Strings.CAT_MISC, Dict.AGE.toString(), p -> {
        return "?";
//        return String.valueOf(p.ext().getAge(ChronoUnit.DAYS));
    }),
    MISC_GROUP(Strings.CAT_MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_Z(Strings.CAT_MISC, "Z", p -> {
        return "?";
//        return MathHelper.convertDoubleToString(p.getZ(), 1);
    });
    private final String mCategory;
    private final Function<BVaderstation, String> mFunction;
    private final String mName;

    private VaderstationLabelBy(String category, String name, Function<BVaderstation, String> function) {
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

    public String getLabel(BVaderstation o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String CAT_MISC = Dict.MISCELLANEOUS.toString();
        public static final String CAT_ROOT = "";

    }
}
