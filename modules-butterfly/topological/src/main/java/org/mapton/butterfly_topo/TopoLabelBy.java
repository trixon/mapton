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
package org.mapton.butterfly_topo;

import java.time.temporal.ChronoUnit;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BTrendPeriod;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum TopoLabelBy implements LabelBy.Operations {
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
    ALARM_H_PERCENT(LabelBy.CAT_ALARM, LabelBy.HEIGHT_PERCENT, p -> {
        return LabelBy.alarmHPercent(p);
    }),
    ALARM_H_DIFFERENTIAL(LabelBy.CAT_ALARM, LabelBy.HEIGHT_DIFFERENTIAL, p -> {
        return LabelBy.alarmDifferential(p, BComponent.HEIGHT);
    }),
    ALARM_H_DIFFERENTIAL_2(LabelBy.CAT_ALARM, LabelBy.HEIGHT_DIFFERENTIAL + " (%s)".formatted("gräns"), p -> {
        return LabelBy.alarmDifferential(p, BComponent.HEIGHT, 2);
    }),
    ALARM_P_NAME(LabelBy.CAT_ALARM, LabelBy.PLANE_NAME, p -> {
        return LabelBy.alarmPName(p);
    }),
    ALARM_P_VALUE(LabelBy.CAT_ALARM, LabelBy.PLANE_VALUE, p -> {
        return LabelBy.alarmPValue(p);
    }),
    ALARM_P_PERCENT(LabelBy.CAT_ALARM, LabelBy.PLANE_PERCENT, p -> {
        return LabelBy.alarmPPercent(p);
    }),
    ALARM_P_DIFFERENTIAL(LabelBy.CAT_ALARM, LabelBy.PLANE_DIFFERENTIAL, p -> {
        return LabelBy.alarmDifferential(p, BComponent.PLANE);
    }),
    ALARM_PERCENT(LabelBy.CAT_ALARM, "%", p -> {
        return LabelBy.alarmPercent(p);
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
    DATE_ALARM_LEVEL_CHANGED(LabelBy.CAT_DATE, SDict.ALARM_LEVEL.toString(), p -> {

        return LabelBy.dateAlarmLevelChange(p);
    }),
    DATE_ALARM_LEVEL_CHANGED_DAYS(LabelBy.CAT_DATE, SDict.ALARM_LEVEL.toString() + " (dagar)", p -> {

        return LabelBy.dateAlarmLevelChangeDays(p);
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
    VALUE_DELTA_FIRST(LabelBy.CAT_VALUE, "@Första Δ", p -> {
        return p.ext().deltaFirst().getDelta1d2d(0, 1000);
    }),
    VALUE_DELTA_FIRST_1D(LabelBy.CAT_VALUE, "@Första Δ1d", p -> {
        return p.ext().deltaFirst().getDelta1(0, 1000, false);
    }),
    VALUE_DELTA_FIRST_1D_DAYS(LabelBy.CAT_VALUE, "@Första Δ1d (dagar)", p -> {
        var daysSinceMeasurement = p.ext().getFirstMeasurementAge(ChronoUnit.DAYS);

        return "%s (%d)".formatted(p.ext().deltaFirst().getDelta1(0, 1000, false), daysSinceMeasurement);
    }),
    VALUE_DELTA_FIRST_2D(LabelBy.CAT_VALUE, "@Första Δ2d", p -> {
        return p.ext().deltaFirst().getDelta2(0, 1000, false);
    }),
    VALUE_DELTA_FIRST_2D_DAYS(LabelBy.CAT_VALUE, "@Första Δ2d (dagar)", p -> {
        var daysSinceMeasurement = p.ext().getFirstMeasurementAge(ChronoUnit.DAYS);

        return "%s (%d)".formatted(p.ext().deltaFirst().getDelta2(0, 1000, false), daysSinceMeasurement);
    }),
    VALUE_DELTA_ZERO(LabelBy.CAT_VALUE, "@Noll Δ", p -> {
        return p.ext().deltaZero().getDelta1d2d(0, 1000);
    }),
    VALUE_DELTA_ZERO_1D(LabelBy.CAT_VALUE, "@Noll Δ1d", p -> {
        return p.ext().deltaZero().getDelta1(0, 1000, false);
    }),
    VALUE_DELTA_ZERO_1D_DAYS(LabelBy.CAT_VALUE, "@Noll Δ1d (dagar)", p -> {
        var daysSinceMeasurement = p.ext().getZeroMeasurementAge(ChronoUnit.DAYS);

        return "%s (%d)".formatted(p.ext().deltaZero().getDelta1(0, 1000, false), daysSinceMeasurement);
    }),
    VALUE_DELTA_ZERO_2D(LabelBy.CAT_VALUE, "@Noll Δ2d", p -> {
        return p.ext().deltaZero().getDelta2(0, 1000, false);
    }),
    VALUE_DELTA_ZERO_2D_DAYS(LabelBy.CAT_VALUE, "@Noll Δ2d (dagar)", p -> {
        var daysSinceMeasurement = p.ext().getZeroMeasurementAge(ChronoUnit.DAYS);

        return "%s (%d)".formatted(p.ext().deltaZero().getDelta2(0, 1000, false), daysSinceMeasurement);
    }),
    VALUE_DELTA_ROLLING(LabelBy.CAT_VALUE, "@Rullande Δ", p -> {
        return p.ext().deltaRolling().getDelta1d2d(3);
    }),
    VALUE_DELTA_ROLLING_1D(LabelBy.CAT_VALUE, "@Rullande Δ1d", p -> {
        return p.ext().deltaRolling().getDelta1(3);
    }),
    VALUE_DELTA_ROLLING_2D(LabelBy.CAT_VALUE, "@Rullande Δ2d", p -> {
        return p.ext().deltaRolling().getDelta2(3);
    }),
    VALUE_DELTA_LATEST_1D(LabelBy.CAT_VALUE, "@2 senaste Δ1d (dagar)", p -> {
        if (p.getDimension() == BDimension._2d) {
            return ":";
        }
        long daysSinceMeasurement;
        var observations = p.ext().getObservationsTimeFiltered();
        double delta;
        if (observations.size() > 1) {
            var secondLast = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            var lastDelta = last.ext().getDeltaZ();
            var secondLastDelta = secondLast.ext().getDeltaZ();
            if (ObjectUtils.anyNull(secondLastDelta, lastDelta)) {
                return "-";
            }

            delta = lastDelta - secondLastDelta;
            daysSinceMeasurement = ChronoUnit.DAYS.between(secondLast.getDate(), last.getDate());
        } else {
            return "-";
        }

        return "%s (%d)".formatted(StringHelper.round(delta, 3), daysSinceMeasurement);
    }),
    VALUE_DELTA_LATEST_1D_ZERO(LabelBy.CAT_VALUE, "@2 senaste Δ1d (Δ1d₀)", p -> {
        if (p.getDimension() == BDimension._2d) {
            return ":";
        }
        var observations = p.ext().getObservationsTimeFiltered();
        double delta;
        if (observations.size() > 1) {
            var secondLast = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            var lastDelta = last.ext().getDeltaZ();
            var secondLastDelta = secondLast.ext().getDeltaZ();
            if (ObjectUtils.anyNull(secondLastDelta, lastDelta)) {
                return "-";
            }

            delta = lastDelta - secondLastDelta;
        } else {
            return "-";
        }

        return "%s (%.3f)".formatted(StringHelper.round(delta, 3), p.ext().deltaZero().getDelta1());
    }),
    VALUE_DELTA_LATEST_2D(LabelBy.CAT_VALUE, "@2 senaste Δ2d (dagar)", p -> {
        if (p.getDimension() == BDimension._1d) {
            return ":";
        }
        long daysSinceMeasurement;
        var observations = p.ext().getObservationsTimeFiltered();
        double delta;
        if (observations.size() > 1) {
            var secondLast = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            var lastDelta = last.ext().getDelta2d();
            var secondLastDelta = secondLast.ext().getDelta2d();
            if (ObjectUtils.anyNull(secondLastDelta, lastDelta)) {
                return "-";
            }

            delta = lastDelta - secondLastDelta;
            daysSinceMeasurement = ChronoUnit.DAYS.between(secondLast.getDate(), last.getDate());
        } else {
            return "-";
        }

        return "%s (%d)".formatted(StringHelper.round(delta, 3), daysSinceMeasurement);
    }),
    VALUE_DELTA_LATEST_2D_ZERO(LabelBy.CAT_VALUE, "@2 senaste Δ2d (Δ2d₀)", p -> {
        if (p.getDimension() == BDimension._1d) {
            return ":";
        }
        var observations = p.ext().getObservationsTimeFiltered();
        double delta;
        if (observations.size() > 1) {
            var secondLast = observations.get(observations.size() - 2);
            var last = observations.get(observations.size() - 1);
            var lastDelta = last.ext().getDelta2d();
            var secondLastDelta = secondLast.ext().getDelta2d();
            if (ObjectUtils.anyNull(secondLastDelta, lastDelta)) {
                return "-";
            }

            delta = lastDelta - secondLastDelta;
        } else {
            return "-";
        }

        return "%s (%.3f)".formatted(StringHelper.round(delta, 3), p.ext().deltaZero().getDelta2());
    }),
    VALUE_Z(LabelBy.CAT_VALUE, "Z", p -> {
        return LabelBy.valueZeroZ(p);
    }),
    TREND_1D_1W(LabelBy.CAT_TREND, "1d " + BTrendPeriod.WEEK.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.WEEK, BComponent.HEIGHT);
    }),
    TREND_1D_1M(LabelBy.CAT_TREND, "1d " + BTrendPeriod.MONTH.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.MONTH, BComponent.HEIGHT);
    }),
    TREND_1D_3M(LabelBy.CAT_TREND, "1d " + BTrendPeriod.QUARTER.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.QUARTER, BComponent.HEIGHT);
    }),
    TREND_1D_6M(LabelBy.CAT_TREND, "1d " + BTrendPeriod.HALF_YEAR.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.HALF_YEAR, BComponent.HEIGHT);
    }),
    TREND_1D_1Y(LabelBy.CAT_TREND, "1d " + BTrendPeriod.YEAR.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.YEAR, BComponent.HEIGHT);
    }),
    TREND_1D_Z(LabelBy.CAT_TREND, "1d " + BTrendPeriod.ZERO.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.ZERO, BComponent.HEIGHT);
    }),
    TREND_1D_F(LabelBy.CAT_TREND, "1d " + BTrendPeriod.FIRST.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.FIRST, BComponent.HEIGHT);
    }),
    TREND_2D_1W(LabelBy.CAT_TREND, "2d " + BTrendPeriod.WEEK.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.WEEK, BComponent.PLANE);
    }),
    TREND_2D_1M(LabelBy.CAT_TREND, "2d " + BTrendPeriod.MONTH.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.MONTH, BComponent.PLANE);
    }),
    TREND_2D_3M(LabelBy.CAT_TREND, "2d " + BTrendPeriod.QUARTER.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.QUARTER, BComponent.PLANE);
    }),
    TREND_2D_6M(LabelBy.CAT_TREND, "2d " + BTrendPeriod.HALF_YEAR.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.HALF_YEAR, BComponent.PLANE);
    }),
    TREND_2D_1Y(LabelBy.CAT_TREND, "2d " + BTrendPeriod.YEAR.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.YEAR, BComponent.PLANE);
    }),
    TREND_2D_Z(LabelBy.CAT_TREND, "2d " + BTrendPeriod.ZERO.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.ZERO, BComponent.PLANE);
    }),
    TREND_2D_F(LabelBy.CAT_TREND, "2d " + BTrendPeriod.FIRST.getTitle(), p -> {
        return LabelBy.trend(p, BTrendPeriod.FIRST, BComponent.PLANE);
    });

    private final String mCategory;
    private final Function<BTopoControlPoint, String> mFunction;
    private final String mName;

    private TopoLabelBy(String category, String name, Function<BTopoControlPoint, String> function) {
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

    public String getLabel(BTopoControlPoint o) {
        return mFunction.apply(o);
    }

    @Override
    public String getName() {
        return mName;
    }

}
