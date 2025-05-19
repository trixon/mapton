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
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum TopoLabelBy {
    NAME(LabelByCategories.ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelByCategories.ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_H_NAME(LabelByCategories.ALARM, Strings.HEIGHT_NAME, p -> {
        return p.getAlarm1Id();
    }),
    ALARM_H_VALUE(LabelByCategories.ALARM, Strings.HEIGHT_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
    }),
    ALARM_H_PERCENT(LabelByCategories.ALARM, Strings.HEIGHT_PERCENT, p -> {
        return p.ext().getAlarmPercentHString(p.ext());
    }),
    ALARM_P_NAME(LabelByCategories.ALARM, Strings.PLANE_NAME, p -> {
        return p.getAlarm2Id();
    }),
    ALARM_P_VALUE(LabelByCategories.ALARM, Strings.PLANE_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
    }),
    ALARM_P_PERCENT(LabelByCategories.ALARM, Strings.PLANE_PERCENT, p -> {
        return p.ext().getAlarmPercentPString(p.ext());
    }),
    ALARM_PERCENT(LabelByCategories.ALARM, "%", p -> {
        return p.ext().getAlarmPercentString(p.ext());
    }),
    DATE_LATEST(LabelByCategories.DATE, SDict.LATEST.toString(), p -> {
        var date = p.ext().getObservationFilteredLastDate();

        return date == null ? "-" : date.toString();
    }),
    DATE_NEXT(LabelByCategories.DATE, Dict.NEXT.toString(), p -> {
        var date = p.ext().getObservationRawNextDate();

        return date == null ? "-" : date.toString();
    }),
    DATE_ZERO(LabelByCategories.DATE, SDict.ZERO.toString(), p -> {
        var date = p.getDateZero();

        return date == null ? "-" : date.toString();
    }),
    DATE_FIRST(LabelByCategories.DATE, Dict.FIRST.toString(), p -> {
//        try {
//            return p.ext().getObservationsTimeFiltered().getFirst().getDate().toLocalDate().toString();
//        } catch (Exception e) {
//            return "-";
//        }
        var date = p.ext().getObservationFilteredFirstDate();

        return date == null ? "-" : date.toString();
    }),
    //    DATE_ROLLING(LabelByCategories.DATE, "rullande", o -> {
    //        var date = o.getDateRolling();
    //
    //        return date == null ? "-" : date.toString();
    //    }),
    DATE_VALIDITY(LabelByCategories.DATE, "%s - %s".formatted(Dict.FROM.toString(), Dict.TO.toString()), p -> {
        var d1 = p.getDateValidFrom();
        var d2 = p.getDateValidTo();
        if (ObjectUtils.allNull(d1, d2)) {
            return "";
        } else {
            var dat1 = d1 == null ? "" : d1.toString();
            var dat2 = d2 == null ? "" : d2.toString();

            return "%s - %s".formatted(dat1, dat2);
        }
    }),
    DATE_REACHED_LIMIT_VALUE(LabelByCategories.DATE, SDict.REACHED_LIMIT_VALUE.toString(), p -> {
        return p.ext().limitValuePredictor().getExtrapolatedLimitDate();
    }),
    MISC_GROUP(LabelByCategories.MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_CATEGORY(LabelByCategories.MISC, Dict.CATEGORY.toString(), p -> {
        return Objects.toString(p.getCategory(), "NODATA");
    }),
    MISC_STATUS(LabelByCategories.MISC, Dict.STATUS.toString(), p -> {
        return Objects.toString(p.getStatus(), "NODATA");
    }),
    MISC_OPERATOR(LabelByCategories.MISC, SDict.OPERATOR.toString(), p -> {
        return Objects.toString(p.getOperator(), "NODATA");
    }),
    MISC_ORIGIN(LabelByCategories.MISC, Dict.ORIGIN.toString(), p -> {
        return Objects.toString(p.getOrigin(), "NODATA");
    }),
    MISC_FREQUENCY(LabelByCategories.MISC, SDict.FREQUENCY.toString(), p -> {
        return p.getFrequency() != null ? p.getFrequency().toString() : "--";
    }),
    MISC_FREQUENCY_DEFAULT(LabelByCategories.MISC, "%s (%s)".formatted(SDict.FREQUENCY.toString(), Dict.DEFAULT.toLower()), p -> {
        return p.getDefaultFrequency().toString();
    }),
    MISC_FREQUENCY_AND_DEFAULT(LabelByCategories.MISC, "%s / %s".formatted(SDict.FREQUENCY.toString(), Dict.DEFAULT.toString()), p -> {
        var freq = p.getFrequency() != null ? p.getFrequency().toString() : "--";
        var def = p.getDefaultFrequency() != null ? p.getDefaultFrequency().toString() : "--";

        return "%s / %s".formatted(freq, def);
    }),
    MISC_DIMENS(LabelByCategories.MISC, SDict.DIMENSION.toString(), p -> {
        return p.getDimension() != null ? p.getDimension().getName() : "--";
    }),
    MISC_DIMENS_FREQUENCY(LabelByCategories.MISC, Strings.DIMENS_FREQ, p -> {
        return "%sD %s".formatted(MISC_DIMENS.getLabel(p), MISC_FREQUENCY.getLabel(p));
    }),
    MEAS_SPEED(LabelByCategories.MEAS, "%s (mm/%s)".formatted(Dict.SPEED.toString(), Dict.Time.YEAR.toLower()), p -> {
        if (p.getDimension() == BDimension._2d || p.ext().getObservationsTimeFiltered().size() < 2 || p.ext().deltaZero().getDelta1() == null) {
            return "-";
        } else {
            try {
                var speed = p.ext().getSpeed();
                var ageIndicator = p.ext().getMeasurementAge(ChronoUnit.DAYS) > 365 ? "*" : "";

                return "%.1f  (%.1f)%s".formatted(speed[0] * 1000.0, speed[1], ageIndicator);
//                return "%.1f mm/%s (%.1f)%s".formatted(speed[0] * 1000.0, Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
//                return "%.1f (%.1f)%s".formatted(speed[0], Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
            } catch (Exception e) {
                return "-";
            }
        }
    }),
    MEAS_LATEST_OPERATOR(LabelByCategories.MEAS, SDict.LATEST_S.toString().formatted(SDict.OPERATOR.toLower()), p -> {
        return p.ext().getObservationsAllRaw().getLast().getOperator();
    }),
    MEAS_COUNT_ALL(LabelByCategories.MEAS, Strings.MEAS_COUNT_ALL, p -> {
        return "%d".formatted(
                p.ext().getNumOfObservations()
        );
    }),
    MEAS_COUNT_SELECTION(LabelByCategories.MEAS, Strings.MEAS_COUNT_SELECTION, p -> {
        return "%d".formatted(
                p.ext().getNumOfObservationsFiltered()
        );
    }),
    MEAS_COUNT_SELECTION_ALL(LabelByCategories.MEAS, Strings.MEAS_COUNT, p -> {
        return "%d / %d".formatted(
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations()
        );
    }),
    MEAS_AGE(LabelByCategories.MEAS, Dict.AGE.toString(), p -> {
        var daysSinceMeasurement = p.ext().getMeasurementAge(ChronoUnit.DAYS);

        return "%d".formatted(daysSinceMeasurement);
    }),
    MEAS_AGE_ZERO(LabelByCategories.MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ZERO.toLower()), p -> {
        var daysSinceMeasurement = p.ext().getZeroMeasurementAge(ChronoUnit.DAYS);

        return "%d".formatted(daysSinceMeasurement);
    }),
    MEAS_AGE_ALARMLEVEL(LabelByCategories.MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower()), p -> {
        return p.ext().getAlarmLevelAge();
    }),
    MEAS_NEED(LabelByCategories.MEAS, Dict.NEED.toString(), p -> {
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        return need;
    }),
    MEAS_NEED_FREQ(LabelByCategories.MEAS, "%s (%s)".formatted(Dict.NEED.toString(), SDict.FREQUENCY.toString()), p -> {
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        var freq = p.getFrequency() != null ? p.getFrequency().toString() : "--";

        return "%s (%s)".formatted(need, freq);
    }),
    VALUE_Z(LabelByCategories.VALUE, "Z", p -> {
        var z = p.getZeroZ();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 3);
    }),
    VALUE_DELTA_ZERO(LabelByCategories.VALUE, "Δ₀", p -> {
        return p.ext().deltaZero().getDelta(3);
    }),
    VALUE_DELTA_ZERO_Z(LabelByCategories.VALUE, "ΔZ₀", p -> {
        var daysSinceMeasurement = p.ext().getZeroMeasurementAge(ChronoUnit.DAYS);

        return "%s (%d)".formatted(p.ext().deltaZero().getDelta1(3), daysSinceMeasurement);
    }),
    VALUE_DELTA_LATEST_Z(LabelByCategories.VALUE, "ΔZ (dagar)", p -> {
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
    VALUE_DELTA_LATEST_Z_ZERO(LabelByCategories.VALUE, "ΔZ (ΔZ₀)", p -> {
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
    VALUE_DELTA_ROLLING(LabelByCategories.VALUE, "Δᵣ", p -> {
        return p.ext().deltaRolling().getDelta(3);
    }),
    VALUE_DELTA_ROLLING_Z(LabelByCategories.VALUE, "ΔZᵣ", p -> {
        return p.ext().deltaRolling().getDelta1(3);
    });
    private final String mCategory;
    private final Function<BTopoControlPoint, String> mFunction;
    private final String mName;

    private TopoLabelBy(String category, String name, Function<BTopoControlPoint, String> function) {
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

    public String getLabel(BTopoControlPoint o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String HEIGHT_NAME = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.NAME.toLower());
        public static final String HEIGHT_PERCENT = "%s, %%".formatted(Dict.Geometry.HEIGHT);
        public static final String HEIGHT_VALUE = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.VALUE.toLower());
        public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
        public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
        public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());
        public static final String PLANE_NAME = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.NAME.toLower());
        public static final String PLANE_PERCENT = "%s, %%".formatted(Dict.Geometry.PLANE);
        public static final String PLANE_VALUE = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.VALUE.toLower());
        public static final String DIMENS_FREQ = "%s & %s".formatted(SDict.DIMENSION.toString(), SDict.FREQUENCY.toString());

    }
}
