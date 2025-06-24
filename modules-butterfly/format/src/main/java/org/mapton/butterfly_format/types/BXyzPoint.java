/*
 * Copyright 2024 Patrik Karlström.
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

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.ml.clustering.Clusterable;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import se.trixon.almond.util.MathHelper;
import se.trixon.almond.util.StringHelper;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BXyzPoint extends BBaseControlPoint implements Clusterable {

    private String alarm1Id;
    private String alarm2Id;
    private BDimension dimension;
    private BMeasurementMode measurementMode;
    private Integer numOfDecXY;
    private Integer numOfDecZ;
    private Double offsetX;
    private Double offsetY;
    private Double offsetZ;
    private Double rollingX;
    private Double rollingY;
    private Double rollingZ;
    private Double zeroX;
    private transient Double zeroXScaled;
    private Double zeroY;
    private transient Double zeroYScaled;
    private Double zeroZ;
    private transient Double zeroZScaled;

    public String getAlarm1Id() {
        return alarm1Id;
    }

    public Object ext() {
        return null;
    }

    public String getAlarm2Id() {
        return alarm2Id;
    }

    public BDimension getDimension() {
        return dimension;
    }

    public BMeasurementMode getMeasurementMode() {
        return measurementMode;
    }

    public Integer getNumOfDecXY() {
        return numOfDecXY;
    }

    public Integer getNumOfDecZ() {
        return numOfDecZ;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Double getOffsetZ() {
        return offsetZ;
    }

    @Override
    public double[] getPoint() {
        return new double[]{zeroXScaled, zeroYScaled, zeroZScaled};
    }

    public Double getRollingX() {
        return rollingX;
    }

    public Double getRollingY() {
        return rollingY;
    }

    public Double getRollingZ() {
        return rollingZ;
    }

    public Double getZeroX() {
        return zeroX;
    }

    public Double getZeroXScaled() {
        return zeroXScaled;
    }

    public Double getZeroY() {
        return zeroY;
    }

    public Double getZeroYScaled() {
        return zeroYScaled;
    }

    public Double getZeroZ() {
        return zeroZ;
    }

    public Double getZeroZScaled() {
        return zeroZScaled;
    }

    public void setAlarm1Id(String alarm1Id) {
        this.alarm1Id = alarm1Id;
    }

    public void setAlarm2Id(String alarm2Id) {
        this.alarm2Id = alarm2Id;
    }

    public void setDimension(BDimension dimension) {
        this.dimension = dimension;
    }

    public void setMeasurementMode(BMeasurementMode measurementMode) {
        this.measurementMode = measurementMode;
    }

    public void setNumOfDecXY(Integer numOfDecXY) {
        this.numOfDecXY = numOfDecXY;
    }

    public void setNumOfDecZ(Integer numOfDecZ) {
        this.numOfDecZ = numOfDecZ;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(Double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public void setRollingX(Double rollingX) {
        this.rollingX = rollingX;
    }

    public void setRollingY(Double rollingY) {
        this.rollingY = rollingY;
    }

    public void setRollingZ(Double rollingZ) {
        this.rollingZ = rollingZ;
    }

    public void setZeroX(Double zeroX) {
        this.zeroX = zeroX;
    }

    public void setZeroXScaled(Double zeroXScaled) {
        this.zeroXScaled = zeroXScaled;
    }

    public void setZeroY(Double zeroY) {
        this.zeroY = zeroY;
    }

    public void setZeroYScaled(Double zeroYScaled) {
        this.zeroYScaled = zeroYScaled;
    }

    public void setZeroZ(Double zeroZ) {
        this.zeroZ = zeroZ;
    }

    public void setZeroZScaled(Double zeroZScaled) {
        this.zeroZScaled = zeroZScaled;
    }

    public abstract class Ext<T extends BXyzPointObservation> extends BBasePoint.Ext<T> {

        private transient final DeltaRolling deltaRolling = new DeltaRolling();
        private transient final DeltaZero deltaZero = new DeltaZero();
        private transient Double mFrequenceIntenseBuffer;

        public void calculateObservations(List<T> observations) {
            if (observations.isEmpty()) {
                return;
            }

            observations.forEach(o -> {
                var dateMatch = getStoredZeroDateTime() == o.getDate();
                if (dateMatch) {
                    setZeroUnset(false);
                }
                o.setZeroMeasurement(dateMatch);
            });

            if (isZeroUnset()) {
                observations.getFirst().setZeroMeasurement(true);
            }

            var latestZero = observations.reversed().stream()
                    .filter(o -> o.isZeroMeasurement())
                    .findFirst().orElse(observations.getFirst());

            var zeroX = latestZero.getMeasuredX();
            var zeroY = latestZero.getMeasuredY();
            var zeroZ = latestZero.getMeasuredZ();
            var accumulatedReplacementsX = 0.0;
            var accumulatedReplacementsY = 0.0;
            var accumulatedReplacementsZ = 0.0;

            for (int i = 0; i < observations.size(); i++) {
                var o = observations.get(i);
                var measuredX = o.getMeasuredX();
                var measuredY = o.getMeasuredY();
                var measuredZ = o.getMeasuredZ();
                var hasAccumulatedReplacements = MathHelper.convertDoubleToDouble(accumulatedReplacementsX) != 0
                        || MathHelper.convertDoubleToDouble(accumulatedReplacementsY) != 0
                        || MathHelper.convertDoubleToDouble(accumulatedReplacementsZ) != 0;

                if (o.isZeroMeasurement()) {
                    if (hasAccumulatedReplacements) {
                        for (var oo : observations) {
                            if (oo == o) {
                                break;
                            }
                            if (oo.ext().getDeltaX() != null) {
                                oo.ext().setDeltaX(oo.ext().getDeltaX() + accumulatedReplacementsX);
                            }
                            if (oo.ext().getDeltaY() != null) {
                                oo.ext().setDeltaY(oo.ext().getDeltaY() + accumulatedReplacementsY);
                            }
                            if (oo.ext().getDeltaZ() != null) {
                                oo.ext().setDeltaZ(oo.ext().getDeltaZ() + accumulatedReplacementsZ);
                            }
                        }
                    }
                    accumulatedReplacementsX = 0.0;
                    accumulatedReplacementsY = 0.0;
                    accumulatedReplacementsZ = 0.0;
                }
                if (ObjectUtils.allNotNull(measuredX, zeroX)) {
                    o.ext().setDeltaX(measuredX - zeroX - accumulatedReplacementsX);
                }
                if (ObjectUtils.allNotNull(measuredY, zeroY)) {
                    o.ext().setDeltaY(measuredY - zeroY - accumulatedReplacementsY);
                }
                if (ObjectUtils.allNotNull(measuredZ, zeroZ)) {
                    o.ext().setDeltaZ(measuredZ - zeroZ - accumulatedReplacementsZ);
                }

                if (o.isReplacementMeasurement() && i > 0) {
                    var prev = observations.get(i - 1);
                    var prevX = prev.getMeasuredX();
                    var prevY = prev.getMeasuredY();
                    var prevZ = prev.getMeasuredZ();

                    if (ObjectUtils.allNotNull(measuredX, prevX, o.ext().getDeltaX())) {
                        var replacementX = measuredX - prevX;
                        o.ext().setDeltaX(o.ext().getDeltaX() - replacementX);
                        accumulatedReplacementsX = accumulatedReplacementsX + replacementX;
                    }

                    if (ObjectUtils.allNotNull(measuredY, prevY, o.ext().getDeltaY())) {
                        var replacementY = measuredY - prevY;
                        o.ext().setDeltaY(o.ext().getDeltaY() - replacementY);
                        accumulatedReplacementsY = accumulatedReplacementsY + replacementY;
                    }

                    if (ObjectUtils.allNotNull(measuredZ, prevZ, o.ext().getDeltaZ())) {
                        var replacementZ = measuredZ - prevZ;
                        o.ext().setDeltaZ(o.ext().getDeltaZ() - replacementZ);
                        accumulatedReplacementsZ = accumulatedReplacementsZ + replacementZ;
                    }
                }

                if (offsetZ != null && offsetZ != 0d) {
                    o.ext().setDeltaZ(o.ext().getDeltaZ() + offsetZ);
                }

                o.ext().setAccuX(accumulatedReplacementsX);
                o.ext().setAccuY(accumulatedReplacementsY);
                o.ext().setAccuZ(accumulatedReplacementsZ);
            }
        }

        public DeltaRolling deltaRolling() {
            return deltaRolling;
        }

        public DeltaZero deltaZero() {
            return deltaZero;
        }

        public boolean firstIsZero() {
            if (getObservationsAllRaw().isEmpty()) {
                return false;
            } else {
                return getObservationRawFirstDate().equals(getDateZero());
            }
        }

        public BAlarm getAlarm(BComponent component) {
            var alarm = getButterfly().getAlarms().stream()
                    .filter(a -> {
                        if (component == BComponent.HEIGHT) {
                            return StringUtils.equals(a.getId(), alarm1Id);
                        } else {
                            return StringUtils.equals(a.getId(), alarm2Id);
                        }
                    }).findAny().orElse(null);

            return alarm;
        }

        public int getAlarmLevel(BComponent component, BXyzPointObservation o) {
            var alarm = getAlarm(component);

            if (ObjectUtils.anyNull(alarm, o)) {
                return -1;
            } else {
                return alarm.ext().getLevel(component == BComponent.HEIGHT ? o.ext().getDeltaZ() : o.ext().getDelta2d());
            }
        }

        public int getAlarmLevel(BXyzPointObservation o) {
            return Math.max(getAlarmLevelHeight(o), getAlarmLevelPlane(o));
        }

        public Long getAlarmLevelAge(BComponent component) {
            var lastObservation = getObservationFilteredLast();
            if (getObservationsTimeFiltered().isEmpty() || component == BComponent.HEIGHT && getDimension() == BDimension._2d || component == BComponent.PLANE && getDimension() == BDimension._1d) {
                return null;
            }

            Long alarmLevelAge = null;

            var currentLevel = getAlarmLevel(component, lastObservation);
            Integer prevLevel = null;
            LocalDate prevDate = null;

            for (var o : getObservationsTimeFiltered().reversed()) {
                int alarmLevel = getAlarmLevel(component, o);
                if (alarmLevel != currentLevel) {
                    prevLevel = alarmLevel;
                    break;
                }
                prevDate = o.getDate().toLocalDate();
            }

            if (prevLevel != null) {
                alarmLevelAge = ChronoUnit.DAYS.between(prevDate, LocalDate.now());
                if (prevLevel < currentLevel) {
                    alarmLevelAge *= -1;
                }
            }

            return alarmLevelAge;
        }

        public String getAlarmLevelAge() {
            switch (getDimension()) {
                case _1d -> {
                    return Objects.toString(getAlarmLevelAge(BComponent.HEIGHT), "-");
                }
                case _2d -> {
                    return Objects.toString(getAlarmLevelAge(BComponent.PLANE), "-");
                }
                case _3d -> {
                    var ageH = getAlarmLevelAge(BComponent.HEIGHT);
                    var ageP = getAlarmLevelAge(BComponent.PLANE);
                    if (ObjectUtils.allNull(ageH, ageP)) {
                        return "-";
                    } else {
                        return "%s // %s".formatted(Objects.toString(ageH, "-"), Objects.toString(ageP, "-"));
                    }
                }

                default ->
                    throw new AssertionError();
            }
        }

        public int getAlarmLevelHeight(BXyzPointObservation o) {
            return getAlarmLevel(BComponent.HEIGHT, o);
        }

        public int getAlarmLevelPlane(BXyzPointObservation o) {
            return getAlarmLevel(BComponent.PLANE, o);
        }

        public Integer getAlarmPercent() {
            if (null == getDimension()) {
                return 0;
            } else {
                switch (getDimension()) {
                    case _1d -> {
                        if (StringUtils.isNotBlank(getAlarm1Id())) {
                            return getAlarmPercent(BComponent.HEIGHT);
                        } else {
                            return 0;
                        }
                    }
                    case _2d -> {
                        if (StringUtils.isNotBlank(getAlarm2Id())) {
                            return getAlarmPercent(BComponent.PLANE);
                        } else {
                            return 0;
                        }
                    }
                    default -> {
                        if (StringUtils.isNoneBlank(getAlarm2Id(), getAlarm1Id())) {
                            try {
                                return Math.max(getAlarmPercent(BComponent.HEIGHT), getAlarmPercent(BComponent.PLANE));
                            } catch (Exception e) {
                                return 0;
                            }
                        } else {
                            return 0;
                        }
                    }
                }
            }
        }

        public Integer getAlarmPercent(BComponent component) {
            try {
                var delta = component == BComponent.HEIGHT ? deltaZero().getDelta1() : deltaZero().getDelta2();
                return getAlarmPercent(component, delta);
            } catch (Exception e) {
                return null;
            }
        }

        public Integer getAlarmPercent(BComponent component, Double delta) {
            if (delta == null) {
                return null;
            }

            var alarm = getAlarm(component);
            if (alarm == null
                    || (component == BComponent.HEIGHT && getDimension() == _2d)
                    || (component == BComponent.PLANE && getDimension() == _1d)) {
                return null;
            }

            try {
                double limit;
                Range<Double> range;
                if (alarm.ext().getRange2() != null) {
                    range = alarm.ext().getRange2();
                } else if (alarm.ext().getRange1() != null) {
                    range = alarm.ext().getRange1();
                } else {
                    range = alarm.ext().getRange0();
                }

                if (delta < 0 && component == BComponent.HEIGHT) {
                    limit = range.getMinimum();
                } else {
                    limit = range.getMaximum();
                }
                return (int) Math.round((delta / limit) * 100);
            } catch (Exception e) {
                return null;
            }
        }

        public String getAlarmPercentHString(BXyzPoint.Ext ext) {
            var percentH = getAlarmPercentString(ext, BComponent.HEIGHT);
            var delta = ext.deltaZero().getDelta1();
            var direction = "";
            if (delta != null) {
                if (delta > 0) {
                    direction = " ↑";
                } else if (delta < 0) {
                    direction = " ↓";
                }
            }

            return percentH + direction;
        }

        public String getAlarmPercentPString(BXyzPoint.Ext ext) {
            return getAlarmPercentString(ext, BComponent.PLANE);
        }

        public String getAlarmPercentString(BXyzPoint.Ext ext, BComponent component) {
            var percent = ext.getAlarmPercent(component);
            if (percent == null) {
                return "";
            } else {
                return "%d%%".formatted(percent);
            }
        }

        public String getAlarmPercentString(BXyzPoint.Ext ext) {
            switch (getDimension()) {
                case _1d -> {
                    return getAlarmPercentHString(ext);
                }
                case _2d -> {
                    return getAlarmPercentPString(ext);
                }
                case _3d -> {
                    return "%s :: %s".formatted(StringUtils.defaultIfBlank(getAlarmPercentHString(ext), "-"), StringUtils.defaultIfBlank(getAlarmPercentPString(ext), "-"));
                    //return "%s :: %s".formatted(getAlarmPercentHString(ext), getAlarmPercentPString(ext));
                }
                default ->
                    throw new AssertionError();
            }

        }

        public Double getFrequenceIntenseBuffer() {
            return mFrequenceIntenseBuffer;
        }

        /**
         * WARNING!!!
         */
        public Object[] getHeightDirectionTrendDaysMeas() {
            var observations = getObservationsTimeFiltered();
            if (observations.size() < 2 || getDimension() == _2d) {
                return new Object[]{null, -1, -1};
            }

            T last = observations.getLast();
            T prevO = observations.get(observations.size() - 2);
            T firstO = null;

            int days = -1;
            int meas = 1;

            var prevSignum = Math.signum(rounder(last.ext().getDeltaZ()) - rounder(prevO.ext().getDeltaZ()));
            for (T o : observations.reversed()) {
                if (o == last) {
                    continue;
                }
                var signum = Math.signum(rounder(o.ext().getDeltaZ()) - rounder(prevO.ext().getDeltaZ()));
                firstO = o;

                if (signum != prevSignum) {
                    break;
                }
                meas++;
                prevO = o;
            }

            if (firstO != null) {
                days = (int) ChronoUnit.DAYS.between(firstO.getDate(), last.getDate());
            }

            return new Object[]{firstO.getDate().toLocalDate().toString(), days, meas};
        }

        /**
         * WARNING!!!
         */
        public int[] getHeightDirectionTrendDaysMeasXXX() {
            var observations = getObservationsTimeFiltered();

            if (observations.size() < 2 || getDimension() == _2d) {
                return new int[]{-1, -1};
            }

            T last = observations.getLast();
            T prevO = observations.get(observations.size() - 2);
            T firstO = null;

            int days = -1;
            int meas = 1;

            double prevSignum = Math.signum(last.ext().getDeltaZ() - prevO.ext().getDeltaZ());
            for (T o : observations.reversed()) {
                if (o == last) {
                    continue;
                }
                var signum = Math.signum(o.ext().getDeltaZ() - prevO.ext().getDeltaZ());
                firstO = o;

                if (signum != prevSignum) {
                    break;
                }
                meas++;
                prevO = o;
            }

            if (firstO != null) {
                days = (int) ChronoUnit.DAYS.between(firstO.getDate(), last.getDate());
            }

            return new int[]{days, meas};
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;
            var nextMeas = latest.plusDays(getFrequency());

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public LocalDate getObservationRawNextDate() {
            if (ObjectUtils.allNotNull(getObservationRawLastDate(), getFrequency()) && getFrequency() != 0) {
                return getObservationRawLastDate().plusDays(getFrequency());
            } else {
                return null;
            }
        }

        public double[] getSpeed() {
            try {
                var periodLength = ChronoUnit.DAYS.between(getObservationFilteredFirstDate(), getObservationFilteredLastDate()) / 365.0;
                var distance = deltaZero().getDelta1() - getObservationsTimeFiltered().getFirst().ext().getDeltaZ();
                var speed = distance / periodLength;

                return new double[]{speed, periodLength};
            } catch (Exception e) {
                return new double[]{-1, -1};
            }
        }

        public double[] getSpeed(BXyzPointObservation o1, BXyzPointObservation o2) {
            try {
                var periodLength = ChronoUnit.DAYS.between(o1.getDate(), o2.getDate()) / 365.0;
                var speed = (o2.ext().getDeltaZ() - o1.ext().getDeltaZ()) / periodLength;

                return new double[]{speed, periodLength};
            } catch (Exception e) {
                return new double[]{-1, -1};
            }
        }

        public long getZeroMeasurementAge(ChronoUnit chronoUnit) {
            if (getDateZero() != null) {
                return chronoUnit.between(getDateZero(), LocalDate.now());
            } else {
                return -1L;
            }
        }

        public long getZeroToLatestMeasurementAge(ChronoUnit chronoUnit) {
            if (getDateZero() != null) {
                return chronoUnit.between(getDateZero(), getDateLatest());
            } else {
                return -1L;
            }
        }

        public void setFrequenceIntenseBuffer(Double frequenceIntenseBuffer) {
            this.mFrequenceIntenseBuffer = frequenceIntenseBuffer;
        }

        private double rounder(double d) {
            var snap = 3;
            return snap * (Math.round(d * 1000 / snap));
        }

        public abstract class Delta {

            public String getDelta(int decimals, int factor) {
                return StringHelper.joinNonNulls(", ",
                        getDelta1(decimals, factor),
                        getDelta2(decimals, factor),
                        getDelta3(decimals, factor)
                );
            }

            public String getDelta(int decimals) {
                return getDelta(decimals, 1);
            }

            public Double getDelta() {
                switch (getDimension()) {
                    case _1d -> {
                        return getDelta1();
                    }
                    case _2d -> {
                        return getDelta2();
                    }
                    case _3d -> {
                        return getDelta3();
                    }
                }

                return null;
            }

            public Double getDelta1() {
                return getDeltaZ();
            }

            public String getDelta1(int decimals) {
                return getDelta1(decimals, 1);
            }

            public String getDelta1(int decimals, int factor) {
                var delta = getDelta1();
                if (delta == null) {
                    return null;
                } else {
                    return StringHelper.round(delta * factor, decimals, "Δ1d ", "", true);
                }
            }

            public String getDelta1d2d(int decimals, int factor) {
                return StringHelper.joinNonNulls(", ",
                        getDelta1(decimals, factor),
                        getDelta2(decimals, factor)
                );
            }

            public String getDelta1d2d(int decimals) {
                return getDelta1d2d(decimals, 1);
            }

            public String getDelta2(int decimals) {
                return getDelta2(decimals, 1);
            }

            public String getDelta2(int decimals, int factor) {
                var delta = getDelta2();
                if (delta == null) {
                    return null;
                } else {
                    return StringHelper.round(delta * factor, decimals, "Δ2d ", "", true);
                }
            }

            public Double getDelta2() {
                if (ObjectUtils.allNotNull(getDeltaX(), getDeltaY())) {
                    return Math.hypot(getDeltaX(), getDeltaY());
                } else {
                    return null;
                }
            }

            public String getDelta3(int decimals) {
                return getDelta3(decimals, 1);
            }

            public String getDelta3(int decimals, int factor) {
                var delta = getDelta3();
                if (delta == null) {
                    return null;
                } else {
                    return StringHelper.round(delta * factor, decimals, "Δ3d ", "", true);
                }
            }

            public Double getDelta3() {
                if (ObjectUtils.allNotNull(getDelta1(), getDelta2())) {
                    return Math.hypot(getDelta1(), getDelta2()) * MathHelper.sign(getDelta1());
                } else {
                    return null;
                }
            }

            public abstract Double getDeltaX();

            public String getDeltaX(int decimals) {
                var delta = getDeltaX();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔX=", "", false);
            }

            public abstract Double getDeltaY();

            public String getDeltaY(int decimals) {
                var delta = getDeltaY();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔY=", "", false);
            }

            public abstract Double getDeltaZ();

            public String getDeltaZ(int decimals) {
                var delta = getDeltaZ();
                return delta == null ? null : StringHelper.round(delta, decimals, "ΔZ=", "", true);
            }

            public String getDeltaZAbsolute(int decimals) {
                var delta = getDeltaZ();
                return delta == null ? null : StringHelper.round(Math.abs(delta), decimals, "ΔZ=", "", false);
            }
        }

        public class DeltaRolling extends Delta {

            @Override
            public Double getDeltaX() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingX(), observations.getLast().getMeasuredX())) {
                    return observations.getLast().getMeasuredX() - getRollingX();
                } else {
                    return null;
                }
            }

            @Override
            public Double getDeltaY() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingY(), observations.getLast().getMeasuredY())) {
                    return observations.getLast().getMeasuredY() - getRollingY();
                } else {
                    return null;
                }
            }

            @Override
            public Double getDeltaZ() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return null;
                }

                if (ObjectUtils.allNotNull(getRollingZ(), observations.getLast().getMeasuredZ())) {
                    return observations.getLast().getMeasuredZ() - getRollingZ();
                } else {
                    return null;
                }
            }
        }

        public class DeltaZero extends Delta {

            @Override
            public Double getDeltaX() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaX();
            }

            @Override
            public Double getDeltaY() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaY();
            }

            @Override
            public Double getDeltaZ() {
                return getObservationsTimeFiltered().isEmpty() ? null : getObservationsTimeFiltered().getLast().ext().getDeltaZ();
            }
        }

    }
}
