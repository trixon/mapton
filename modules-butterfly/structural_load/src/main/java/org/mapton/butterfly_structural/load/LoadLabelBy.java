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
package org.mapton.butterfly_structural.load;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum LoadLabelBy {
    NAME(Strings.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(Strings.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_NAME(LabelByCategories.ALARM, Dict.NAME.toString(), p -> {
        return p.getAlarm1Id();
    }),
    ALARM_VALUE(LabelByCategories.ALARM, Dict.VALUE.toString(), p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
    }),
    ALARM_PERCENT(LabelByCategories.ALARM, "%", p -> {
        return p.ext().getAlarmPercentHString(p.ext());
    }),
    DATE_LATEST(LabelByCategories.DATE, SDict.LATEST.toString(), p -> {
        var date = p.ext().getObservationFilteredLastDate();

        return date == null ? "-" : date.toString();
    }),
    //    DATE_NEXT(LabelByCategories.DATE, Dict.NEXT.toString(), p -> {
    //        var date = p.ext().getObservationRawNextDate();
    //
    //        return date == null ? "-" : date.toString();
    //    }),
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
    //    DATE_REACHED_LIMIT_VALUE(LabelByCategories.DATE, SDict.REACHED_LIMIT_VALUE.toString(), p -> {
    //        return p.ext().limitValuePredictor().getExtrapolatedLimitDate();
    //    }),
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
    MEAS_COUNT_ALL(LabelByCategories.MEAS, "TopoLabelBy.Strings.MEAS_COUNT_ALL", p -> {
        return "%d".formatted(
                p.ext().getNumOfObservations()
        );
    }),
    MEAS_COUNT_SELECTION(LabelByCategories.MEAS, "TopoLabelBy.Strings.MEAS_COUNT_SELECTION", p -> {
        return "%d".formatted(
                p.ext().getNumOfObservationsFiltered()
        );
    }),
    MEAS_COUNT_SELECTION_ALL(LabelByCategories.MEAS, "TopoLabelBy.Strings.MEAS_COUNT", p -> {
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
//        var daysSinceMeasurement = p.ext().getZeroMeasurementAge(ChronoUnit.DAYS);
        var daysSinceMeasurement = 666;

        return "%d".formatted(daysSinceMeasurement);
    }),
    MEAS_AGE_ALARMLEVEL(LabelByCategories.MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower()), p -> {
//        return p.ext().getAlarmLevelAge();
        return "666";
    }),
    MEAS_NEED(LabelByCategories.MEAS, Dict.NEED.toString(), p -> {
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        return need;
    }),
    VALUE_DELTA_ZERO(LabelByCategories.VALUE, Dict.VALUE.toString(), p -> {
        return "%.0f".formatted(p.ext().getObservationFilteredLast().getMeasuredZ());
    });
    private final String mCategory;
    private final Function<BStructuralLoadCellPoint, String> mFunction;
    private final String mName;

    private LoadLabelBy(String category, String name, Function<BStructuralLoadCellPoint, String> function) {
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

    public String getLabel(BStructuralLoadCellPoint o) {
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
