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
package org.mapton.butterfly_hydro.groundwater;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.hydro.BHydroGroundwaterPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;

/**
 *
 * @author Patrik Karlström
 */
public enum GroundwaterLabelBy {
    NAME(LabelByCategories.ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelByCategories.ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    ALARM_H_NAME(LabelByCategories.ALARM, Dict.NAME.toString(), p -> {
        return p.getAlarm1Id();
    }),
    ALARM_H_VALUE(LabelByCategories.ALARM, Dict.VALUE.toString(), p -> {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
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
    DATE_FIRST(LabelByCategories.DATE, Dict.FIRST.toString(), p -> {
        var date = p.ext().getObservationFilteredFirstDate();

        return date == null ? "-" : date.toString();
    }),
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
    MISC_ORIGIN(LabelByCategories.MISC, Dict.ORIGIN.toString(), p -> {
        return Objects.toString(p.getOrigin(), "NODATA");
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
        var daysSinceMeasurement = p.ext().getMeasurementAge(ChronoUnit.DAYS);

        return "%d".formatted(daysSinceMeasurement);
    }),
    MEAS_NEED(LabelByCategories.MEAS, Dict.NEED.toString(), p -> {
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));

        return need;
    }),
    VALUE_LEVEL_CHANGE_30(LabelByCategories.VALUE, "Nivåförändring, 30 dagar", p -> {
        var z = p.ext().getGroundwaterLevelDiff(30);

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 2);
    }),
    VALUE_LEVEL_CHANGE_60(LabelByCategories.VALUE, "Nivåförändring, 60 dagar", p -> {
        var z = p.ext().getGroundwaterLevelDiff(60);

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 2);
    }),
    VALUE_LEVEL_CHANGE_90(LabelByCategories.VALUE, "Nivåförändring, 90 dagar", p -> {
        var z = p.ext().getGroundwaterLevelDiff(90);

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 2);
    }),
    VALUE_GROUNDWATER(LabelByCategories.VALUE, "Grundvattennivå", p -> {
        var z = p.ext().getObservationFilteredLast().getGroundwaterLevel();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 2);
    }),
    VALUE_Z(LabelByCategories.VALUE, "Z", p -> {
        var z = p.getZeroZ();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 3);
    });
    private final String mCategory;
    private final Function<BHydroGroundwaterPoint, String> mFunction;
    private final String mName;

    private GroundwaterLabelBy(String category, String name, Function<BHydroGroundwaterPoint, String> function) {
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

    public String getLabel(BHydroGroundwaterPoint o) {
        return mFunction.apply(o);
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
        public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
        public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());
        public static final String DIMENS_FREQ = "%s & %s".formatted(SDict.DIMENSION.toString(), SDict.FREQUENCY.toString());

    }
}
