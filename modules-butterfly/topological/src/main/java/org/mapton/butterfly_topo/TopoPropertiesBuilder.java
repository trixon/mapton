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
import eu.hansolo.tilesfx.chart.ChartData;
import eu.hansolo.tilesfx.skins.LeaderBoardItem;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import org.mapton.butterfly_core.api.AlarmHelper;
import org.mapton.butterfly_core.api.BPropertiesBuilder;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoPropertiesBuilder extends BPropertiesBuilder<BTopoControlPoint> {

    private static final double TILE_HEIGHT = 150;
    private static final double TILE_WIDTH = 150;
    private ChartData chartData1;
    private ChartData chartData2;
    private ChartData chartData3;
    private ChartData chartData4;

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
//        loadDashboard(p);
        mDashBoardScrollPane.setFitToWidth(true);
        mSplitProperties.load(mDashBoardScrollPane, buildDetails(p));

        return mSplitProperties;
    }

    private LinkedHashMap<String, Object> buildDetails(BTopoControlPoint p) {
        var propertyMap = new LinkedHashMap<String, Object>();

//******************************************************************************
        var basicParams = new BasicParams();
        propertyMap.putAll(populateBasics(p, basicParams));
//******************************************************************************
        Double azimuth = null;
        try {
            var o = p.ext().getObservationsTimeFiltered().getLast();
            azimuth = o.ext().getBearing();
        } catch (Exception e) {
        }
        var measParams = new MeasParams<BTopoControlPoint>(
                azimuth,
                p.ext().getMeasurementUntilNext(ChronoUnit.DAYS),
                p.ext().getMeasurementAge(ChronoUnit.DAYS),
                p.ext().getNumOfObservationsFiltered(),
                p.ext().getNumOfObservations(),
                p.ext().firstIsZero(),
                p.ext().getObservationsAllRaw().stream().filter(obs -> obs.isReplacementMeasurement()).count(),
                AlarmHelper.getInstance().getLimitsAsString(BComponent.HEIGHT, p),
                AlarmHelper.getInstance().getLimitsAsString(BComponent.PLANE, p),
                p.ext().getAlarmPercentString(p.ext()),
                p.ext().getAlarmLevelAge(),
                p.ext().deltaRolling().getDelta(3),
                p.ext().deltaZero().getDelta(3)
        );
        propertyMap.putAll(populateMeas(p, measParams));
//******************************************************************************
        var dateParams = new DateParams(
                p.ext().getObservationRawFirstDate(),
                p.ext().getObservationFilteredFirstDate(),
                p.ext().getObservationRawLastDate(),
                p.ext().getObservationFilteredLastDate(),
                p.ext().getObservationRawNextDate()
        );
        propertyMap.putAll(populateDates(p, dateParams));
//******************************************************************************

//TODO Replace with trend based analysis
//
//        var category = "Analys (VARNING)";
//        try {
//            var trend = p.ext().getHeightDirectionTrendDaysMeas();
//            propertyMap.put(getCatKeyNum(category, "Trend (dagar::antal)"), "%s::%d::%d".formatted(trend[0], trend[1], trend[2]));
//
//        } catch (NullPointerException e) {
//        }
//        var speed = p.ext().getSpeed();
//        var ageIndicator = p.ext().getMeasurementAge(ChronoUnit.DAYS) > 365 ? "*" : "";
//        var speedString = "%.1f mm/%s (%.1f)%s".formatted(speed[0] * 1000.0, Dict.Time.YEAR.toLower(), speed[1], ageIndicator);
//
//        propertyMap.put(getCatKeyNum(category, Dict.SPEED.toString()), speedString);
//
//        var limitValuePredictor = p.ext().limitValuePredictor();
//        if (limitValuePredictor.getRemainingUntilLimit() != null) {
//            propertyMap.put(getCatKeyNum(category, Dict.REMAINING.toString()), StringHelper.round(limitValuePredictor.getRemainingUntilLimit() * 1000, 1, "", " mm", false));
//            var limitDate = limitValuePredictor.getExtrapolatedLimitDate();
//            if (!StringUtils.equalsAny(limitDate, "-", "E")) {
//                limitDate = "%s (%d)".formatted(limitDate, limitValuePredictor.getExtrapolatedLimitDaysFromNow());
//            }
//            propertyMap.put(getCatKeyNum(category, Dict.Time.END_DATE.toString()), limitDate);
//            var direction = limitValuePredictor.isRisingByTrend() ? Dict.INCREASEING.toString() : Dict.DECREASING.toString();
//            propertyMap.put(getCatKeyNum(category, Dict.Geometry.DIRECTION.toString()), direction);
//        }
//******************************************************************************
        propertyMap.putAll(populateDatabase(p));

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
        this.chartData1 = new ChartData(Dict.NEED.toString(), 24.0, Tile.GREEN);
        this.chartData2 = new ChartData(SDict.FREQUENCY.toString(), 10.0, Tile.BLUE);
        this.chartData3 = new ChartData(Dict.AGE.toString(), 12.0, Tile.RED);
        this.chartData4 = new ChartData("Ålder larmnivå", 13.0, Tile.YELLOW_ORANGE);

        var cycleStepTile = TileBuilder.create()
                .skinType(SkinType.CYCLE_STEP)
                .prefSize(TILE_WIDTH, TILE_HEIGHT * 1.5)
                .title("CycleStep Tile")
                .textVisible(false)
                .chartData(chartData1, chartData2, chartData3, chartData4)
                .animated(false)
                .decimals(0)
                .build();

        int row = 0;
        mDashboard.add(mNameTextTile, 0, row++, 2, 1);
        mDashboard.addRow(row++, mAlarmHGaugeTile, mAlarmPGaugeTile);
        mDashboard.addRow(row++, mAlarmHLedTile, mAlarmPLedTile);
        mDashboard.add(leaderBoardTile, 0, row++, 2, 1);
        mDashboard.add(cycleStepTile, 0, row++, 2, 1);
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

        try {
            if (p.getDimension() != BDimension._2d && p.ext().deltaZero() != null) {
                mAlarmHGaugeTile.setValue(p.ext().deltaRolling().getDelta1());
                mAlarmHGaugeTile.setValue(p.ext().deltaZero().getDelta1() * 1000);
            }
            if (p.getDimension() != BDimension._1d && p.ext().deltaZero() != null && p.ext().deltaZero().getDelta2() != null) {
                mAlarmPGaugeTile.setValue(p.ext().deltaZero().getDelta2() * 1000);
            }
        } catch (NullPointerException e) {
        }
//        mGaugeTile.setAv(p.ext().deltaZero().getDelta1());
        mDayNeedLeaderBoardItem.setValue(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        mDayFreqLeaderBoardItem.setValue(p.getFrequency());
        mDayAgeLeaderBoardItem.setValue(-1);
        mDayAgeAlarmLevelLeaderBoardItem.setValue(-1);
        chartData1.setValue(p.ext().getMeasurementUntilNext(ChronoUnit.DAYS));
        chartData2.setValue(p.getFrequency());
        chartData3.setValue(-1);
        chartData4.setValue(-1);

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
