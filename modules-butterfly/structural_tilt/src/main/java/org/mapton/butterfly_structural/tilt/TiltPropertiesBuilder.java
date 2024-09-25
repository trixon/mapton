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
package org.mapton.butterfly_structural.tilt;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.ResourceBundle;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.api.ui.forms.PropertiesBuilder;
import static org.mapton.api.ui.forms.PropertiesBuilder.SEPARATOR;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.openide.util.NbBundle;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TiltPropertiesBuilder extends PropertiesBuilder<BStructuralTiltPoint> {

    private final ResourceBundle mBundle = NbBundle.getBundle(TiltPropertiesBuilder.class);

    @Override
    public Object build(BStructuralTiltPoint p) {
        if (p == null) {
            return p;
        }

        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();

        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1, Dict.STATUS.toString()), p.getStatus());
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());
        propertyMap.put(getCatKey(cat1, Dict.ORIGIN.toString()), p.getOrigin());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, SDict.ALARM.toString()), p.getNameOfAlarm());
        propertyMap.put(getCatKey(cat1, Dict.VALUE.toString()), AlarmHelper.getInstance().getLimitsAsString(p));
        var measurements = "%d / %d    (%d - %d)".formatted(
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isZeroMeasurement()).count(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count()
        );
        String validFromTo = null;
        if (ObjectUtils.anyNotNull(p.getDateValidFrom(), p.getDateValidTo())) {
            var fromDat = Objects.toString(DateHelper.toDateString(p.getDateValidFrom()), "1970-01-01");
            var toDat = Objects.toString(DateHelper.toDateString(p.getDateValidTo()), "2099-12-31");
            validFromTo = StringHelper.joinNonNulls(" // ", fromDat, toDat);
        }
        propertyMap.put(getCatKey(cat1, SDict.MEASUREMENTS.toString()), measurements);
        propertyMap.put(getCatKey(cat1, "%s %s - %s".formatted(Dict.VALID.toString(), Dict.FROM.toLower(), Dict.TO.toLower())), validFromTo);

        var firstRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawFirstDate()), "-");
        var firstFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredFirstDate()), "-");
        var lastRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "-");
        var lastFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredLastDate()), "-");
//        var nextRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawNextDate()), "-");
        var nextRaw = Objects.toString(DateHelper.toDateString((LocalDate) null), "-");

        propertyMap.put(getCatKey(cat1, SDict.FREQUENCY.toString()), p.getFrequency());
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, Dict.NEED.toString()), need);
        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getMeasurementAge(ChronoUnit.DAYS));
//+++        propertyMap.put(getCatKey(cat1, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower())), p.ext().getAlarmLevelAge());
        propertyMap.put(getCatKey(cat1, Dict.LATEST.toString()),
                "%s (%s)".formatted(lastRaw, lastFiltered)
        );
        propertyMap.put(getCatKey(cat1, Dict.NEXT.toString()), nextRaw);
        propertyMap.put(getCatKey(cat1, Dict.REFERENCE.toString()),
                "%s (%s)".formatted(
                        Objects.toString(DateHelper.toDateString(p.getDateZero()), "-"),
                        Objects.toString(DateHelper.toDateString(p.getDateRolling()), "-"))
        );
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString()),
                "%s (%s)".formatted(firstRaw, firstFiltered)
        );
        var delta = "Δ ";
        propertyMap.put(getCatKey(cat1, delta + SDict.ROLLING.toString()), p.ext().getDeltaRolling());
        propertyMap.put(getCatKey(cat1, delta + Dict.REFERENCE.toString()), p.ext().getDeltaZero());

        propertyMap.put(getCatKey(cat1, Dict.Geometry.DIRECTION_X.toString()), StringHelper.round(p.getDirectionX(), 0));
        propertyMap.put(getCatKey(cat1, "N"), StringHelper.round(p.getZeroY(), 3));
        propertyMap.put(getCatKey(cat1, "E"), StringHelper.round(p.getZeroX(), 3));
        propertyMap.put(getCatKey(cat1, "H"), StringHelper.round(p.getZeroZ(), 3));
        propertyMap.put(getCatKey(cat1, Dict.CREATED.toString()), DateHelper.toDateString(p.getDateCreated()));
        propertyMap.put(getCatKey(cat1, Dict.CHANGED.toString()), DateHelper.toDateString(p.getDateChanged()));

        calcBearing(p);
        return propertyMap;
    }

    private double calcBearing(BStructuralTiltPoint p) {
        System.out.println("");
        System.out.println(p.getName());
        var bearing = MathHelper.convert(p.getDirectionX());
        var o0 = p.ext().getObservationFilteredFirst();
        var o1 = p.ext().getObservationFilteredLast();
        if (ObjectUtils.anyNull(o0, o1)) {
            System.out.println("calc failed");
            return 0;

        }
        var v0 = Math.atan(o0.getMeasuredY() / o0.getMeasuredX());
        var v1 = Math.atan(o1.getMeasuredY() / o1.getMeasuredX());
        var delta = Math.toDegrees(v1 - v0);
        var r = bearing + delta;

        if (r > 360) {
//            r -= 360;
        } else if (r < 0) {
//            r += 360;
        }
        System.out.println("bearing =%.3f".formatted(bearing));
        System.out.println("delta   =%.3f".formatted(delta));
        System.out.println("sum     =%.3f".formatted(r));

        return r;
    }

    private double calcBearingX(BStructuralTiltPoint p) {
        var bearing = MathHelper.convert(p.getDirectionX());
        System.out.println("");
        System.out.println(p.getName());
        var o0 = p.ext().getObservationFilteredFirst();
        var o1 = p.ext().getObservationFilteredLast();
        var dx = o1.getMeasuredX() - o0.getMeasuredX();
        var dy = o1.getMeasuredY() - o0.getMeasuredY();
//        var v0 = Math.atan(o0.getMeasuredY() / o0.getMeasuredX());
//        var v1 = Math.atan(o1.getMeasuredY() / o1.getMeasuredX());
        var ar = Math.atan(dy / dx);//vinkel i radianer
        var ag = Math.toDegrees(ar);//vinkel i grader
        System.out.println("x1=%.6f".formatted(o1.getMeasuredX()));
        System.out.println("x0=%.6f".formatted(o0.getMeasuredX()));
        System.out.println("dx=%.6f".formatted(dx));
        System.out.println("y1=%.6f".formatted(o1.getMeasuredY()));
        System.out.println("y0=%.6f".formatted(o0.getMeasuredY()));
        System.out.println("dy=%.6f".formatted(dy));
        System.out.println("aR=%.6f".formatted(ar));
        System.out.println("aG=%.6f".formatted(ag));
//        var delta = Math.toDegrees(v1 - v0);
        var toBearingStyle = MathHelper.convert(ag);
        System.out.println("dg=%.6f".formatted(toBearingStyle));
//        ag = 10.0;
//        if (delta < 0) {
//            delta += 360;
//        }
        System.out.println("%.4f".formatted(bearing));
        double r = bearing + toBearingStyle;
        System.out.println("NEW BEARING=%.3f".formatted(r));
        if (r > 360) {
            r -= 360;
        } else if (r < 0) {
            r += 360;

        }
        return r;
    }

}
