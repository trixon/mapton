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
package org.mapton.butterfly_topo_convergence.pair;

import java.util.Objects;
import java.util.function.Function;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePair;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public enum ConvergencePairLabelBy {
    NAME(Strings.CAT_ROOT, Dict.NAME.toString(), p -> {
        return p.getName();
    }),
    NONE(Strings.CAT_ROOT, Dict.NONE.toString(), p -> {
        return "";
    }),
    MISC_DELTA(Strings.CAT_MISC, "Delta", p -> {
        return "%.1f".formatted(p.getDeltaDistanceOverTime() * 1000);
    }),
    MISC_DISTANCE(Strings.CAT_MISC, "Avstånd", p -> {
        return "%.3f".formatted(p.getDistance());
    }),
    MISC_DISTANCE_PLANE(Strings.CAT_MISC, "Avstånd, plan", p -> {
        return "%.3f".formatted(p.getDeltaR());
    }),
    MISC_DISTANCE_HEIGHT(Strings.CAT_MISC, "Avstånd, höjd", p -> {
        return "%.3f".formatted(p.getDeltaZ());
    }),
    MISC_GROUP(Strings.CAT_MISC, Dict.GROUP.toString(), p -> {
        return Objects.toString(p.getGroup(), "NODATA");
    });
    private final String mCategory;
    private final Function<BTopoConvergencePair, String> mFunction;
    private final String mName;

    private ConvergencePairLabelBy(String category, String name, Function<BTopoConvergencePair, String> function) {
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

    public String getLabel(BTopoConvergencePair o) {
        try {
            return mFunction.apply(o);
        } catch (Exception e) {
            return "ERROR %s <<<<<<<<".formatted(o.getName());
        }
    }

    public String getName() {
        return mName;
    }

    private class Strings {

        public static final String CAT_MISC = Dict.MISCELLANEOUS.toString();
        public static final String CAT_ROOT = "";

    }
}
