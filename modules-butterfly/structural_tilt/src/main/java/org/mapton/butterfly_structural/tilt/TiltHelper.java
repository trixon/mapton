/*
 * Copyright 2024 Patrik Karlström.
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

import org.apache.commons.math3.util.FastMath;
import org.mapton.butterfly_alarm.api.AlarmManager;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHelper {

    public static javafx.scene.paint.Color getAlarmColorHeightFx(BStructuralTiltPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevel(p));
    }

    public static int getAlarmLevel(BStructuralTiltPoint p) {
        var o = p.ext().getObservationFilteredLast();
        if (o == null) {
            return -1;
        } else {
            var oldZ = o.ext().getDeltaZ();
            var newZ = toDegreeBased(oldZ);

            o.ext().setDeltaZ(newZ);
            var alarmLevel = p.ext().getAlarmLevelHeight(o);
            o.ext().setDeltaZ(oldZ);

            return alarmLevel;
        }
    }

    public static Double toDegreeBased(Double value) {
        return value == null ? null : FastMath.toDegrees(value) / 1000.0;
    }

    public static Double toRadianBased(Double value) {
        return value == null ? null : FastMath.toRadians(value) * 1000.0;
    }

    public static String getLimitsAsString(BStructuralTiltPoint p) {
        var manager = AlarmManager.getInstance();
        var result = "";
        var alarm = manager.getAllItemsMap().get(p.getAlarm1Id());
        if (alarm != null) {
            var max0 = alarm.ext().getRange0().getMaximum();
            var max1 = alarm.ext().getRange1().getMaximum();
            var s = "%s%.2f // %s%.2f".formatted(
                    alarm.getType(), toRadianBased(max0),
                    alarm.getType(), toRadianBased(max1)
            );
            return s;
        } else {
            System.out.println("Alarm not found: %s, %s".formatted(p.getName(), p.getAlarm1Id()));
        }

        return result;
    }

}
