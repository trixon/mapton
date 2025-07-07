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

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.BXyzPointObservation;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class LabelBy {

    public static final String CAT_ALARM = SDict.ALARM.toString();
    public static final String CAT_DATE = Dict.DATE.toString();
    public static final String CAT_DELTA = "Delta";
    public static final String CAT_MEAS = SDict.MEASUREMENTS.toString();
    public static final String CAT_MISC = Dict.MISCELLANEOUS.toString();
    public static final String CAT_ROOT = "";
    public static final String CAT_VALUE = Dict.VALUE.toString();
    public static final String DIMENS_FREQ = "%s & %s".formatted(SDict.DIMENSION.toString(), SDict.FREQUENCY.toString());

    public static final String HEIGHT_NAME = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.NAME.toLower());
    public static final String HEIGHT_PERCENT = "%s, %%".formatted(Dict.Geometry.HEIGHT);
    public static final String HEIGHT_VALUE = "%s, %s".formatted(Dict.Geometry.HEIGHT, Dict.VALUE.toLower());
    public static final String MEAS_COUNT = Dict.NUM_OF_S.toString().formatted(SDict.MEASUREMENTS.toLower());
    public static final String MEAS_COUNT_ALL = "%s (%s)".formatted(MEAS_COUNT, Dict.ALL.toLower());
    public static final String MEAS_COUNT_SELECTION = "%s (%s)".formatted(MEAS_COUNT, Dict.SELECTION.toLower());
    public static final String PLANE_NAME = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.NAME.toLower());
    public static final String PLANE_PERCENT = "%s, %%".formatted(Dict.Geometry.PLANE);
    public static final String PLANE_VALUE = "%s, %s".formatted(Dict.Geometry.PLANE, Dict.VALUE.toLower());
    private static final String DEFAULT_DATE_IF_NULL = "-";

    public static String alarmHName(BXyzPoint p) {
        return p.getAlarm1Id();
    }

    public static String alarmHPercent(BXyzPoint p) {
        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            return ext.getAlarmPercentHString(ext);
        }
        return "ERROR";
    }

    public static String alarmHValue(BXyzPoint p) {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p);
    }

    public static String alarmPName(BXyzPoint p) {
        return p.getAlarm2Id();
    }

    public static String alarmPPercent(BXyzPoint p) {
        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            return ext.getAlarmPercentPString(ext);
        }
        return "ERROR";
    }

    public static String alarmPValue(BXyzPoint p) {
        return AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p);
    }

    public static String alarmPercent(BXyzPoint p) {
        if (p.ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            return ext.getAlarmPercentString(ext);
        }
        return "ERROR";
    }

    public static String dateFirst(BXyzPoint p) {
        return Objects.toString(p.extOrNull().getObservationFilteredFirstDate(), DEFAULT_DATE_IF_NULL);
    }

    public static String dateLatest(BXyzPoint p) {
        return Objects.toString(p.extOrNull().getObservationFilteredLastDate(), DEFAULT_DATE_IF_NULL);
    }

    public static String dateNext(BXyzPoint p) {
        return Objects.toString(p.extOrNull().getObservationRawNextDate(), DEFAULT_DATE_IF_NULL);
    }

    public static String dateRolling(BXyzPoint p) {
        return Objects.toString(p.getDateRolling(), DEFAULT_DATE_IF_NULL);
    }

    public static String dateValidity(BXyzPoint p) {
        var d1 = p.getDateValidFrom();
        var d2 = p.getDateValidTo();
        if (ObjectUtils.allNull(d1, d2)) {
            return "";
        } else {
            var dat1 = d1 == null ? "" : d1.toString();
            var dat2 = d2 == null ? "" : d2.toString();

            return "%s - %s".formatted(dat1, dat2);
        }
    }

    public static String dateZero(BXyzPoint p) {
        return Objects.toString(p.getDateZero(), DEFAULT_DATE_IF_NULL);
    }

    public static String measAge(BXyzPoint p) {
        var daysSinceMeasurement = p.extOrNull().getMeasurementAge(ChronoUnit.DAYS);

        return "%d".formatted(daysSinceMeasurement);
    }

    public static String measAgeAlarmLevel(BXyzPoint p) {
        return p.extOrNull().getAlarmLevelAge();
    }

    public static String measAgeZero(BXyzPoint p) {
        var daysSinceMeasurement = p.extOrNull().getZeroMeasurementAge(ChronoUnit.DAYS);

        return "%d".formatted(daysSinceMeasurement);
    }

    public static String measCountAll(BXyzPoint p) {
        return "%d".formatted(p.extOrNull().getNumOfObservations());
    }

    public static String measCountFiltered(BXyzPoint p) {
        return "%d".formatted(p.extOrNull().getNumOfObservationsFiltered());
    }

    public static String measCountFilteredAll(BXyzPoint p) {
        return "%d / %d".formatted(
                p.extOrNull().getNumOfObservationsFiltered(),
                p.extOrNull().getNumOfObservations()
        );
    }

    public static String measLatestOperator(BXyzPoint p) {
        return p.extOrNull().getObservationsAllRaw().getLast().getOperator();
    }

    public static String measNeed(BXyzPoint p) {
        return p.getFrequency() == 0 ? "-" : Long.toString(p.extOrNull().getMeasurementUntilNext(ChronoUnit.DAYS));
    }

    public static String measNeedFreq(BXyzPoint p) {
        return "%s (%s)".formatted(measNeed(p), miscFrequency(p));
    }

    public static String miscCategory(BXyzPoint p) {
        return Objects.toString(p.getCategory(), "NODATA");
    }

    public static String miscDimens(BXyzPoint p) {
        return p.getDimension() != null ? p.getDimension().getName() : "--";
    }

    public static String miscFrequency(BXyzPoint p) {
        return Objects.toString(p.getFrequency(), "--");
    }

    public static String miscFrequencyAll(BXyzPoint p) {
        return StringHelper.join(" : ", "", p.getFrequency().toString(), p.getFrequencyDefault().toString(), p.getFrequencyHigh().toString());
    }

    public static String miscFrequencyDefault(BXyzPoint p) {
        return p.getFrequencyDefault().toString();
    }

    public static String miscFrequencyHigh(BXyzPoint p) {
        return p.getFrequencyHigh().toString();
    }

    public static String miscFrequencyHighParam(BXyzPoint p) {
        return p.getFrequencyHighParam();
    }

    public static String miscGroup(BXyzPoint p) {
        return Objects.toString(p.getGroup(), "NODATA");
    }

    public static String miscOperator(BXyzPoint p) {
        return Objects.toString(p.getOperator(), "NODATA");
    }

    public static String miscOrigin(BXyzPoint p) {
        return Objects.toString(p.getOrigin(), "NODATA");
    }

    public static String miscRollingFormula(BXyzPoint p) {
        return Objects.toString(p.getRollingFormula(), "");
    }

    public static String miscSparse(BXyzPoint p) {
        return Objects.toString(p.getSparse(), "");
    }

    public static String miscStatus(BXyzPoint p) {
        return Objects.toString(p.getStatus(), "NODATA");
    }

    public static String valueZeroZ(BXyzPoint p) {
        var z = p.getZeroZ();

        return z == null ? "-" : MathHelper.convertDoubleToStringWithSign(z, 3);
    }

}
