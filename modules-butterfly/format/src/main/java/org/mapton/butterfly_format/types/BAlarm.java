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
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.Arrays;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.numbers.core.Precision;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "id",
    "type",
    "name",
    "description"
})
public class BAlarm extends BBase {

    private String description;
    private String id;
    private String limit1;
    private String limit2;
    private String limit3;
    private final transient Ext mExt = new Ext();
    private String name;
    private String ratio1;
    private String ratio2;
    private String ratio3;
    private String type;

    public BAlarm() {
    }

    public Ext ext() {
        return mExt;
    }

    public String getDescription() {
        return description;
    }

    public String getId() {
        return id;
    }

    public String getLimit1() {
        return limit1;
    }

    public String getLimit2() {
        return limit2;
    }

    public String getLimit3() {
        return limit3;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getRatio1() {
        return ratio1;
    }

    public String getRatio2() {
        return ratio2;
    }

    public String getRatio3() {
        return ratio3;
    }

    public String getType() {
        return type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLimit1(String limit1) {
        this.limit1 = limit1;
    }

    public void setLimit2(String limit2) {
        this.limit2 = limit2;
    }

    public void setLimit3(String limit3) {
        this.limit3 = limit3;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public void setRatio1(String ratio1) {
        this.ratio1 = ratio1;
    }

    public void setRatio2(String ratio2) {
        this.ratio2 = ratio2;
    }

    public void setRatio3(String ratio3) {
        this.ratio3 = ratio3;
    }

    public void setType(String type) {
        this.type = type;
    }

    public class Ext {

        private Range<Double> mRange0 = null;
        private Range<Double> mRange1 = null;
        private Range<Double> mRange2 = null;

        public Ext() {
        }

        public int getLevel(Double value) {
            if (ObjectUtils.anyNull(value, mRange0, mRange1)) {
                return -1;
            } else if (value == 0 || mRange0.contains(value)) {
                return 0;
            } else if (mRange1.contains(value)) {
                return 1;
            } else {
                return 2;
            }
        }

        public Range<Double> getRange0() {
            return mRange0;
        }

        public Range<Double> getRange1() {
            return mRange1;
        }

        public Range<Double> getRange2() {
            return mRange2;
        }

        public void populateRanges() {
            String l1s = getLimit1();
            String l2s = getLimit2();
            String l3s = getLimit3();

            switch (getType()) {
                case "+" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        mRange0 = Range.of(0.0, Double.parseDouble(l1s) - Precision.EPSILON);

                        if (StringUtils.isNotBlank(l2s)) {
                            mRange1 = Range.of(0.0, Double.parseDouble(l2s) - Precision.EPSILON);

                            if (StringUtils.isNotBlank(l3s)) {
                                mRange2 = Range.of(0.0, Double.parseDouble(l3s) - Precision.EPSILON);
                            }
                        }
                    }
                }

                case "-" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        mRange0 = Range.of(Double.valueOf(l1s), Double.MAX_VALUE);

                        if (StringUtils.isNotBlank(l2s)) {
                            mRange1 = Range.of(Precision.EPSILON + Double.parseDouble(l2s), Double.MAX_VALUE);

                            if (StringUtils.isNotBlank(l3s)) {
                                mRange2 = Range.of(Precision.EPSILON + Double.parseDouble(l3s), Double.MAX_VALUE);
                            }
                        }
                    }
                }

                case ":" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        var values1 = Arrays.stream(StringUtils.split(l1s, "..")).map(k -> Double.valueOf(k)).toArray(Double[]::new);
                        mRange0 = Range.of(Precision.EPSILON + values1[0], values1[1] - Precision.EPSILON);

                        if (StringUtils.isNotBlank(l2s)) {
                            var values2 = Arrays.stream(StringUtils.split(l2s, "..")).map(k -> Double.valueOf(k)).toArray(Double[]::new);
                            mRange1 = Range.of(Precision.EPSILON + values2[0], values2[1] - Precision.EPSILON);

                            if (StringUtils.isNotBlank(l3s)) {
                                var values3 = Arrays.stream(StringUtils.split(l3s, "..")).map(k -> Double.valueOf(k)).toArray(Double[]::new);
                                mRange2 = Range.of(Precision.EPSILON + values3[0], values3[1] - Precision.EPSILON);
                            }
                        }
                    }
                }

                case "±" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        var lim1 = Double.parseDouble(StringUtils.substringAfter(l1s, "±"));
                        mRange0 = Range.of(Precision.EPSILON - lim1, lim1 - Precision.EPSILON);

                        if (StringUtils.isNotBlank(l2s)) {
                            var lim2 = Double.parseDouble(StringUtils.substringAfter(l2s, "±"));
                            mRange1 = Range.of(Precision.EPSILON - lim2, lim2 - Precision.EPSILON);

                            if (StringUtils.isNotBlank(l3s)) {
                                var lim3 = Double.parseDouble(StringUtils.substringAfter(l3s, "±"));
                                mRange2 = Range.of(Precision.EPSILON - lim3, lim3 - Precision.EPSILON);
                            }
                        }
                    }
                }

                default -> {
                    throw new AssertionError();
                }
            }
        }
    }
}
