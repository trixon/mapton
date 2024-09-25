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
package org.mapton.butterfly_alarm.api;

import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.hydro.BGroundwaterPoint;
import org.mapton.butterfly_format.types.structural.BStructuralTiltPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public class AlarmHelper {

    private final AlarmManager mManager = AlarmManager.getInstance();

    public static AlarmHelper getInstance() {
        return AlarmHelperHolder.INSTANCE;
    }

    private AlarmHelper() {
    }

    public String getLimitsAsString(BComponent component, BTopoControlPoint p) {
        var result = "";
        var alarmH = mManager.getAllItemsMap().get(p.getNameOfAlarmHeight());
        var alarmP = mManager.getAllItemsMap().get(p.getNameOfAlarmPlane());

        if (component == BComponent.HEIGHT) {
            if (alarmH != null) {
                result = StringHelper.join(" // ", "-", alarmH.getLimit1(), alarmH.getLimit2());
            } else {
                System.out.println("Alarm H not found: %s, %s".formatted(p.getName(), p.getNameOfAlarmHeight()));
            }
        } else if (p.getDimension() != BDimension._1d) {
            if (alarmP != null) {
                result = StringHelper.join(" // ", "-", alarmP.getLimit1(), alarmP.getLimit2());
            } else {
                System.out.println("Alarm P not found: %s, %s".formatted(p.getName(), p.getNameOfAlarmPlane()));
            }
        }

        return result;
    }

    public String getLimitsAsString(BStructuralTiltPoint p) {
        var result = "";
        var alarm = mManager.getAllItemsMap().get(p.getNameOfAlarm());
        if (alarm != null) {
            result = StringHelper.join(" // ", "-", alarm.getLimit1(), alarm.getLimit2());
        } else {
            System.out.println("Alarm not found: %s, %s".formatted(p.getName(), p.getNameOfAlarm()));
        }

        return result;
    }

    public String getLimitsAsString(BGroundwaterPoint p) {
        var result = "";
        var alarm = mManager.getAllItemsMap().get(p.getNameOfAlarm());

        if (alarm != null) {
            result = StringHelper.join(" // ", "-", alarm.getLimit1(), alarm.getLimit2());
        } else {
            System.out.println("Alarm H not found: %s, %s".formatted(p.getName(), p.getNameOfAlarm()));
        }

        return result;
    }

    private static class AlarmHelperHolder {

        private static final AlarmHelper INSTANCE = new AlarmHelper();
    }
}
