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

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;
import javafx.geometry.Point3D;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Range;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.mapton.butterfly_format.Butterfly;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import se.trixon.almond.util.DateHelper;
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
    private boolean noSplit;
    private Integer numOfDecXY;
    private Integer numOfDecZ;
    @JsonSetter(nulls = Nulls.SKIP)
    private double offsetX = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double offsetY = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double offsetZ = Double.NaN;
    private String rollingFormula;
    @JsonSetter(nulls = Nulls.SKIP)
    private double rollingX = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double rollingY = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double rollingZ = Double.NaN;
    private String sparse;
    @JsonSetter(nulls = Nulls.SKIP)
    private double zeroX = Double.NaN;
    private transient double zeroXScaled = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double zeroY = Double.NaN;
    private transient double zeroYScaled = Double.NaN;
    @JsonSetter(nulls = Nulls.SKIP)
    private double zeroZ = Double.NaN;
    private transient double zeroZScaled = Double.NaN;

    public static HashMap<LocalDate, Point3D> createObservationMap(BXyzPoint p, DateTimeFormatter dateTimeFormatterTo, DateTimeFormatter dateTimeFormatterFrom) {
        var observationsTimeFiltered = p.extOrNull().getObservationsTimeFiltered();
        var map1 = new HashMap<LocalDate, Point3D>();

        var weekToObservations = new HashMap<String, ArrayList<Point3D>>();
        var beforeZero1 = true;
        var beforeZero3 = true;
        if (p.getDimension() == BDimension._1d) {
            var hasZero = false;
            for (var o : observationsTimeFiltered) {
                if (o.isZeroMeasurement()) {
                    hasZero = true;
                    break;
                }
            }

            for (var o : observationsTimeFiltered) {
                if (o.isZeroMeasurement()) {
                    beforeZero1 = false;
                }
                if (hasZero && beforeZero1 || Butterfly.anyNaN(o.ext().getDeltaZ())) {
                    continue;
                }
                var yyyyww = o.getDate().toLocalDate().format(dateTimeFormatterTo);
                var point3D = new Point3D(0, 0, o.ext().getDeltaZ());
                weekToObservations.computeIfAbsent(yyyyww, k -> new ArrayList<>()).add(point3D);
            }
        } else if (p.getDimension() == BDimension._3d) {
            var hasZero = false;
            for (var o : observationsTimeFiltered) {
                if (o.isZeroMeasurement()) {
                    hasZero = true;
                    break;
                }
            }

            for (var o : observationsTimeFiltered) {
                if (o.isZeroMeasurement()) {
                    beforeZero3 = false;
                }
                if (hasZero && beforeZero3 || Butterfly.anyNaN(o.ext().getDeltaX(), o.ext().getDeltaY(), o.ext().getDeltaZ())) {
                    continue;
                }
                var key = o.getDate().toLocalDate().format(dateTimeFormatterTo);
                var point3D = new Point3D(o.ext().getDeltaX(), o.ext().getDeltaY(), o.ext().getDeltaZ());
                weekToObservations.computeIfAbsent(key, k -> new ArrayList<>()).add(point3D);
            }
        }

        for (var entry : weekToObservations.entrySet()) {
            var yyyyww = entry.getKey();
            var observations = entry.getValue();
//            var x = observations.stream().mapToDouble(o -> o.getX()).average().getAsDouble();
//            var y = observations.stream().mapToDouble(o -> o.getY()).average().getAsDouble();
//            var z = observations.stream().mapToDouble(o -> o.getZ()).average().getAsDouble();

            double x = getMedian(observations.stream().mapToDouble(o -> o.getX()));
            double y = getMedian(observations.stream().mapToDouble(o -> o.getY()));
            double z = getMedian(observations.stream().mapToDouble(o -> o.getZ()));
            var point3D = new Point3D(x, y, z);

            map1.put(LocalDate.parse(yyyyww, dateTimeFormatterFrom), point3D);
        }

        return map1;
    }

    public Object ext() {
        return null;
    }

    public BXyzPoint.Ext<? extends BXyzPointObservation> extOrNull() {
        if (ext() instanceof BXyzPoint.Ext<? extends BXyzPointObservation> ext) {
            return ext;
        } else {
            return null;
        }
    }

    public String getAlarm1Id() {
        return alarm1Id;
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

    public double getOffsetX() {
        return offsetX;
    }

    public double getOffsetY() {
        return offsetY;
    }

    public double getOffsetZ() {
        return offsetZ;
    }

    @Override
    public double[] getPoint() {
        return new double[]{zeroXScaled, zeroYScaled, zeroZScaled};
    }

    public String getRollingFormula() {
        return rollingFormula;
    }

    public double getRollingX() {
        return rollingX;
    }

    public double getRollingY() {
        return rollingY;
    }

    public double getRollingZ() {
        return rollingZ;
    }

    public String getSparse() {
        return sparse;
    }

    public double getZeroX() {
        return zeroX;
    }

    public double getZeroXScaled() {
        return zeroXScaled;
    }

    public double getZeroY() {
        return zeroY;
    }

    public double getZeroYScaled() {
        return zeroYScaled;
    }

    public double getZeroZ() {
        return zeroZ;
    }

    public double getZeroZScaled() {
        return zeroZScaled;
    }

    public boolean isNoSplit() {
        return noSplit;
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

    public void setNoSplit(boolean noSplit) {
        this.noSplit = noSplit;
    }

    public void setNumOfDecXY(Integer numOfDecXY) {
        this.numOfDecXY = numOfDecXY;
    }

    public void setNumOfDecZ(Integer numOfDecZ) {
        this.numOfDecZ = numOfDecZ;
    }

    public void setOffsetX(double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(double offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public void setRollingFormula(String rollingFormula) {
        this.rollingFormula = rollingFormula;
    }

    public void setRollingX(double rollingX) {
        this.rollingX = rollingX;
    }

    public void setRollingY(double rollingY) {
        this.rollingY = rollingY;
    }

    public void setRollingZ(double rollingZ) {
        this.rollingZ = rollingZ;
    }

    public void setSparse(String sparse) {
        this.sparse = sparse;
    }

    public void setZeroX(double zeroX) {
        this.zeroX = zeroX;
    }

    public void setZeroXScaled(double zeroXScaled) {
        this.zeroXScaled = zeroXScaled;
    }

    public void setZeroY(double zeroY) {
        this.zeroY = zeroY;
    }

    public void setZeroYScaled(double zeroYScaled) {
        this.zeroYScaled = zeroYScaled;
    }

    public void setZeroZ(double zeroZ) {
        this.zeroZ = zeroZ;
    }

    public void setZeroZScaled(double zeroZScaled) {
        this.zeroZScaled = zeroZScaled;
    }

    public abstract class Ext<T extends BXyzPointObservation> extends BBasePoint.Ext<T> {

        private transient final DeltaFirst deltaFirst = new DeltaFirst();
        private transient final DeltaRolling deltaRolling = new DeltaRolling();
        private transient final DeltaZero deltaZero = new DeltaZero();
        private transient double mFrequenceHighBuffer;
        private TreeMap<LocalDateTime, Point3D> mReplacements = new TreeMap<>();

        public void calculateObservations(List<T> observations) {
            mReplacements.clear();
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

            var localZeroX = latestZero.getMeasuredX();
            var localZeroY = latestZero.getMeasuredY();
            var localZeroZ = latestZero.getMeasuredZ();
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

                            if (!Double.isNaN(oo.ext().getDeltaX())) {
                                oo.ext().setDeltaX(oo.ext().getDeltaX() + accumulatedReplacementsX);
                            }
                            if (!Double.isNaN(oo.ext().getDeltaY())) {
                                oo.ext().setDeltaY(oo.ext().getDeltaY() + accumulatedReplacementsY);
                            }
                            if (!Double.isNaN(oo.ext().getDeltaZ())) {
                                oo.ext().setDeltaZ(oo.ext().getDeltaZ() + accumulatedReplacementsZ);
                            }
                        }
                    }
                    accumulatedReplacementsX = 0.0;
                    accumulatedReplacementsY = 0.0;
                    accumulatedReplacementsZ = 0.0;
                }
                if (Butterfly.allNotNaN(measuredX, localZeroX)) {
                    o.ext().setDeltaX(measuredX - localZeroX - accumulatedReplacementsX);
                }
                if (Butterfly.allNotNaN(measuredY, localZeroY)) {
                    o.ext().setDeltaY(measuredY - localZeroY - accumulatedReplacementsY);
                }
                if (Butterfly.allNotNaN(measuredZ, localZeroZ)) {
                    o.ext().setDeltaZ(measuredZ - localZeroZ - accumulatedReplacementsZ);
                }

                if (o.isReplacementMeasurement() && i > 0) {
                    var prev = observations.get(i - 1);
                    var prevX = prev.getMeasuredX();
                    var prevY = prev.getMeasuredY();
                    var prevZ = prev.getMeasuredZ();

                    var replacementX = 0d;
                    var replacementY = 0d;
                    var replacementZ = 0d;

                    if (Butterfly.allNotNaN(measuredX, prevX, o.ext().getDeltaX())) {
                        replacementX = measuredX - prevX;
                        o.ext().setDeltaX(o.ext().getDeltaX() - replacementX);
                        accumulatedReplacementsX = accumulatedReplacementsX + replacementX;
                    }

                    if (Butterfly.allNotNaN(measuredY, prevY, o.ext().getDeltaY())) {
                        replacementY = measuredY - prevY;
                        o.ext().setDeltaY(o.ext().getDeltaY() - replacementY);
                        accumulatedReplacementsY = accumulatedReplacementsY + replacementY;
                    }

                    if (Butterfly.allNotNaN(measuredZ, prevZ, o.ext().getDeltaZ())) {
                        replacementZ = measuredZ - prevZ;
                        o.ext().setDeltaZ(o.ext().getDeltaZ() - replacementZ);
                        accumulatedReplacementsZ = accumulatedReplacementsZ + replacementZ;
                    }

                    mReplacements.put(o.getDate(), new Point3D(replacementX, replacementY, replacementZ));
                }

                if (!Double.isNaN(offsetZ) && offsetZ != 0d) {
                    o.ext().setDeltaZ(o.ext().getDeltaZ() + offsetZ);
                }

                o.ext().setAccuX(accumulatedReplacementsX);
                o.ext().setAccuY(accumulatedReplacementsY);
                o.ext().setAccuZ(accumulatedReplacementsZ);
            }
        }

        public DeltaFirst deltaFirst() {
            return deltaFirst;
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
                            return Strings.CS.equals(a.getId(), getAlarm1Id());
                        } else {
                            return Strings.CS.equals(a.getId(), getAlarm2Id());
                        }
                    }).findAny().orElse(null);

            return alarm;
        }

        public int getAlarmLevel(BComponent component, BXyzPointObservation o) {
            try {
                return getAlarmLevel(component, component == BComponent.HEIGHT ? o.ext().getDeltaZ() : o.ext().getDelta2d());
            } catch (Exception e) {
                return -1;
            }
        }

        public int getAlarmLevel(BComponent component, double delta) {
            var alarm = getAlarm(component);

            if (alarm == null || Double.isNaN(delta)) {
                return -1;
            } else {
                return alarm.ext().getLevel(delta);
            }
        }

        public int getAlarmLevel() {
            return getAlarmLevel(getObservationFilteredLast());
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

        public Integer getAlarmPercent(BComponent component, double delta) {
            if (Double.isNaN(delta)) {
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
                if (alarm.ext().getRange2() != null || alarm.ext().getRange1() != null) {
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
            if (!Double.isNaN(delta)) {
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

        public String getAlarmRatios(BComponent component) {
            var alarm = getAlarm(component);
            if (alarm != null) {
                return StringHelper.joinNonNulls("//", alarm.getRatio1s(), alarm.getRatio2s(), alarm.getRatio3s());
            } else {
                return "";
            }
        }

        public String getAlarmRatios(BComponent component, int level) {
            var alarm = getAlarm(component);
            if (alarm != null) {
                switch (level) {
                    case 1:
                        return alarm.getRatio1s();
                    case 2:
                        return alarm.getRatio2s();
                    case 3:
                        return alarm.getRatio3s();
                    default:
                        throw new AssertionError();
                }
            } else {
                return "";
            }
        }

        public long getFirstMeasurementAge(ChronoUnit chronoUnit) {
            if (getDateFirst() != null) {
                return chronoUnit.between(getDateFirst().toLocalDate(), LocalDate.now());
            } else {
                return -1L;
            }
        }

        public double getFrequenceHighBuffer() {
            return mFrequenceHighBuffer;
        }

        public long getMeasurementUntilNext(ChronoUnit chronoUnit) {
            var latest = getDateLatest() != null ? getDateLatest().toLocalDate() : LocalDate.MIN;

            var nextMeas = latest;
            if (!latest.isEqual(LocalDate.MIN) && !latest.isEqual(LocalDate.MAX)) {
                nextMeas = latest.plusDays(getFrequency());
            }

            return chronoUnit.between(LocalDate.now(), nextMeas);
        }

        public int getNumOfReplacements() {
            return (int) getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count();
        }

        public int getNumOfReplacementsAfterZero() {
            var num = 0;
            var zeroFound = false;
            for (var o : getObservationsAllRaw()) {
                if (o.isZeroMeasurement()) {
                    zeroFound = true;
                }

                if (zeroFound && o.isReplacementMeasurement()) {
                    num++;
                }
            }

            return num;
        }

        public LocalDate getObservationRawNextDate() {
            if (ObjectUtils.allNotNull(getObservationRawLastDate(), getFrequency()) && getFrequency() != 0) {
                return getObservationRawLastDate().plusDays(getFrequency());
            } else {
                return null;
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

        public void setFrequenceHighBuffer(double frequenceHighBuffer) {
            this.mFrequenceHighBuffer = frequenceHighBuffer;
        }

        private double rounder(double d) {
            var snap = 3;
            return snap * (Math.round(d * 1000 / snap));
        }

        public abstract class Delta {

            public String getDelta(int decimals, int factor) {
                return StringHelper.joinNonNulls(", ",
                        getDelta1(decimals, factor, true),
                        getDelta2(decimals, factor, true),
                        getDelta3(decimals, factor)
                );
            }

            public String getDelta(int decimals) {
                return getDelta(decimals, 1);
            }

            public double getDelta() {
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

                return Double.NaN;
            }

            public double getDelta1() {
                return getDeltaZ();
            }

            public String getDelta1(int decimals) {
                return getDelta1(decimals, 1, false);
            }

            public String getDelta1(int decimals, int factor, boolean usePrefix) {
                var delta = getDelta1();
                if (Double.isNaN(delta)) {
                    return null;
                } else {
                    var prefix = usePrefix ? "Δ1d " : "";
                    return StringHelper.round(delta * factor, decimals, prefix, "", true);
                }
            }

            public String getDelta1d2d(int decimals, int factor) {
                return switch (dimension) {
                    case _1d ->
                        getDelta1(decimals, factor, true);
                    case _2d ->
                        getDelta2(decimals, factor, false);
                    default ->
                        StringHelper.joinNonNulls(", ",
                        getDelta1(decimals, factor, true),
                        getDelta2(decimals, factor, true)
                        );
                };
            }

            public String getDelta1d2d(int decimals) {
                return getDelta1d2d(decimals, 1);
            }

            public String getDelta2(int decimals) {
                return getDelta2(decimals, 1, false);
            }

            public String getDelta2(int decimals, int factor, boolean usePrefix) {
                var delta = getDelta2();
                if (Double.isNaN(delta)) {
                    return null;
                } else {
                    var prefix = usePrefix ? "Δ2d " : "";
                    return StringHelper.round(delta * factor, decimals, prefix, "", true);
                }
            }

            public double getDelta2() {
                if (Butterfly.allNotNaN(getDeltaX(), getDeltaY())) {
                    return Math.hypot(getDeltaX(), getDeltaY());
                } else {
                    return Double.NaN;
                }
            }

            public String getDelta3(int decimals) {
                return getDelta3(decimals, 1);
            }

            public String getDelta3(int decimals, int factor) {
                var delta = getDelta3();
                if (Double.isNaN(delta)) {
                    return null;
                } else {
                    return StringHelper.round(delta * factor, decimals, "Δ3d ", "", true);
                }
            }

            public double getDelta3() {
                if (Butterfly.allNotNaN(getDelta1(), getDelta2())) {
                    return Math.hypot(getDelta1(), getDelta2()) * MathHelper.sign(getDelta1());
                } else {
                    return Double.NaN;
                }
            }

            public abstract double getDeltaX();

            public String getDeltaX(int decimals) {
                var delta = getDeltaX();
                return Double.isNaN(delta)
                        ? null
                        : StringHelper.round(delta, decimals, "ΔX=", "", false);
            }

            public abstract double getDeltaY();

            public String getDeltaY(int decimals) {
                var delta = getDeltaY();
                return Double.isNaN(delta)
                        ? null
                        : StringHelper.round(delta, decimals, "ΔY=", "", false);
            }

            public abstract double getDeltaZ();

            public String getDeltaZ(int decimals) {
                var delta = getDeltaZ();
                return Double.isNaN(delta)
                        ? null
                        : StringHelper.round(delta, decimals, "ΔZ=", "", true);
            }

            public String getDeltaZAbsolute(int decimals) {
                var delta = getDeltaZ();
                return Double.isNaN(delta)
                        ? null
                        : StringHelper.round(Math.abs(delta), decimals, "ΔZ=", "", false);
            }
        }

        public class DeltaFirst extends Delta {

            @Override
            public double getDeltaX() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                var firstMeasured = observations.getFirst().getMeasuredX();
                var lastMeasured = observations.getLast().getMeasuredX();

                var replacement = mReplacements.entrySet().stream()
                        .filter(entry -> DateHelper.isBetween(observations.getFirst().getDate(), observations.getLast().getDate(), entry.getKey()))
                        .mapToDouble(entry -> entry.getValue().getX())
                        .sum();

                if (Butterfly.allNotNaN(firstMeasured, lastMeasured)) {
                    return lastMeasured - firstMeasured - replacement;
                } else {
                    return Double.NaN;
                }
            }

            @Override
            public double getDeltaY() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                var firstMeasured = observations.getFirst().getMeasuredY();
                var lastMeasured = observations.getLast().getMeasuredY();
                var replacement = mReplacements.entrySet().stream()
                        .filter(entry -> DateHelper.isBetween(observations.getFirst().getDate(), observations.getLast().getDate(), entry.getKey()))
                        .mapToDouble(entry -> entry.getValue().getY())
                        .sum();

                if (Butterfly.allNotNaN(firstMeasured, lastMeasured)) {
                    return lastMeasured - firstMeasured - replacement;
                } else {
                    return Double.NaN;
                }
            }

            @Override
            public double getDeltaZ() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                var firstMeasured = observations.getFirst().getMeasuredZ();
                var lastMeasured = observations.getLast().getMeasuredZ();
                var replacement = mReplacements.entrySet().stream()
                        .filter(entry -> DateHelper.isBetween(observations.getFirst().getDate(), observations.getLast().getDate(), entry.getKey()))
                        .mapToDouble(entry -> entry.getValue().getZ())
                        .sum();

                if (Butterfly.allNotNaN(firstMeasured, lastMeasured)) {
                    return lastMeasured - firstMeasured - replacement;
                } else {
                    return Double.NaN;
                }
            }
        }

        public class DeltaRolling extends Delta {

            @Override
            public double getDeltaX() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                if (Butterfly.allNotNaN(getRollingX(), observations.getLast().getMeasuredX())) {
                    return observations.getLast().getMeasuredX() - getRollingX();
                } else {
                    return Double.NaN;
                }
            }

            @Override
            public double getDeltaY() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                if (Butterfly.allNotNaN(getRollingY(), observations.getLast().getMeasuredY())) {
                    return observations.getLast().getMeasuredY() - getRollingY();
                } else {
                    return Double.NaN;
                }
            }

            @Override
            public double getDeltaZ() {
                var observations = getObservationsTimeFiltered();
                if (observations == null || observations.isEmpty()) {
                    return Double.NaN;
                }

                if (Butterfly.allNotNaN(getRollingZ(), observations.getLast().getMeasuredZ())) {
                    return observations.getLast().getMeasuredZ() - getRollingZ();
                } else {
                    return Double.NaN;
                }
            }
        }

        public class DeltaZero extends Delta {

            @Override
            public double getDeltaX() {
                return getObservationsTimeFiltered().isEmpty() ? Double.NaN : getObservationsTimeFiltered().getLast().ext().getDeltaX();
            }

            @Override
            public double getDeltaY() {
                return getObservationsTimeFiltered().isEmpty() ? Double.NaN : getObservationsTimeFiltered().getLast().ext().getDeltaY();
            }

            @Override
            public double getDeltaZ() {
                return getObservationsTimeFiltered().isEmpty() ? Double.NaN : getObservationsTimeFiltered().getLast().ext().getDeltaZ();
            }
        }

    }
}
