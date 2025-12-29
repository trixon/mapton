/*
 * Copyright 2025 Patrik Karlström <patrik@trixon.se>.
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
package org.mapton.addon.photos.api;

import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public enum SplitBy {
    NONE(Dict.DO_NOT_SPLIT.toString(), "'NO_SPLIT'"),
    HOUR(Dict.Time.HOUR.toString(), "yyyyMMddHH"),
    DAY(Dict.Time.DAY.toString(), "yyyyMMdd"),
    WEEK(Dict.Time.WEEK.toString(), "yyyyww"),
    MONTH(Dict.Time.MONTH.toString(), "yyyyMM"),
    YEAR(Dict.Time.YEAR.toString(), "yyyy");
    private String mPattern;
    private String mTitle;

    private SplitBy(String title, String pattern) {
        mTitle = title;
        mPattern = pattern;
    }

    public String getPattern() {
        return mPattern;
    }

    @Override
    public String toString() {
        return mTitle;
    }

}
