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
package org.mapton.butterfly_meteo;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.BMeteoPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum MeteoLabelBy implements LabelBy.Operations {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    DATE_LATEST(LabelBy.CAT_DATE, SDict.LATEST.toString(), p -> {
        return LabelBy.dateLatest(p);
    }),
    DATE_LATEST_WITH_TIME(LabelBy.CAT_DATE, SDict.LATEST.toString() + " (med tid)", p -> {
        return LabelBy.dateLatestWithTime(p);
    }),
    DATE_FIRST(LabelBy.CAT_DATE, Dict.FIRST.toString(), p -> {
        return LabelBy.dateFirst(p);
    }),
    MISC_GROUP(LabelBy.CAT_MISC, Dict.GROUP.toString(), p -> {
        return LabelBy.miscGroup(p);
    }),
    MISC_CATEGORY(LabelBy.CAT_MISC, Dict.CATEGORY.toString(), p -> {
        return LabelBy.miscCategory(p);
    }),
    MISC_STATUS(LabelBy.CAT_MISC, Dict.STATUS.toString(), p -> {
        return LabelBy.miscStatus(p);
    }),
    MISC_OPERATOR(LabelBy.CAT_MISC, SDict.OPERATOR.toString(), p -> {
        return LabelBy.miscOperator(p);
    }),
    MISC_ORIGIN(LabelBy.CAT_MISC, Dict.ORIGIN.toString(), p -> {
        return LabelBy.miscOrigin(p);
    }),
    MEAS_COUNT_ALL(LabelBy.CAT_MEAS, LabelBy.MEAS_COUNT_ALL, p -> {
        return LabelBy.measCountAll(p);
    }),
    MEAS_COUNT_SELECTION(LabelBy.CAT_MEAS, LabelBy.MEAS_COUNT_SELECTION, p -> {
        return LabelBy.measCountFiltered(p);
    }),
    MEAS_COUNT_SELECTION_ALL(LabelBy.CAT_MEAS, LabelBy.MEAS_COUNT, p -> {
        return LabelBy.measCountFilteredAll(p);
    }),
    MEAS_AGE(LabelBy.CAT_MEAS, Dict.AGE.toString(), p -> {
        return LabelBy.measAge(p);
    }),
    VALUE_TEMPERATURE(LabelBy.CAT_VALUE, "Temperatur", p -> {
        try {
            var o = p.ext().getObservationFilteredLast();
            var value = "%+.0f °C @".formatted(o.getAirTemperature());
            var pattern = "yyyy-MM-dd";
            if (o.getDate().toLocalDate().isEqual(LocalDate.now())) {
                pattern = "HH.mm";
            } else {

            }

            return value + o.getDate().format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            //nvm
        }

        return "";
    }),
    VALUE_Z(LabelBy.CAT_VALUE, "Z", p -> {
        return LabelBy.valueZeroZ(p);
    });
    private final String mCategory;
    private final Function<BMeteoPoint, String> mFunction;
    private final String mName;

    private MeteoLabelBy(String category, String name, Function<BMeteoPoint, String> function) {
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

    public String getLabel(BMeteoPoint o) {
        return mFunction.apply(o);
    }

    @Override
    public String getName() {
        return mName;
    }
}
