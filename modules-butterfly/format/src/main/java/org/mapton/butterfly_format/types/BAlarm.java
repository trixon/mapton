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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.numbers.core.Precision;
import se.trixon.almond.util.MathHelper;

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
    private Double ratio1;
    private String ratio1s;
    private Double ratio2;
    private String ratio2s;
    private Double ratio3;
    private String ratio3s;
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

    public Double getRatio1() {
        return ratio1;
    }

    public String getRatio1s() {
        return ratio1s;
    }

    public Double getRatio2() {
        return ratio2;
    }

    public String getRatio2s() {
        return ratio2s;
    }

    public Double getRatio3() {
        return ratio3;
    }

    public String getRatio3s() {
        return ratio3s;
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

    public void setRatio1(Double ratio1) {
        this.ratio1 = ratio1;
    }

    public void setRatio1s(String ratio1s) {
        this.ratio1s = ratio1s;
    }

    public void setRatio2(Double ratio2) {
        this.ratio2 = ratio2;
    }

    public void setRatio2s(String ratio2s) {
        this.ratio2s = ratio2s;
    }

    public void setRatio3(Double ratio3) {
        this.ratio3 = ratio3;
    }

    public void setRatio3s(String ratio3s) {
        this.ratio3s = ratio3s;
    }

    public void setType(String type) {
        this.type = type;
    }

    public class Ext {

        private Range<Double> mRange0 = null;
        private Range<Double> mRange1 = null;
        private Range<Double> mRange2 = null;
        private Range<Double> mRatioRange0 = null;
        private Range<Double> mRatioRange1 = null;
        private Range<Double> mRatioRange2 = null;

        public Ext() {
        }

        public int getLevel(Double value) {
            value = MathHelper.round(value, 6);//Get rid of things like -0.004999999999999005

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

        public Range<Double> getRange(int rangeIndex) {
            switch (rangeIndex) {
                case 0 -> {
                    return mRange0;
                }
                case 1 -> {
                    return mRange1;
                }
                case 2 -> {
                    return mRange2;
                }
                default ->
                    throw new AssertionError();
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

        public Range<Double> getRangeRatio(int rangeIndex) {
            switch (rangeIndex) {
                case 0 -> {
                    return mRatioRange0;
                }
                case 1 -> {
                    return mRatioRange1;
                }
                case 2 -> {
                    return mRatioRange2;
                }
                default ->
                    throw new AssertionError();
            }
        }

        public List<Range<Double>> getRanges() {
            var list = new ArrayList<Range<Double>>();
            list.add(mRange0);
            list.add(mRange1);
            list.add(mRange2);

            return list.stream().filter(r -> r != null).toList();
        }

        public int getRatioLevel(Double value) {
            value = MathHelper.round(value, 6);//Get rid of things like -0.004999999999999005

            if (ObjectUtils.anyNull(value, mRatioRange0, mRatioRange1)) {
                return -1;
            } else if (value == 0 || mRatioRange0.contains(value)) {
                return 0;
            } else if (mRatioRange1.contains(value)) {
                return 1;
            } else {
                return 2;
            }
        }

        public Range<Double> getRatioRange0() {
            return mRatioRange0;
        }

        public Range<Double> getRatioRange1() {
            return mRatioRange1;
        }

        public Range<Double> getRatioRange2() {
            return mRatioRange2;
        }

        public List<Range<Double>> getRatioRanges() {
            var list = new ArrayList<Range<Double>>();
            list.add(mRatioRange0);
            list.add(mRatioRange1);
            list.add(mRatioRange2);

            return list.stream().filter(r -> r != null).toList();
        }

        public void populateRanges() {
            populateRangesLimit();
            populateRangesRatio();
        }

        private void populateRangesLimit() {
            String l1s = getLimit1();
            String l2s = getLimit2();
            String l3s = getLimit3();

            switch (getType()) {
                case "+" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        mRange0 = Range.of(Double.NEGATIVE_INFINITY, Double.parseDouble(l1s) - Precision.EPSILON);

                        if (StringUtils.isNotBlank(l2s)) {
                            mRange1 = Range.of(Double.NEGATIVE_INFINITY, Double.parseDouble(l2s) - Precision.EPSILON);

                            if (StringUtils.isNotBlank(l3s)) {
                                mRange2 = Range.of(Double.NEGATIVE_INFINITY, Double.parseDouble(l3s) - Precision.EPSILON);
                            }
                        }
                    }
                }

                case "-" -> {
                    if (StringUtils.isNotBlank(l1s)) {
                        mRange0 = Range.of(Double.valueOf(l1s), Double.POSITIVE_INFINITY);

                        if (StringUtils.isNotBlank(l2s)) {
                            mRange1 = Range.of(Precision.EPSILON + Double.parseDouble(l2s), Double.POSITIVE_INFINITY);

                            if (StringUtils.isNotBlank(l3s)) {
                                mRange2 = Range.of(Precision.EPSILON + Double.parseDouble(l3s), Double.POSITIVE_INFINITY);
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

        private void populateRangesRatio() {
            if (ratio1 != null) {
                mRatioRange0 = Range.of(Double.NEGATIVE_INFINITY, ratio1 - Precision.EPSILON);

                if (ratio2 != null) {
                    mRatioRange1 = Range.of(Double.NEGATIVE_INFINITY, ratio2 - Precision.EPSILON);

                    if (ratio3 != null) {
                        mRatioRange2 = Range.of(Double.NEGATIVE_INFINITY, ratio3 - Precision.EPSILON);
                    }
                }
            }
        }
    }
}
