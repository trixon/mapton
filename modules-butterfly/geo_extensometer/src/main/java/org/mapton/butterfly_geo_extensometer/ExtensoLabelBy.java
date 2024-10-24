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
package org.mapton.butterfly_geo_extensometer;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelByCategories;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum ExtensoLabelBy {
    NAME(LabelByCategories.ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelByCategories.ROOT, Dict.NONE.toString(), p -> {
        return "";
    });
//    MISC_LATEST(LabelByCategories.MISC, SDict.LATEST.toString(), mon -> {
//        var date = mon.getControlPoint().ext().getObservationFilteredLastDate();
//
//        return date == null ? "-" : date.toString();
//    }),
//    MISC_QUOTA(LabelByCategories.MISC, "Kvot", mon -> {
//        return "%s   %s   %s".formatted(mon.getString(1), mon.getString(7), mon.getString(14));
//    });

    private final String mCategory;
    private final Function<BGeoExtensometer, String> mFunction;
    private final String mName;

    private ExtensoLabelBy(String category, String name, Function<BGeoExtensometer, String> function) {
        mCategory = category;
        mName = name;
        mFunction = function;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getFullName() {
        if (StringUtils.isBlank(mCategory)) {
            return mName;
        } else {
            return "%s/%s".formatted(mCategory, mName);
        }
    }

    public String getLabel(BGeoExtensometer o) {
        try {
            return mFunction.apply(o);
        } catch (Exception e) {
            return "ERROR %s <<<<<<<<".formatted(o.getName());
        }
    }

    public String getName() {
        return mName;
    }

}
