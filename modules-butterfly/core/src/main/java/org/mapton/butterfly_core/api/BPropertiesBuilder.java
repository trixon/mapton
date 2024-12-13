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

    public BPropertiesBuilder() {
    }

    public LinkedHashMap<String, Object> populateBasics(BXyzPoint p, BasicParams params) {
        var map = new LinkedHashMap<String, Object>();
        var category = Dict.BASIC.toString();

        map.put(getCatKeyNum(category, Dict.NAME.toString()), p.getName());
        map.put(getCatKeyNum(category,
                StringHelper.join(SEPARATOR, "", Dict.STATUS.toString(), SDict.DIMENSION.toString())),
                StringHelper.join(SEPARATOR, "", p.getStatus(), p.getDimension().getName()));
        map.put(getCatKeyNum(category,
                StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        map.put(getCatKeyNum(category,
                StringHelper.join(SEPARATOR, "", Dict.ORIGIN.toString(), SDict.OPERATOR.toString())),
                StringHelper.join(SEPARATOR, "", p.getOrigin(), p.getOperator()));
        map.put(getCatKeyNum(category, Dict.COMMENT.toString()), p.getComment());

        return map;
    }

    public LinkedHashMap<String, Object> populateDatabase(BXyzPoint p) {
        var map = new LinkedHashMap<String, Object>();
        var category = Dict.DATABASE.toString();

        map.put(getCatKeyNum(category, Dict.CREATED.toString()), DateHelper.toDateString(p.getDateCreated()));
        map.put(getCatKeyNum(category, Dict.CHANGED.toString()), DateHelper.toDateString(p.getDateChanged()));

        return map;
    }

    public LinkedHashMap<String, Object> populateDates(BXyzPoint p, DateParams params) {
        var map = new LinkedHashMap<String, Object>();
        var category = Dict.DATE.toString();

        var firstRaw = Objects.toString(DateHelper.toDateString(params.firstRawDate), "-");
        var firstFiltered = Objects.toString(DateHelper.toDateString(params.firstFilteredDate), "-");
        var lastRaw = Objects.toString(DateHelper.toDateString(params.lastRawDate), "-");
        var lastFiltered = Objects.toString(DateHelper.toDateString(params.lastFilteredDate), "-");
        var nextRaw = Objects.toString(DateHelper.toDateString(params.nextRawDate), "-");
        map.put(getCatKeyNum(category, Dict.LATEST.toString()),
                "%s (%s)".formatted(lastRaw, lastFiltered)
        );
        map.put(getCatKeyNum(category, Dict.NEXT.toString()), nextRaw);
        map.put(getCatKeyNum(category, Dict.FIRST.toString()),
                "%s (%s)".formatted(firstRaw, firstFiltered)
        );

        String validFromTo = null;
        if (ObjectUtils.anyNotNull(p.getDateValidFrom(), p.getDateValidTo())) {
            var fromDat = Objects.toString(DateHelper.toDateString(p.getDateValidFrom()), "1970-01-01");
            var toDat = Objects.toString(DateHelper.toDateString(p.getDateValidTo()), "2099-12-31");
            validFromTo = StringHelper.joinNonNulls(" // ", fromDat, toDat);
        }

        map.put(getCatKeyNum(category, "%s %s - %s".formatted(Dict.VALID.toString(), Dict.FROM.toLower(), Dict.TO.toLower())), validFromTo);
        map.put(getCatKeyNum(category, Dict.REFERENCE.toString()),
                "%s (%s)".formatted(
                        Objects.toString(DateHelper.toDateString(p.getDateZero()), "-"),
                        Objects.toString(DateHelper.toDateString(p.getDateRolling()), "-"))
        );

        return map;
    }

    public LinkedHashMap<String, Object> populateMeas(BXyzPoint p, MeasParams params) {
        var map = new LinkedHashMap<String, Object>();
        var category = SDict.MEASUREMENTS.toString();

        map.put(getCatKeyNum(category, SDict.ALARM.toString()), StringHelper.join(SEPARATOR, "", p.getAlarm1Id(), p.getAlarm2Id()));
        map.put(getCatKeyNum(category, Dict.Geometry.HEIGHT.toString()), params.alarmLimitH);
        map.put(getCatKeyNum(category, Dict.Geometry.PLANE.toString()), params.alarmLimitP);
        map.put(getCatKeyNum(category, "Larmförbrukning"), params.alarmPercent);
        map.put(getCatKeyNum(category, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower())), params.alarmLevelAge);
        //
        var need = p.getFrequency() == 0 ? "-" : Long.toString(params.dayUntilNext);
        map.put(getCatKeyNum(category, SDict.FREQUENCY.toString()), p.getFrequency());
        map.put(getCatKeyNum(category, Dict.NEED.toString()), need);
        map.put(getCatKeyNum(category, Dict.AGE.toString()), params.age);
        var measurements = "%d / %d".formatted(
                params.numOfObsFiltered,
                params.numOfObsTotal
        );
        map.put(getCatKeyNum(category, SDict.MEASUREMENTS.toString()), measurements);
        map.put(getCatKeyNum(category, "FIRST_IS_ZERO"), BooleanHelper.asYesNo(params.firstIsZero));
        map.put(getCatKeyNum(category, "NUM_OF_REPLACEMENTS"), params.numOfReplacements);
        var delta = "Δ ";
        map.put(getCatKeyNum(category, Dict.BEARING.toString()), StringHelper.round(params.azimuth(), 0));
        map.put(getCatKeyNum(category, delta + SDict.ROLLING.toString()), params.deltaRolling);
        map.put(getCatKeyNum(category, delta + Dict.REFERENCE.toString()), params.deltaZero);
        map.put(getCatKeyNum(category, "N"), StringHelper.round(p.getZeroY(), 3));
        map.put(getCatKeyNum(category, "E"), StringHelper.round(p.getZeroX(), 3));
        map.put(getCatKeyNum(category, "H"), StringHelper.round(p.getZeroZ(), 3));

        return map;
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
            String deltaZero) {

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

}
