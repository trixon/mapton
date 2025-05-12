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
package org.mapton.ce_jfreechart.api;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jfree.data.time.Day;
import org.jfree.data.time.Minute;

/**
 *
 * @author Patrik Karlström
 */
public class ChartHelper {

    private ChartHelper() {
    }

    public static Day convertToDay(LocalDate ld) {
        return new Day(
                ld.getDayOfMonth(),
                ld.getMonthValue(),
                ld.getYear()
        );
    }

    public static Minute convertToMinute(LocalDateTime ldt) {
        return new Minute(
                ldt.getMinute(),
                ldt.getHour(),
                ldt.getDayOfMonth(),
                ldt.getMonthValue(),
                ldt.getYear()
        );
    }
}
