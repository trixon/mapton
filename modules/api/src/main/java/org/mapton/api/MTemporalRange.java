/*
 * Copyright 2022 Patrik Karlström.
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
import java.time.ZoneId;
import java.util.Date;

/**
 *
 * @author Patrik Karlström
 */
public class MTemporalRange {

    private LocalDate mFromLocalDate;
    private LocalDate mToLocalDate;

    public MTemporalRange() {
    }

    public MTemporalRange(LocalDate fromLocalDate, LocalDate toLocalDate) {
        mFromLocalDate = fromLocalDate;
        mToLocalDate = toLocalDate;
    }

    public MTemporalRange(Date fromDate, Date toDate) {
        mFromLocalDate = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        mToLocalDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDate getFromLocalDate() {
        return mFromLocalDate;
    }

    public LocalDate getToLocalDate() {
        return mToLocalDate;
    }

    public void setFromLocalDate(LocalDate fromLocalDate) {
        mFromLocalDate = fromLocalDate;
    }

    public void setToLocalDate(LocalDate toLocalDate) {
        mToLocalDate = toLocalDate;
    }
}
