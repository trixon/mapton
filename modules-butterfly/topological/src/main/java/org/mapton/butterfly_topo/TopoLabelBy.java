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
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

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
        return p.getNameOfAlarmHeight();
    }),
    ALARM_H_VALUE(LabelByCategories.ALARM, Strings.HEIGHT_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
    }),
    ALARM_P_NAME(LabelByCategories.ALARM, Strings.PLANE_NAME, p -> {
        return p.getNameOfAlarmPlane();
    }),
    ALARM_P_VALUE(LabelByCategories.ALARM, Strings.PLANE_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
    }),
    DATE_LATEST(LabelByCategories.DATE, SDict.LATEST.toString(), p -> {
        var date = p.ext().getObservationFilteredLastDate();
//        var date = p.getDateLatest();

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
    MISC_FREQUENCY(LabelByCategories.MISC, SDict.FREQUENCY.toString(), p -> {
        return p.getFrequency() != null ? p.getFrequency().toString() : "--";
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
        return "%d".formatted(
                p.ext().getMeasurementAge(ChronoUnit.DAYS)
        );
    }),
    MEAS_NEED(LabelByCategories.MEAS, Dict.NEED.toString(), p -> {
        return "%d".formatted(
                p.ext().getMeasurementUntilNext(ChronoUnit.DAYS)
        );
    }),
    VALUE_Z(LabelByCategories.VALUE, "Z", p -> {
        var z = p.getZeroZ();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 3);
    }),
    VALUE_DELTA_ZERO(LabelByCategories.VALUE, "Δ₀", p -> {
        return p.ext().deltaZero().getDelta(3);
    }),
    VALUE_DELTA_ZERO_Z(LabelByCategories.VALUE, "ΔZ₀", p -> {
        return p.ext().deltaZero().getDelta1(3);
    }),
    VALUE_DELTA_ROLLING(LabelByCategories.VALUE, "Δᵣ", p -> {
        return p.ext().deltaRolling().getDelta(3);
    }),
    VALUE_DELTA_ROLLING_Z(LabelByCategories.VALUE, "ΔZᵣ", p -> {
        return p.ext().deltaRolling().getDelta1(3);
    }),
    ZZZ("Z", "z", p -> {
        return "Z";
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
        public static final String HEIGHT_VALUE = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.VALUE.toLower());
        public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
        public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
        public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());
        public static final String PLANE_NAME = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.NAME.toLower());
        public static final String PLANE_VALUE = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.VALUE.toLower());

    }
}
