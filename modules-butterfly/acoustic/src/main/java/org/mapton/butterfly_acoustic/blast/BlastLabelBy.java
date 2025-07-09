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
package org.mapton.butterfly_acoustic.blast;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.LabelBy;
import org.mapton.butterfly_format.types.acoustic.BAcousticBlast;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.MathHelper;

/**
 *
 * @author Patrik Karlström
 */
public enum BlastLabelBy implements LabelBy.Operations {
    NAME(LabelBy.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(LabelBy.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MISC_DATE(LabelBy.CAT_MISC, Dict.DATE.toString(), p -> {
        var date = Objects.toString(DateHelper.toDateString(p.getDateLatest()), "-");

        return date;
    }),
    MISC_AGE(LabelBy.CAT_MISC, Dict.AGE.toString(), p -> {
        return String.valueOf(p.ext().getMeasurementAge(ChronoUnit.DAYS));
    }),
    MISC_GROUP(LabelBy.CAT_MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    }),
    MISC_EXT_ID(LabelBy.CAT_MISC, "Ext.id", p -> {
        return p.getExternalId();
    }),
    MISC_Z(LabelBy.CAT_MISC, "Z", p -> {
        return MathHelper.convertDoubleToString(p.getZeroZ(), 1);
    });
    private final String mCategory;
    private final Function<BAcousticBlast, String> mFunction;
    private final String mName;

    private BlastLabelBy(String category, String name, Function<BAcousticBlast, String> function) {
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

    public String getLabel(BAcousticBlast o) {
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
