/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.api;

import java.time.LocalDate;
import javafx.util.Pair;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;

/**
 *
 * @author Patrik Karlström
 */
public class MDateFormula {

    private final String mCode;

    public MDateFormula(String code) {
        mCode = code;
    }

    public String getCode() {
        return mCode;
    }

    public LocalDate getEndDate() {
        return getStartAndEndDate().getValue();
    }

    public Pair<LocalDate, LocalDate> getStartAndEndDate() {
        var items = StringUtils.split(mCode, ",");
        var mode = items[0];
        var now = LocalDate.now();
        var startDate = LocalDate.MIN;
        var endDate = LocalDate.MAX;
        if (Strings.CI.equals(mode, "L")) {
            var value = Integer.parseInt(items[1]);
            var unit = items[2];
            endDate = now;
            switch (unit) {
                case "D" ->
                    startDate = now.minusDays(value);
                case "W" ->
                    startDate = now.minusWeeks(value);
                case "M" ->
                    startDate = now.minusMonths(value);
                case "Y" ->
                    startDate = now.minusYears(value);
                default ->
                    throw new AssertionError();
            }
        } else if (Strings.CI.equals(mode, "C")) {
            var unit = items[1];
            switch (unit) {
                case "M" ->
                    startDate = now.withDayOfMonth(1);
                case "Y" ->
                    startDate = now.withDayOfYear(1);
                default ->
                    throw new AssertionError();
            }
            endDate = now;
        } else if (Strings.CI.equals(mode, "P")) {
            var unit = items[1];
            switch (unit) {
                case "M" -> {
                    startDate = now.minusMonths(1).withDayOfMonth(1);
                    endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
                }
                case "Y" -> {
                    startDate = now.minusYears(1).withDayOfYear(1);
                    endDate = startDate.withDayOfYear(startDate.lengthOfYear());
                }
                default ->
                    throw new AssertionError();
            }
        }

        return new Pair<>(startDate, endDate);
    }

    public LocalDate getStartDate() {
        return getStartAndEndDate().getKey();
    }

}
