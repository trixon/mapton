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
package org.mapton.api;

/**
 *
 * @author Patrik Karlström
 */
public enum MAvgPeriod {
    NERVOUS("Nervös", 20),//10
    SENSITIVE("Känslig", 60),//20
    NORMAL("Normal", 180),//60
    CALM("Lugn", 730),//180
    DEAD_CALM("Kolugn", 3650);//730
    private final int mDays;
    private final String mTitle;

    private MAvgPeriod(String title, int days) {
        mTitle = title;
        mDays = days;
    }

    public int getDays() {
        return mDays;
    }

    @Override
    public String toString() {
        return "%s (%d)".formatted(mTitle, mDays);
    }

}
