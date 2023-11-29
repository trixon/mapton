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
package org.mapton.butterfly_tmo.grundvatten;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.tmo.BGrundvatten;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GrundvattenLabelBy {
    NAME(LabelByCategories.ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelByCategories.ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    DATE_LATEST(LabelByCategories.DATE, SDict.LATEST.toString(), p -> {
        var date = p.ext().getObservationFilteredLastDate();
//        var date = p.getDateLatest();

        return date == null ? "-" : date.toString();
    }),
    //    DATE_ZERO(LabelByCategories.DATE, SDict.ZERO.toString(), p -> {
    //        var date = p.getDateZero();
    //
    //        return date == null ? "-" : date.toString();
    //    }),
    DATE_FIRST(LabelByCategories.DATE, Dict.FIRST.toString(), p -> {
//        try {
//            return p.ext().getObservationsTimeFiltered().getFirst().getDate().toLocalDate().toString();
//        } catch (Exception e) {
//            return "-";
//        }
        var date = p.ext().getObservationFilteredFirstDate();

        return date == null ? "-" : date.toString();
    }),
    MISC_DATE(LabelByCategories.MISC, Dict.DATE.toString(), p -> {
        var date = Objects.toString(DateHelper.toDateString(p.getInstallationsdatum()), "-");

        return date;
    }),
    MISC_AGE(LabelByCategories.MISC, Dict.AGE.toString(), p -> {
        return "?";
//        return String.valueOf(p.ext().getAge(ChronoUnit.DAYS));
    }),
    MISC_GROUP(LabelByCategories.MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_MARKNIVA(LabelByCategories.MISC, "Marknivå", p -> {
        var z = p.getMarknivå();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 2);
    }),
    MISC_Z(LabelByCategories.MISC, "Z", p -> {
        return "?";
//        return MathHelper.convertDoubleToString(p.getZ(), 1);
    });
    private final String mCategory;
    private final Function<BGrundvatten, String> mFunction;
    private final String mName;

    private GrundvattenLabelBy(String category, String name, Function<BGrundvatten, String> function) {
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

    public String getLabel(BGrundvatten o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

    }
}
