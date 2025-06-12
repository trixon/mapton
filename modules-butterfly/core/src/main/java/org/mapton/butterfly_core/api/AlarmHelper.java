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

import java.util.Arrays;
import javafx.geometry.Point2D;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.BXyzPoint;
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

    public String getLimitsAsString(BComponent component, BXyzPoint p) {
        var result = "";
        var alarmH = mManager.getAllItemsMap().get(p.getAlarm1Id());
        var alarmP = mManager.getAllItemsMap().get(p.getAlarm2Id());

        if (component == BComponent.HEIGHT) {
            if (alarmH != null) {
                result = StringHelper.join(" // ", "-", alarmH.getLimit1(), alarmH.getLimit2());
            } else {
                System.out.println("Alarm H not found: %s, %s".formatted(p.getName(), p.getAlarm1Id()));
            }
        } else if (p.getDimension() != BDimension._1d) {
            if (alarmP != null) {
                result = StringHelper.join(" // ", "-", alarmP.getLimit1(), alarmP.getLimit2());
            } else {
                System.out.println("Alarm P not found: %s, %s".formatted(p.getName(), p.getAlarm2Id()));
            }
        }

        return result;
    }

    public Point2D getMinMax(BAlarm... alarms) {
        var min = Double.MAX_VALUE;
        var max = Double.MIN_VALUE;

        for (var alarm : alarms) {
            if (alarm != null) {
                for (var r : Arrays.asList(alarm.ext().getRange0(), alarm.ext().getRange1(), alarm.ext().getRange2())) {
                    if (r != null) {
                        var rMin = r.getMinimum();
                        if (Double.isFinite(rMin) && rMin < min) {
                            min = r.getMinimum();
                        }
                        var rMax = r.getMaximum();
                        if (Double.isFinite(rMax) && rMax > max) {
                            max = r.getMaximum();
                        }
                    }
                }
            }
        }

        if (Double.isInfinite(min) || Double.MAX_VALUE == min) {
            min = 0.0;
        }
        if (Double.isInfinite(max) || Double.MIN_VALUE == max) {
            max = 0.0;
        }

        return new Point2D(min, max);
    }

    private static class AlarmHelperHolder {

        private static final AlarmHelper INSTANCE = new AlarmHelper();
    }
}
