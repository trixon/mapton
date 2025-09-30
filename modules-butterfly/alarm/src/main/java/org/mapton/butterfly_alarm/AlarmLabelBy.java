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
package org.mapton.butterfly_alarm;

import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.BAlarm;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum AlarmLabelBy implements LabelBy.Operations {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MISC_ID(LabelBy.CAT_MISC, "Id", p -> {
        return p.getId();
    }),
    MISC_LIMIT_1(LabelBy.CAT_MISC, "Limit 1", p -> {
        return p.getLimit1();
    }),
    MISC_LIMIT_2(LabelBy.CAT_MISC, "Limit 2", p -> {
        return p.getLimit2();
    }),
    MISC_LIMIT_3(LabelBy.CAT_MISC, "Limit 3", p -> {
        return p.getLimit3();
    }),
    MISC_RATIO_1(LabelBy.CAT_MISC, "Ratio 1", p -> {
        return p.getRatio1s();
    }),
    MISC_RATIO_2(LabelBy.CAT_MISC, "Ratio 2", p -> {
        return p.getRatio2s();
    }),
    MISC_RATIO_3(LabelBy.CAT_MISC, "Ratio 3", p -> {
        return p.getRatio3s();
    }),
    MISC_POINT_COUNT(LabelBy.CAT_MISC, "Antal punkter", p -> {
        return String.valueOf(p.ext().getPoints().size());
    }),
    MISC_GROUP(LabelBy.CAT_MISC, Dict.GROUP.toString(), p -> {
        return LabelBy.miscGroup(p);
    }),
    MISC_ORIGIN(LabelBy.CAT_MISC, Dict.ORIGIN.toString(), p -> {
        return LabelBy.miscOrigin(p);
    });
    private final String mCategory;
    private final Function<BAlarm, String> mFunction;
    private final String mName;

    private AlarmLabelBy(String category, String name, Function<BAlarm, String> function) {
        mCategory = category;
        mName = name;
        mFunction = function;
    }

    @Override
    public String getCategory() {
        return mCategory;
    }

    @Override
    public String getFullName() {
        if (StringUtils.isBlank(mCategory)) {
            return mName;
        } else {
            return "%s/%s".formatted(mCategory, mName);
        }
    }

    public String getLabel(BAlarm o) {
        try {
            return mFunction.apply(o);
        } catch (Exception e) {
            return "ERROR %s <<<<<<<<".formatted(o.getName());
        }
    }

    @Override
    public String getName() {
        return mName;
    }
}
