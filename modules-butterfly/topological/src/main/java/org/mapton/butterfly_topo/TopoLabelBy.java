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
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.controlpoint.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum TopoLabelBy {
    NAME(Strings.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(Strings.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_H_NAME(Strings.CAT_ALARM, Strings.HEIGHT_NAME, p -> {
        return p.getNameOfAlarmHeight();
    }),
    ALARM_H_VALUE(Strings.CAT_ALARM, Strings.HEIGHT_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
    }),
    ALARM_P_NAME(Strings.CAT_ALARM, Strings.PLANE_NAME, p -> {
        return p.getNameOfAlarmPlane();
    }),
    ALARM_P_VALUE(Strings.CAT_ALARM, Strings.PLANE_VALUE, p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
    }),
    DATE_LATEST(Strings.CAT_DATE, SDict.LATEST.toString(), p -> {
        var date = p.getDateLatest();

        return date == null ? "-" : date.toString();
    }),
    DATE_FIRST(Strings.CAT_DATE, Dict.FIRST.toString(), p -> {
        var date = p.getDateZero();

        return date == null ? "-" : date.toString();
    }),
    //    DATE_ROLLING(Strings.CAT_DATE, "rullande", o -> {
    //        var date = o.getDateRolling();
    //
    //        return date == null ? "-" : date.toString();
    //    }),
    DATE_VALIDITY(Strings.CAT_DATE, "%s - %s".formatted(Dict.FROM.toString(), Dict.TO.toString()), p -> {
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
    MISC_GROUP(Strings.CAT_MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_CATEGORY(Strings.CAT_MISC, Dict.CATEGORY.toString(), p -> {
        return Objects.toString(p.getCategory(), "NODATA");
    }),
    MISC_STATUS(Strings.CAT_MISC, Dict.STATUS.toString(), p -> {
        return Objects.toString(p.getStatus(), "NODATA");
    }),
    MISC_OPERATOR(Strings.CAT_MISC, SDict.OPERATOR.toString(), p -> {
        return Objects.toString(p.getOperator(), "NODATA");
    }),
    MISC_FREQUENCY(Strings.CAT_MISC, SDict.FREQUENCY.toString(), p -> {
        return p.getFrequency() != null ? p.getFrequency().toString() : "--";
    }),
    MEAS_LATEST_OPERATOR(Strings.CAT_MEAS, SDict.LATEST_S.toString().formatted(SDict.OPERATOR.toLower()), p -> {
        return p.ext().getObservationsRaw().getLast().getOperator();
    }),
    MEAS_COUNT_ALL(Strings.CAT_MEAS, Strings.MEAS_COUNT_ALL, p -> {
        return "%d".formatted(
                p.ext().getNumOfObservations()
        );
    }),
    MEAS_COUNT_SELECTION(Strings.CAT_MEAS, Strings.MEAS_COUNT_SELECTION, p -> {
        return "%d".formatted(
                p.ext().getNumOfObservationsTimeFiltered()
        );
    }),
    MEAS_COUNT_SELECTION_ALL(Strings.CAT_MEAS, Strings.MEAS_COUNT, p -> {
        return "%d / %d".formatted(
                p.ext().getNumOfObservationsTimeFiltered(),
                p.ext().getNumOfObservations()
        );
    }),
    MEAS_AGE(Strings.CAT_MEAS, Dict.AGE.toString(), p -> {
        return "%d".formatted(
                p.ext().getMeasurementAge(ChronoUnit.DAYS)
        );
    }),
    MEAS_NEED(Strings.CAT_MEAS, Dict.NEED.toString(), p -> {
        return "%d".formatted(
                p.ext().getMeasurementUntilNext(ChronoUnit.DAYS)
        );
    }),
    VALUE_Z(Strings.CAT_VALUE, "Z", p -> {
        var z = p.getZeroZ();
        return z == null ? "-" : "%+.3f".formatted(z);
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

    public String getLabel(BTopoControlPoint o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String CAT_ALARM = SDict.ALARM.toString();
        public static final String CAT_DATE = Dict.DATE.toString();
        public static final String CAT_DELTA = "Delta";
        public static final String CAT_MEAS = SDict.MEASUREMENTS.toString();
        public static final String CAT_MISC = Dict.MISCELLANEOUS.toString();
        public static final String CAT_ROOT = "";
        public static final String CAT_VALUE = Dict.VALUE.toString();
        public static final String HEIGHT_NAME = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.NAME.toLower());
        public static final String HEIGHT_VALUE = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.VALUE.toLower());
        public static final String PLANE_NAME = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.NAME.toLower());
        public static final String PLANE_VALUE = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.VALUE.toLower());
        public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
        public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
        public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());

    }
}
