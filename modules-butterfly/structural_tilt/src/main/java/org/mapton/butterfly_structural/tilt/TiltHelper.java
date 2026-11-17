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

import java.awt.Color;
import org.apache.commons.lang3.ObjectUtils;
import org.mapton.butterfly_core.api.ButterflyHelper;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHelper {

    public static Color getAlarmColorHeightAwt(BStructuralTiltPoint p) {
        return ButterflyHelper.getAlarmColorAwt(getAlarmLevel(p));
    }

    public static javafx.scene.paint.Color getAlarmColorHeightFx(BStructuralTiltPoint p) {
        return ButterflyHelper.getAlarmColorFx(getAlarmLevel(p));
    }

    public static int getAlarmLevel(BXyzPoint p) {
        var o = p.extOrNull().getObservationFilteredLast();
        if (o == null) {
            return -1;
        } else {
            return p.extOrNull().getAlarmLevelHeight(o);
        }
    }

    public static int getAlarmLevelLong(BStructuralTiltPoint p) {
        var o = p.ext().getObservationFilteredLast();
        if (o == null) {
            return -1;
        } else {
            var alarm = p.ext().getAlarm(BComponent.HEIGHT);
            if (ObjectUtils.anyNull(alarm, o)) {
                return -1;
            } else {
                return alarm.ext().getLevel(o.ext().getDeltaY());
            }
        }
    }

    public static int getAlarmLevelTrans(BStructuralTiltPoint p) {
        var o = p.ext().getObservationFilteredLast();
        if (o == null) {
            return -1;
        } else {
            var alarm = p.ext().getAlarm(BComponent.HEIGHT);
            if (ObjectUtils.anyNull(alarm, o)) {
                return -1;
            } else {
                return alarm.ext().getLevel(o.ext().getDeltaX());
            }
        }
    }
}
