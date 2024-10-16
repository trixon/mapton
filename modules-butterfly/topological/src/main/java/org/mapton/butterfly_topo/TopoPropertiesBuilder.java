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
package org.mapton.butterfly_topo;

import eu.hansolo.tilesfx.Section;
import eu.hansolo.tilesfx.SectionBuilder;
import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.Tile.SkinType;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.ui.forms.PropertiesBuilder;
import org.mapton.butterfly_alarm.api.AlarmHelper;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoPropertiesBuilder extends PropertiesBuilder<BTopoControlPoint> {

    private static final double TILE_HEIGHT = 150;
    private static final double TILE_WIDTH = 150;

    private Tile mAlarmHGaugeTile;
    private Tile mAlarmHLedTile;
    private Tile mAlarmPGaugeTile;
    private Tile mAlarmPLedTile;
    private LeaderBoardItem mDayAgeAlarmLevelLeaderBoardItem;
    private LeaderBoardItem mDayAgeLeaderBoardItem;
    private LeaderBoardItem mDayFreqLeaderBoardItem;
    private LeaderBoardItem mDayNeedLeaderBoardItem;
    private Tile mNameTextTile;
    private final double mPadding = FxHelper.getUIScaled(8);
    private final GridPane mDashboard = new GridPane(mPadding, mPadding);
    private final ScrollPane mDashBoardScrollPane = new ScrollPane(mDashboard);
    private final SplitProperties mSplitProperties = new SplitProperties();

    public TopoPropertiesBuilder() {
        initDashBoard();
    }

    @Override
    public Object build(BTopoControlPoint p) {
        if (p == null) {
            return p;
        }
        loadDashboard(p);
        mDashBoardScrollPane.setFitToWidth(true);
        mSplitProperties.load(mDashBoardScrollPane, buildDetails(p));

        return mSplitProperties;
    }

    private LinkedHashMap<String, Object> buildDetails(BTopoControlPoint p) {
        var propertyMap = new LinkedHashMap<String, Object>();
        var cat1 = Dict.BASIC.toString();
        propertyMap.put(getCatKey(cat1, Dict.NAME.toString()), p.getName());
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.STATUS.toString(), SDict.DIMENSION.toString())),
                StringHelper.join(SEPARATOR, "", p.getStatus(), p.getDimension().getName()));
        propertyMap.put(getCatKey(cat1,
                StringHelper.join(SEPARATOR, "", Dict.GROUP.toString(), Dict.CATEGORY.toString())),
                StringHelper.join(SEPARATOR, "", p.getGroup(), p.getCategory()));
        propertyMap.put(getCatKey(cat1, SDict.OPERATOR.toString()), p.getOperator());
        propertyMap.put(getCatKey(cat1, Dict.ORIGIN.toString()), p.getOrigin());
        propertyMap.put(getCatKey(cat1, Dict.COMMENT.toString()), p.getComment());
        propertyMap.put(getCatKey(cat1, SDict.ALARM.toString()), StringHelper.join(SEPARATOR, "", p.getNameOfAlarmHeight(), p.getNameOfAlarmPlane()));
        propertyMap.put(getCatKey(cat1, Dict.Geometry.HEIGHT.toString()), AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p));
        propertyMap.put(getCatKey(cat1, Dict.Geometry.PLANE.toString()), AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p));
        var measurements = "%d / %d    (%d - %d)".formatted(
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isZeroMeasurement()).count(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count()
        );
        String validFromTo = null;
        if (ObjectUtils.anyNotNull(p.getDateValidFrom(), p.getDateValidTo())) {
            var fromDat = Objects.toString(DateHelper.toDateString(p.getDateValidFrom()), "1970-01-01");
            var toDat = Objects.toString(DateHelper.toDateString(p.getDateValidTo()), "2099-12-31");
            validFromTo = StringHelper.joinNonNulls(" // ", fromDat, toDat);
        }
        propertyMap.put(getCatKey(cat1, SDict.MEASUREMENTS.toString()), measurements);
        propertyMap.put(getCatKey(cat1, "%s %s - %s".formatted(Dict.VALID.toString(), Dict.FROM.toLower(), Dict.TO.toLower())), validFromTo);

        var firstRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawFirstDate()), "-");
        var firstFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredFirstDate()), "-");
        var lastRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawLastDate()), "-");
        var lastFiltered = Objects.toString(DateHelper.toDateString(p.ext().getObservationFilteredLastDate()), "-");
        var nextRaw = Objects.toString(DateHelper.toDateString(p.ext().getObservationRawNextDate()), "-");

        propertyMap.put(getCatKey(cat1, SDict.FREQUENCY.toString()), p.getFrequency());
        var need = p.getFrequency() == 0 ? "-" : Long.toString(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, Dict.NEED.toString()), need);
        propertyMap.put(getCatKey(cat1, Dict.AGE.toString()), p.ext().getMeasurementAge(ChronoUnit.DAYS));
        propertyMap.put(getCatKey(cat1, "%s, %s".formatted(Dict.AGE.toString(), SDict.ALARM_LEVEL.toLower())), p.ext().getAlarmLevelAge());
        propertyMap.put(getCatKey(cat1, Dict.LATEST.toString()),
                "%s (%s)".formatted(lastRaw, lastFiltered)
        );
        propertyMap.put(getCatKey(cat1, Dict.NEXT.toString()), nextRaw);
        propertyMap.put(getCatKey(cat1, Dict.REFERENCE.toString()),
                "%s (%s)".formatted(
                        Objects.toString(DateHelper.toDateString(p.getDateZero()), "-"),
                        Objects.toString(DateHelper.toDateString(p.getDateRolling()), "-"))
        );
        propertyMap.put(getCatKey(cat1, Dict.FIRST.toString()),
                "%s (%s)".formatted(firstRaw, firstFiltered)
        );
        var delta = "Δ ";
        propertyMap.put(getCatKey(cat1, delta + SDict.ROLLING.toString()), p.ext().deltaRolling().getDelta(3));
        propertyMap.put(getCatKey(cat1, delta + Dict.REFERENCE.toString()), p.ext().deltaZero().getDelta(3));
        var speed = p.ext().getSpeed();
        var ageIndicator = p.ext().getMeasurementAge(ChronoUnit.DAYS) > 365 ? "*" : "";
        var speedString = "%.1f mm/%s (%.1f)%s".formatted(speed[0] * 1000.0, Dict.Time.YEAR.toLower(), speed[1], ageIndicator);

        propertyMap.put(getCatKey(cat1, Dict.SPEED.toString()), speedString);

        var limitValuePredictor = p.ext().limitValuePredictor();
        if (limitValuePredictor.getRemainingUntilLimit() != null) {
            propertyMap.put(getCatKey(cat1, Dict.REMAINING.toString()), StringHelper.round(limitValuePredictor.getRemainingUntilLimit() * 1000, 1, "", " mm", false));
            var limitDate = limitValuePredictor.getExtrapolatedLimitDate();
            if (!StringUtils.equalsAny(limitDate, "-", "E")) {
                limitDate = "%s (%d)".formatted(limitDate, limitValuePredictor.getExtrapolatedLimitDaysFromNow());
            }
            propertyMap.put(getCatKey(cat1, Dict.Time.END_DATE.toString()), limitDate);
            var direction = limitValuePredictor.isRisingByTrend() ? Dict.INCREASEING.toString() : Dict.DECREASING.toString();
            propertyMap.put(getCatKey(cat1, Dict.Geometry.DIRECTION.toString()), direction);
        }
        propertyMap.put(getCatKey(cat1, "N"), StringHelper.round(p.getZeroY(), 3));
        propertyMap.put(getCatKey(cat1, "E"), StringHelper.round(p.getZeroX(), 3));
        propertyMap.put(getCatKey(cat1, "H"), StringHelper.round(p.getZeroZ(), 3));

        Double bearing = null;
        try {
            var o = p.ext().getObservationsTimeFiltered().getLast();
            bearing = o.ext().getBearing();
        } catch (Exception e) {
        }

        propertyMap.put(getCatKey(cat1, Dict.BEARING.toString()), StringHelper.round(bearing, 0));

        propertyMap.put(getCatKey(cat1, Dict.CREATED.toString()), DateHelper.toDateString(p.getDateCreated()));
        propertyMap.put(getCatKey(cat1, Dict.CHANGED.toString()), DateHelper.toDateString(p.getDateChanged()));

        return propertyMap;
    }

    private void initDashBoard() {
        mNameTextTile = TileBuilder.create()
                .backgroundColor(Color.DARKBLUE)
                .descriptionAlignment(Pos.TOP_LEFT)
                .skinType(SkinType.TEXT)
                .prefSize(TILE_WIDTH * 2, TILE_HEIGHT * 2)
                .textVisible(true)
                .build();

        mAlarmHLedTile = TileBuilder.create()
                .skinType(SkinType.LED)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                //                .prefWidth(TILE_WIDTH)
                .title(Dict.Geometry.HEIGHT.toString())
                .build();

        mAlarmPLedTile = TileBuilder.create()
                .skinType(SkinType.LED)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                //                .prefWidth(TILE_WIDTH)
                .title(Dict.Geometry.PLANE.toString())
                .build();

        mAlarmHGaugeTile = TileBuilder.create()
                .skinType(SkinType.GAUGE)
                .prefSize(TILE_WIDTH, TILE_WIDTH)
                .animated(false)
                .decimals(0)
                .sectionsVisible(true)
                .sectionsAlwaysVisible(true)
                .unit("mm")
                .oldValueVisible(true)
                .build();

        mAlarmPGaugeTile = TileBuilder.create()
                .skinType(SkinType.GAUGE)
                .prefSize(TILE_WIDTH, TILE_WIDTH)
                .animated(false)
                .decimals(0)
                .sectionsVisible(true)
                .sectionsAlwaysVisible(true)
                .unit("mm")
                .oldValueVisible(true)
                .build();

        mDayNeedLeaderBoardItem = new LeaderBoardItem(Dict.NEED.toString());
        mDayFreqLeaderBoardItem = new LeaderBoardItem(SDict.FREQUENCY.toString());
        mDayAgeLeaderBoardItem = new LeaderBoardItem(Dict.AGE.toString());
        mDayAgeAlarmLevelLeaderBoardItem = new LeaderBoardItem("Ålder Larmnivå");

        var leaderBoardTile = TileBuilder.create()
                .skinType(SkinType.LEADER_BOARD)
                .prefSize(TILE_WIDTH, TILE_HEIGHT * 1.5)
                .title("Dagar")
                .animated(false)
                .decimals(0)
                .itemSorting(Tile.ItemSorting.NONE)
                .leaderBoardItems(mDayNeedLeaderBoardItem, mDayFreqLeaderBoardItem, mDayAgeLeaderBoardItem, mDayAgeAlarmLevelLeaderBoardItem)
                .build();

        leaderBoardTile.setInteractive(false);

        int row = 0;
        mDashboard.add(mNameTextTile, 0, row++, 2, 1);
        mDashboard.addRow(row++, mAlarmHLedTile, mAlarmPLedTile);
        mDashboard.addRow(row++, mAlarmHGaugeTile, mAlarmPGaugeTile);
        mDashboard.add(leaderBoardTile, 0, row++, 2, 1);
        FxHelper.autoSizeColumn(mDashboard, 2);
        mDashboard.setPadding(FxHelper.getUIScaledInsets(mPadding));
    }

    private Node loadDashboard(BTopoControlPoint p) {
        var desc = String.join("\n",
                p.getName(),
                p.getStatus(),
                p.getGroup(),
                p.getCategory()
        );
        mNameTextTile.setDescription(desc);

        var hasHeight = p.getDimension() != BDimension._2d;
        var hasPlane = p.getDimension() != BDimension._1d;

        setLedState(mAlarmHLedTile, hasHeight);
        setLedState(mAlarmPLedTile, hasPlane);
        setGaugeState(mAlarmHGaugeTile, hasHeight);
        setGaugeState(mAlarmPGaugeTile, hasPlane);

        if (hasHeight) {
            mAlarmHLedTile.setActiveColor(TopoHelper.getAlarmColorHeightFx(p));
        } else {

        }

        if (hasPlane) {
            mAlarmPLedTile.setActiveColor(TopoHelper.getAlarmColorPlaneFx(p));
        } else {
        }

        mAlarmHLedTile.setDescription(AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p));
        mAlarmPLedTile.setDescription(AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p));
//        mAlarmHTile.setDisable(p.getDimension() == BDimension._2d);
//        mAlarmPTile.setDisable(p.getDimension() == BDimension._1d);

        populateAlarmSectionsH(p.ext().getAlarm(BComponent.HEIGHT));
        populateAlarmSectionsP(p.ext().getAlarm(BComponent.PLANE));

        if (p.getDimension() != BDimension._2d && p.ext().deltaZero() != null) {
            mAlarmHGaugeTile.setValue(p.ext().deltaRolling().getDelta1());
            mAlarmHGaugeTile.setValue(p.ext().deltaZero().getDelta1() * 1000);
        }
        if (p.getDimension() != BDimension._1d && p.ext().deltaZero() != null && p.ext().deltaZero().getDelta2() != null) {
            mAlarmPGaugeTile.setValue(p.ext().deltaZero().getDelta2() * 1000);
        }
//        mGaugeTile.setAv(p.ext().deltaZero().getDelta1());
        mDayNeedLeaderBoardItem.setValue(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        mDayFreqLeaderBoardItem.setValue(p.getFrequency());
        mDayAgeLeaderBoardItem.setValue(-1);
        mDayAgeAlarmLevelLeaderBoardItem.setValue(-1);

        return mDashboard;
    }

    private void populateAlarmSectionsH(BAlarm alarm) {
        var tile = mAlarmHGaugeTile;
        var sections = new ArrayList<Section>();
        tile.setSections(sections);
        if (alarm == null) {
            return;
        }

        double start0 = 0;
        double stop0 = 0;
        double start1a = 0;
        double stop1a = 0;
        double start1b = 0;
        double stop1b = 0;
        double start2a = 0;
        double stop2a = 0;
        double start2b = 0;
        double stop2b = 0;

        if (alarm.ext().getRange0() != null) {
            start0 = alarm.ext().getRange0().getMinimum() * 1000;
            stop0 = alarm.ext().getRange0().getMaximum() * 1000;
            sections.add(SectionBuilder.create().start(start0).stop(stop0).color(Color.GREEN).build());
        }

        if (alarm.ext().getRange1() != null) {
            start1a = alarm.ext().getRange1().getMinimum() * 1000;
            stop1a = start0;
            sections.add(SectionBuilder.create().start(start1a).stop(stop1a).color(Color.YELLOW).build());

            start1b = stop0;
            stop1b = alarm.ext().getRange1().getMaximum() * 1000;
            sections.add(SectionBuilder.create().start(start1b).stop(stop1b).color(Color.YELLOW).build());
            //
            double span1 = Math.max(stop1a - start1a, stop1b - start1b);
            start2a = start1a - span1;
            stop2a = start1a;
            sections.add(SectionBuilder.create().start(start2a).stop(stop2a).color(Color.RED).build());

            start2b = stop1b;
            stop2b = start2b + span1;
            sections.add(SectionBuilder.create().start(start2b).stop(stop2b).color(Color.RED).build());
        }

        var limit = Math.max(start2a * -1, stop2b);
        tile.setSections(sections);
        tile.setMinValue(-limit);
        tile.setMaxValue(limit);
    }

    private void populateAlarmSectionsP(BAlarm alarm) {
        var tile = mAlarmPGaugeTile;
        var sections = new ArrayList<Section>();
        tile.setSections(sections);
        if (alarm == null) {
            return;
        }

        double start0 = 0;
        double stop0 = 0;
        double start1 = 0;
        double stop1 = 0;
        double start2 = 0;
        double stop2 = 0;

        if (alarm.ext().getRange0() != null) {
            start0 = 0;
            stop0 = alarm.ext().getRange0().getMaximum() * 1000;
            sections.add(SectionBuilder.create().start(start0).stop(stop0).color(Color.GREEN).build());
        }

        if (alarm.ext().getRange1() != null) {
            start1 = stop0;
            stop1 = alarm.ext().getRange1().getMaximum() * 1000;
            sections.add(SectionBuilder.create().start(start1).stop(stop1).color(Color.YELLOW).build());
            //
            double span1 = stop1 - start1;
            start2 = stop1;
            stop2 = start2 + span1;
            sections.add(SectionBuilder.create().start(start2).stop(stop2).color(Color.RED).build());
        }

        tile.setSections(sections);
        tile.setMinValue(0);
        tile.setMaxValue(stop2);
    }

    private void setGaugeState(Tile tile, boolean state) {
        tile.setValueVisible(state);
        tile.setMinValueVisible(state);
        tile.setMaxValueVisible(state);

        if (state) {
            //
            tile.setNeedleColor(Color.LIGHTGRAY);
            tile.setUnitColor(Color.LIGHTGRAY);
        } else {
            tile.clearSections();
            tile.setNeedleColor(Color.TRANSPARENT);
            tile.setUnitColor(Color.TRANSPARENT);

            tile.setMinValue(0);
            tile.setMaxValue(0.0001);
            tile.setValue(0);
        }
    }

    private void setLedState(Tile tile, boolean state) {
        tile.setActive(state);
        if (state) {

        } else {
            tile.setActiveColor(Color.TRANSPARENT);
        }

    }
}
