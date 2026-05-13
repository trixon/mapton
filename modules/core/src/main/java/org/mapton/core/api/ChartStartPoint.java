/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.core.api;

import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström
 */
public enum ChartStartPoint {
    FIRST("Sedan första", -1),
    ZERO("Sedan nollmätning", -1),
    LATEST_03("Senaste 3 månaderna", 3),
    LATEST_06("Senaste 6 månaderna", 6),
    LATEST_12("Senaste 12 månaderna", 12),
    LATEST_24("Senaste 24 månaderna", 24);
    private final int mMonths;
    private final String mTitle;

    private ChartStartPoint(String title, int months) {
        mTitle = title;
        mMonths = months;
    }

    public LocalDateTime getStartDate() {
        return null;
    }

    public int getMonths() {
        return mMonths;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return mTitle;
    }

}
