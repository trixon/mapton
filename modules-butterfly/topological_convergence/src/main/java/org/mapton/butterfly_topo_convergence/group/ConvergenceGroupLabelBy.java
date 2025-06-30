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
package org.mapton.butterfly_topo_convergence.group;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum ConvergenceGroupLabelBy {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_H_NAME(LabelBy.CAT_ALARM, LabelBy.HEIGHT_NAME, p -> {
        return LabelBy.alarmHName(p);
    }),
    ALARM_H_VALUE(LabelBy.CAT_ALARM, LabelBy.HEIGHT_VALUE, p -> {
        return LabelBy.alarmHValue(p);
    }),
    ALARM_P_NAME(LabelBy.CAT_ALARM, LabelBy.PLANE_NAME, p -> {
        return LabelBy.alarmPName(p);
    }),
    ALARM_P_VALUE(LabelBy.CAT_ALARM, LabelBy.PLANE_VALUE, p -> {
        return LabelBy.alarmPValue(p);
    }),
    DATE_LATEST(LabelBy.CAT_DATE, SDict.LATEST.toString(), p -> {
        return LabelBy.dateLatest(p);
    }),
    DATE_NEXT(LabelBy.CAT_DATE, Dict.NEXT.toString(), p -> {
        return LabelBy.dateNext(p);
    }),
    DATE_ZERO(LabelBy.CAT_DATE, SDict.ZERO.toString(), p -> {
        return LabelBy.dateZero(p);
    }),
    DATE_FIRST(LabelBy.CAT_DATE, Dict.FIRST.toString(), p -> {
        return LabelBy.dateFirst(p);
    }),
    //    DATE_ROLLING(LabelBy.CAT_DATE, "rullande", o -> {
    //        var date = o.getDateRolling();
    //
    //        return date == null ? "-" : date.toString();
    //    }),
    DATE_VALIDITY(LabelBy.CAT_DATE, "%s - %s".formatted(Dict.FROM.toString(), Dict.TO.toString()), p -> {
        return LabelBy.dateValidity(p);
    }),
    MISC_POINT_COUNT(LabelBy.CAT_MISC, "Punktantal", p -> {
        return Objects.toString(p.ext2().getControlPoints().size(), "-");
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
    //    MEAS_SPEED(LabelBy.CAT_MEAS, "%s (mm/%s)".formatted(Dict.SPEED.toString(), Dict.Time.YEAR.toLower()), p -> {
    //        if (p.getDimension() == BDimension._2d || p.ext().getObservationsTimeFiltered().size() < 2 || p.ext().deltaZero().getDelta1() == null) {
    //            return "-";
    //        } else {
    //            try {
    //                var speed = p.ext().getSpeed();
    //                var ageIndicator = p.ext().getMeasurementAge(ChronoUnit.DAYS) > 365 ? "*" : "";
    //
    //                return "%.1f  (%.1f)%s".formatted(speed[0] * 1000.0, speed[1], ageIndicator);
    ////                return "%.1f mm/%s (%.1f)%s".formatted(speed[0] * 1000.0, Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
////                return "%.1f (%.1f)%s".formatted(speed[0], Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
//            } catch (Exception e) {
//                return "-";
//            }
//        }
//    }),
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
    });
    private final String mCategory;
    private final Function<BTopoConvergenceGroup, String> mFunction;
    private final String mName;

    private ConvergenceGroupLabelBy(String category, String name, Function<BTopoConvergenceGroup, String> function) {
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

    public String getLabel(BTopoConvergenceGroup o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

}
