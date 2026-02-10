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
package org.mapton.butterfly_core.api;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Set;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.ui.forms.PropertiesBuilder;
import static org.mapton.api.ui.forms.PropertiesBuilder.SEPARATOR;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.BooleanHelper;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 * @param <T>
 */
public abstract class BPropertiesBuilder<T> extends PropertiesBuilder<T> {

    public static final String CAT_BASIC = Dict.BASIC.toString();
    public static final String CAT_DATABASE = Dict.DATABASE.toString();
    public static final String CAT_DATE = Dict.DATE.toString();
    public static final String CAT_MEAS = SDict.MEASUREMENTS.toString();

    public BPropertiesBuilder() {
    }

    public LinkedHashMap<String, Object> populateBasics(BXyzPoint p, BasicParams params) {
        return populateBasics(p, params, Set.of());
    }

    public LinkedHashMap<String, Object> populateBasics(BXyzPoint p, BasicParams params, Set exclusion) {
        var map = new LinkedHashMap<String, Object>();
        if (isValid(exclusion, ExcludeBasic.NAME)) {
            map.put(getCatKeyNum(CAT_BASIC, Dict.NAME.toString()), p.getName());
        }
        if (isValid(exclusion, ExcludeBasic.STATUS)) {
            map.put(getCatKeyNum(CAT_BASIC,
                    StringHelper.join(SEPARATOR, "", "Sta", "Dim", "Kls")),
                    StringHelper.join(SEPARATOR, "", p.getStatus(), p.getDimension().getName(), p.getClassification()));
        }
        if (isValid(exclusion, ExcludeBasic.GRP_CAT)) {
            map.put(getCatKeyNum(CAT_BASIC,
                    StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                    StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        }
        if (isValid(exclusion, ExcludeBasic.ORIGIN_OPERATOR)) {
            map.put(getCatKeyNum(CAT_BASIC,
                    StringHelper.join(SEPARATOR, "", Dict.ORIGIN.toString(), SDict.OPERATOR.toString())),
                    StringHelper.join(SEPARATOR, "", p.getOrigin(), p.getOperator()));
        }
        if (isValid(exclusion, ExcludeBasic.MEAS_MODE)) {
            var measurementMode = p.getMeasurementMode();
            map.put(getCatKeyNum(CAT_BASIC, "Mätläge"), measurementMode == null ? "-" : measurementMode.toString());
        }
        if (isValid(exclusion, ExcludeBasic.TAG)) {
            map.put(getCatKeyNum(CAT_BASIC, Dict.TAG.toString()), p.getTag());
        }
        if (isValid(exclusion, ExcludeBasic.COMMENT)) {
            map.put(getCatKeyNum(CAT_BASIC, Dict.COMMENT.toString()), p.getComment());
        }

        return map;
    }

    public LinkedHashMap<String, Object> populateDatabase(BXyzPoint p) {
        return populateDatabase(p, Set.of());
    }

    public LinkedHashMap<String, Object> populateDatabase(BXyzPoint p, Set exclusion) {
        var map = new LinkedHashMap<String, Object>();

        if (isValid(exclusion, ExcludeDatabase.UNITS)) {
            map.put(getCatKeyNum(CAT_DATABASE, "Enhet (diff)"), "%s (%s)".formatted(p.getUnit(), p.getUnitDiff()));
        }
        if (isValid(exclusion, ExcludeDatabase.CREATED)) {
            map.put(getCatKeyNum(CAT_DATABASE, Dict.CREATED.toString()), DateHelper.toDateString(p.getDateCreated()));
        }
        if (isValid(exclusion, ExcludeDatabase.CHANGED)) {
            map.put(getCatKeyNum(CAT_DATABASE, Dict.CHANGED.toString()), DateHelper.toDateString(p.getDateChanged()));
        }

        return map;
    }

    public LinkedHashMap<String, Object> populateDates(BXyzPoint p, DateParams params) {
        return populateDates(p, params, Set.of());
    }

    public LinkedHashMap<String, Object> populateDates(BXyzPoint p, DateParams params, Set exclusion) {
        var map = new LinkedHashMap<String, Object>();

        var firstRaw = Objects.toString(DateHelper.toDateString(params.firstRawDate), "-");
        var firstFiltered = Objects.toString(DateHelper.toDateString(params.firstFilteredDate), "-");
        var lastRaw = Objects.toString(DateHelper.toDateString(params.lastRawDate), "-");
        var lastFiltered = Objects.toString(DateHelper.toDateString(params.lastFilteredDate), "-");
        var nextRaw = Objects.toString(DateHelper.toDateString(params.nextRawDate), "-");
        if (isValid(exclusion, ExcludeDate.LATEST)) {
            map.put(getCatKeyNum(CAT_DATE, Dict.LATEST.toString()),
                    "%s (%s)".formatted(lastRaw, lastFiltered)
            );
        }
        if (isValid(exclusion, ExcludeDate.NEXT)) {
            map.put(getCatKeyNum(CAT_DATE, Dict.NEXT.toString()), nextRaw);
        }
        if (isValid(exclusion, ExcludeDate.FIRST)) {
            map.put(getCatKeyNum(CAT_DATE, Dict.FIRST.toString()),
                    "%s (%s)".formatted(firstRaw, firstFiltered)
            );
        }

        String validFromTo = null;
        if (ObjectUtils.anyNotNull(p.getDateValidFrom(), p.getDateValidTo())) {
            var fromDat = Objects.toString(DateHelper.toDateString(p.getDateValidFrom()), "1970-01-01");
            var toDat = Objects.toString(DateHelper.toDateString(p.getDateValidTo()), "2099-12-31");
            validFromTo = StringHelper.joinNonNulls(" // ", fromDat, toDat);
        }

        if (isValid(exclusion, ExcludeDate.REFERENCE)) {
            map.put(getCatKeyNum(CAT_DATE, Dict.REFERENCE.toString()),
                    "%s (%s)".formatted(
                            Objects.toString(DateHelper.toDateString(p.getDateZero()), "-"),
                            Objects.toString(DateHelper.toDateString(p.getDateRolling()), "-"))
            );
        }
        if (isValid(exclusion, ExcludeDate.VALID)) {
            map.put(getCatKeyNum(CAT_DATE, "%s %s - %s".formatted(Dict.VALID.toString(), Dict.FROM.toLower(), Dict.TO.toLower())), validFromTo);
        }

        return map;
    }

    public LinkedHashMap<String, Object> populateMeas(BXyzPoint p, MeasParams params) {
        return populateMeas(p, params, Set.of());
    }

    public LinkedHashMap<String, Object> populateMeas(BXyzPoint p, MeasParams params, Set exclusion) {
        var map = new LinkedHashMap<String, Object>();
        var delta = "Δ ";

        if (isValid(exclusion, ExcludeMeas.ALARM)) {
            map.put(getCatKeyNum(CAT_MEAS, SDict.ALARM.toString()), StringHelper.join(SEPARATOR, "", p.getAlarm1Id(), p.getAlarm2Id()));
        }
        if (isValid(exclusion, ExcludeMeas.HEIGHT)) {
            map.put(getCatKeyNum(CAT_MEAS, Dict.Geometry.HEIGHT.toString()), params.alarmLimitH);
        }
        if (isValid(exclusion, ExcludeMeas.PLANE)) {
            map.put(getCatKeyNum(CAT_MEAS, Dict.Geometry.PLANE.toString()), params.alarmLimitP);
        }
        if (isValid(exclusion, ExcludeMeas.PERCENTAGE)) {
            map.put(getCatKeyNum(CAT_MEAS, "Larmförbrukning"), params.alarmPercent);
        }
        if (isValid(exclusion, ExcludeMeas.ALARM_LEVEL_AGE)) {
            map.put(getCatKeyNum(CAT_MEAS, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower())), params.alarmLevelAge);
        }
        if (isValid(exclusion, ExcludeMeas.FREQ)) {
            map.put(getCatKeyNum(CAT_MEAS,
                    StringHelper.join(SEPARATOR, "", SDict.FREQUENCY.toString(), Dict.DEFAULT.toString(), Dict.HIGH.toString())),
                    StringHelper.join(SEPARATOR, "", p.getFrequency().toString(), p.getFrequencyDefault().toString(), p.getFrequencyHigh().toString()));
        }
        if (isValid(exclusion, ExcludeMeas.FREQ_CONDITION)) {
            var freqCondition = "Frekvensvillkor, hög";
            map.put(getCatKeyNum(CAT_MEAS, freqCondition), p.getFrequencyHighParam());
        }
        if (isValid(exclusion, ExcludeMeas.NEED)) {
            var need = p.getFrequency() == 0 ? "-" : Long.toString(params.dayUntilNext);
            map.put(getCatKeyNum(CAT_MEAS, Dict.NEED.toString()), need);
        }
        if (isValid(exclusion, ExcludeMeas.AGE)) {
            map.put(getCatKeyNum(CAT_MEAS, Dict.AGE.toString()), params.age);
        }
        if (isValid(exclusion, ExcludeMeas.MEAS)) {
            var measurements = "%d / %d".formatted(
                    params.numOfObsFiltered,
                    params.numOfObsTotal
            );
            map.put(getCatKeyNum(CAT_MEAS, SDict.MEASUREMENTS.toString()), measurements);
        }
        if (isValid(exclusion, ExcludeMeas.MEAS_FIRST_ZERO)) {
            map.put(getCatKeyNum(CAT_MEAS, SDict.MEASUREMENTS_FIRST_IS_ZERO.toString()), BooleanHelper.asYesNo(params.firstIsZero));
        }
        if (isValid(exclusion, ExcludeMeas.MEAS_REPLACEMENT)) {
            map.put(getCatKeyNum(CAT_MEAS, SDict.MEASUREMENTS_NUM_OF_REPLACEMENTS.toString()), params.numOfReplacements);
        }
        if (isValid(exclusion, ExcludeMeas.BEARING)) {
            map.put(getCatKeyNum(CAT_MEAS, Dict.BEARING.toString()), StringHelper.round(params.azimuth(), 0));
        }
        if (isValid(exclusion, ExcludeMeas.SPARSE)) {
            map.put(getCatKeyNum(CAT_MEAS, "Utglesning"), p.getSparse());
        }
        if (isValid(exclusion, ExcludeMeas.ROLLING_FORMULA)) {
            map.put(getCatKeyNum(CAT_MEAS, SDict.ROLLING.toString()), p.getRollingFormula());
        }
        if (isValid(exclusion, ExcludeMeas.ROLLING_VALUE)) {
            map.put(getCatKeyNum(CAT_MEAS, delta + SDict.ROLLING.toString()), params.deltaRolling);
        }
        if (isValid(exclusion, ExcludeMeas.DELTA_ZERO)) {
            map.put(getCatKeyNum(CAT_MEAS, delta + Dict.REFERENCE.toString()), params.deltaZero);
        }
        if (isValid(exclusion, ExcludeMeas.DELTA_FIRST)) {
            map.put(getCatKeyNum(CAT_MEAS, delta + Dict.FIRST.toString()), params.deltaFirst);
        }
        if (isValid(exclusion, ExcludeMeas.ZERO_N)) {
            map.put(getCatKeyNum(CAT_MEAS, "N"), StringHelper.round(p.getZeroY(), 3));
        }
        if (isValid(exclusion, ExcludeMeas.ZERO_E)) {
            map.put(getCatKeyNum(CAT_MEAS, "E"), StringHelper.round(p.getZeroX(), 3));
        }
        if (isValid(exclusion, ExcludeMeas.ZERO_H)) {
            map.put(getCatKeyNum(CAT_MEAS, "H"), StringHelper.round(p.getZeroZ(), 3));
        }

        return map;
    }

    private boolean isValid(Set set, Object item) {
        return !set.contains(item);
    }

    public record MeasParams<T>(
            Double azimuth,
            Long dayUntilNext,
            Long age,
            int numOfObsFiltered,
            int numOfObsTotal,
            Boolean firstIsZero,
            long numOfReplacements,
            String alarmLimitH,
            String alarmLimitP,
            String alarmPercent,
            String alarmLevelAge,
            String deltaRolling,
            String deltaZero,
            String deltaFirst) {

    }

    public record BasicParams() {

    }

    public record DateParams(
            LocalDate firstRawDate,
            LocalDate firstFilteredDate,
            LocalDate lastRawDate,
            LocalDate lastFilteredDate,
            LocalDate nextRawDate) {

    }

    public enum ExcludeBasic {
        NAME,
        GRP_CAT,
        STATUS,
        ORIGIN_OPERATOR,
        MEAS_MODE,
        TAG,
        COMMENT
    }

    public enum ExcludeDatabase {
        UNITS,
        CREATED,
        CHANGED
    }

    public enum ExcludeDate {
        LATEST,
        NEXT,
        FIRST,
        REFERENCE,
        VALID
    }

    public enum ExcludeMeas {
        AGE,
        ALARM,
        ALARM_LEVEL_AGE,
        DELTA_FIRST,
        DELTA_ZERO,
        FREQ,
        FREQ_CONDITION,
        HEIGHT,
        MEAS,
        MEAS_FIRST_ZERO,
        MEAS_REPLACEMENT,
        NEED,
        PLANE,
        BEARING,
        SPARSE,
        ROLLING_FORMULA,
        ROLLING_VALUE,
        PERCENTAGE,
        ZERO_N,
        ZERO_E,
        ZERO_H
    }

}
