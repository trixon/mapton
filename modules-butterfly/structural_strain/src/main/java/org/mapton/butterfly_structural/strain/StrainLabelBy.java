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
package org.mapton.butterfly_structural.strain;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum StrainLabelBy implements LabelBy.Operations {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_NAME(LabelBy.CAT_ALARM, Dict.NAME.toString(), p -> {
        return LabelBy.alarmHName(p);
    }),
    ALARM_VALUE(LabelBy.CAT_ALARM, Dict.VALUE.toString(), p -> {
        return LabelBy.alarmHValue(p);
    }),
    ALARM_PERCENT(LabelBy.CAT_ALARM, "%", p -> {
        return LabelBy.alarmHPercent(p);
    }),
    DATE_NEXT(LabelBy.CAT_DATE, Dict.NEXT.toString(), p -> {
        return LabelBy.dateNext(p);
    }),
    DATE_LATEST(LabelBy.CAT_DATE, SDict.LATEST.toString(), p -> {
        return LabelBy.dateLatest(p);
    }),
    DATE_ROLLING(LabelBy.CAT_DATE, SDict.ROLLING.toString(), p -> {
        return LabelBy.dateRolling(p);
    }),
    DATE_ZERO(LabelBy.CAT_DATE, SDict.ZERO.toString(), p -> {
        return LabelBy.dateZero(p);
    }),
    DATE_FIRST(LabelBy.CAT_DATE, Dict.FIRST.toString(), p -> {
        return LabelBy.dateFirst(p);
    }),
    DATE_VALIDITY(LabelBy.CAT_DATE, "%s - %s".formatted(Dict.FROM.toString(), Dict.TO.toString()), p -> {
        return LabelBy.dateValidity(p);
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
    MISC_FREQUENCY(LabelBy.CAT_MISC, SDict.FREQUENCY.toString(), p -> {
        return LabelBy.miscFrequency(p);
    }),
    MISC_FREQUENCY_ALL(LabelBy.CAT_MISC, "%s (aktuell : %s : %s)".formatted(SDict.FREQUENCY.toString(), Dict.DEFAULT.toLower(), Dict.HIGH.toLower()), p -> {
        return LabelBy.miscFrequencyAll(p);
    }),
    MISC_FREQUENCY_DEFAULT(LabelBy.CAT_MISC, "%s (%s)".formatted(SDict.FREQUENCY.toString(), Dict.DEFAULT.toLower()), p -> {
        return LabelBy.miscFrequencyDefault(p);
    }),
    MISC_FREQUENCY_HIGH(LabelBy.CAT_MISC, "%s (%s)".formatted(SDict.FREQUENCY.toString(), Dict.HIGH.toLower()), p -> {
        return LabelBy.miscFrequencyHigh(p);
    }),
    MISC_FREQUENCY_HIGH_PARAM(LabelBy.CAT_MISC, "%s (%s)".formatted(SDict.FREQUENCY.toString(), "%s, villkor".formatted(Dict.HIGH.toLower())), p -> {
        return LabelBy.miscFrequencyHighParam(p);
    }),
    MISC_DIMENS(LabelBy.CAT_MISC, SDict.DIMENSION.toString(), p -> {
        return LabelBy.miscDimens(p);
    }),
    MISC_DIMENS_FREQUENCY(LabelBy.CAT_MISC, LabelBy.DIMENS_FREQ, p -> {
        return "%sD %s".formatted(MISC_DIMENS.getLabel(p), MISC_FREQUENCY.getLabel(p));
    }),
    MISC_ROLLING_FORMULA(LabelBy.CAT_MISC, "Formel, rullande", p -> {
        return LabelBy.miscRollingFormula(p);
    }),
    MISC_ROLLING_SPARSE(LabelBy.CAT_MISC, "Formel, utglesning", p -> {
        return LabelBy.miscSparse(p);
    }),
    MEAS_LATEST_OPERATOR(LabelBy.CAT_MEAS, SDict.LATEST_S.toString().formatted(SDict.OPERATOR.toLower()), p -> {
        return LabelBy.measLatestOperator(p);
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
    MEAS_AGE_ZERO(LabelBy.CAT_MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ZERO.toLower()), p -> {
        return LabelBy.measAgeZero(p);
    }),
    MEAS_AGE_ALARMLEVEL(LabelBy.CAT_MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower()), p -> {
        return LabelBy.measAgeAlarmLevel(p);
    }),
    MEAS_NEED(LabelBy.CAT_MEAS, Dict.NEED.toString(), p -> {
        return LabelBy.measNeed(p);
    }),
    MEAS_NEED_FREQ(LabelBy.CAT_MEAS, "%s (%s)".formatted(Dict.NEED.toString(), SDict.FREQUENCY.toString()), p -> {
        return LabelBy.measNeedFreq(p);
    }),
    VALUE_DELTA_ZERO(LabelBy.CAT_VALUE, "Δ₀", p -> {
        return p.ext().getDeltaZero();
    }),
    VALUE_DELTA_ZERO_Z(LabelBy.CAT_VALUE, "ΔR₀", p -> {
        var deltaRAbsolute = p.ext().deltaZero().getDeltaZAbsolute(1);
        return StringUtils.replace(deltaRAbsolute, "Z", "R");
    });
    private final String mCategory;
    private final Function<BStructuralStrainGaugePoint, String> mFunction;
    private final String mName;

    private StrainLabelBy(String category, String name, Function<BStructuralStrainGaugePoint, String> function) {
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

    public String getLabel(BStructuralStrainGaugePoint o) {
        try {
            return mFunction.apply(o);
        } catch (Exception e) {
            return "ERROR %s <<<<<<<<".formatted(o.getName());
        }
    }

    @Override
    public String getName() {
        return mName;
    }

}
