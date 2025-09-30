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
package org.mapton.butterfly_format.types;

/**
 *
 * @author Patrik Karlström
 */
public enum BTrendPeriod {
    WEEK("1w", "1 vecka"),
    MONTH("1m", "1 månad"),
    QUARTER("3m", "3 månader"),
    HALF_YEAR("6m", "6 månader"),
    YEAR("1y", "1 år"),
    ZERO("z", "Nollmätning"),
    FIRST("f", "Första");
    private final String mCode;
    private final String mTitle;

    private BTrendPeriod(String code, String title) {
        mCode = code;
        mTitle = title;
    }

    public String getCode() {
        return mCode;
    }

    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        return mTitle;
    }

}
