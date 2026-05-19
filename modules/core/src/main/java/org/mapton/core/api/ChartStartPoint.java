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
    ZERO("Sedan nollmätning", -1),
    LATEST_01("Senaste veckan", 1),
    LATEST_02("Senaste två veckorna", 2),
    LATEST_04("Senaste månaden", 4),
    LATEST_08("Senaste två månaderna", 8),
    LATEST_12("Senaste tre månaderna", 12),
    LATEST_26("Senaste halvåret", 26),
    LATEST_52("Senaste året", 52),
    LATEST_104("Senaste två åren", 104),
    FIRST("Sedan första", -1),;
    private final int mWeeks;
    private final String mTitle;

    private ChartStartPoint(String title, int weeks) {
        mTitle = title;
        mWeeks = weeks;
    }

    public LocalDateTime getStartDate() {
        return null;
    }

    public int getWeeks() {
        return mWeeks;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return mTitle;
    }

}
