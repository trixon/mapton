/*
 * Copyright 2020 Patrik Karlström.
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
package org.mapton.poi_trv_ti;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
import eu.hansolo.tilesfx.colors.Bright;
import eu.hansolo.tilesfx.colors.Dark;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Locale;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MGenericLoader;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Measurement;
import se.trixon.trv_traffic_information.road.weatherstation.v1.WeatherStation;

/**
 *
 * @author Patrik Karlström
 */
public class WeatherView extends BorderPane implements MGenericLoader<WeatherStation> {

    private static final double TILE_SIZE = 150;
    private static final double TILE_WIDTH = 150;
    private static final double TILE_HEIGHT = 150;

    private Label mTitleLabel = new Label();
    private Label mTimeLabel = new Label();
    private Label mAirTempLabel = new Label();
    private Label mRoadTempLabel = new Label();
    private final ZoneId mZoneOffset = ZoneOffset.systemDefault();
    private Tile fluidTile;
    private Tile barGaugeTile;
    private Tile highLowTile;

    public WeatherView() {
        createUI();
    }

    @Override
    public void load(WeatherStation weatherStation) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(weatherStation.getModifiedTime().toString());
        mTitleLabel.setText(String.format("%s %d, %s", "VÄG", weatherStation.getRoadNumberNumeric(), weatherStation.getName()));
        mTimeLabel.setText(offsetDateTime.atZoneSameInstant(mZoneOffset).toLocalDateTime().toString());
        final Measurement measurement = weatherStation.getMeasurement();
        mAirTempLabel.setText(String.format("%.1f° C", measurement.getAir().getTemp()));
        mRoadTempLabel.setText(String.format("%.1f° C", measurement.getRoad().getTemp()));

        fluidTile.setValue(measurement.getAir().getRelativeHumidity());
        try {
            barGaugeTile.setValue(measurement.getWind().getForce());
            barGaugeTile.setThreshold(measurement.getWind().getForceMax());
            barGaugeTile.setDisable(false);
            highLowTile.setValue(measurement.getWind().getForce());
            highLowTile.setReferenceValue(measurement.getWind().getForceMax());
        } catch (Exception e) {
            barGaugeTile.setValue(0);
            barGaugeTile.setThreshold(0);
            barGaugeTile.setDisable(true);
        }
        debug(weatherStation);
    }

    private void createUI() {
        setPadding(FxHelper.getUIScaledInsets(8));
        String style = "-fx-font-weight: %s; -fx-font-style: %s; -fx-font-size: %.0fpx";
        final double defaultSize = Font.getDefault().getSize();

        mTitleLabel.setStyle(String.format(style, "bold", "normal", defaultSize * 1.2));
        setTop(mTitleLabel);

        Label airTempLabel = new Label("LUFTTEMPERATUR");
        Label roadTempLabel = new Label("ROADTEMPERATUR");

        final String headerStyle = String.format(style, "normal", "italic", defaultSize);
        final String valueStyle = String.format(style, "bold", "normal", defaultSize);
        airTempLabel.setStyle(headerStyle);
        mAirTempLabel.setStyle(valueStyle);
        roadTempLabel.setStyle(headerStyle);
        mRoadTempLabel.setStyle(valueStyle);
        GridPane gp = new GridPane();
        int row = 0;
        gp.add(mTimeLabel, 0, row++, 2, 1);
        gp.addRow(row++, airTempLabel, mAirTempLabel);
        gp.addRow(row++, roadTempLabel, mRoadTempLabel);

        FxHelper.setPadding(FxHelper.getUIScaledInsets(0, 8, 0, 0), airTempLabel, roadTempLabel);

        var clockTile = TileBuilder.create()
                .prefSize(TILE_SIZE, TILE_SIZE * 2)
                .skinType(Tile.SkinType.CLOCK)
                .title("Clock Tile")
                .text("Whatever text")
                .dateVisible(true)
                .locale(Locale.US)
                .running(true)
                .build();

        fluidTile = TileBuilder.create().skinType(Tile.SkinType.FLUID)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .title("FluidTileSkin")
                .text("Waterlevel")
                .unit("\u0025")
                .decimals(0)
                .barColor(Tile.BLUE) // defines the fluid color, alternatively use sections or gradientstops
                .animated(true)
                .build();
        barGaugeTile = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_GAUGE)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .minValue(0)
                .maxValue(40)
                .startFromZero(true)
                .threshold(80)
                .thresholdVisible(true)
                .title("BarGauge Tile")
                .unit("F")
                .text("Whatever text")
                .gradientStops(new Stop(0, Bright.BLUE),
                        new Stop(0.1, Bright.BLUE_GREEN),
                        new Stop(0.2, Bright.GREEN),
                        new Stop(0.3, Bright.GREEN_YELLOW),
                        new Stop(0.4, Bright.YELLOW),
                        new Stop(0.5, Bright.YELLOW_ORANGE),
                        new Stop(0.6, Bright.ORANGE),
                        new Stop(0.7, Bright.ORANGE_RED),
                        new Stop(0.8, Bright.RED),
                        new Stop(1.0, Dark.RED))
                .strokeWithGradient(true)
                .animated(true)
                .build();
        highLowTile = TileBuilder.create()
                .skinType(Tile.SkinType.HIGH_LOW)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .title("WIND SPEED")
                .unit("m/s")
                .description("\u2197")
                //                .text("Whatever text")
                .referenceValue(6.7)
                .value(8.2)
                .build();

        gp.add(fluidTile, 0, row++, GridPane.REMAINING, 1);
        gp.add(barGaugeTile, 0, row++, GridPane.REMAINING, 1);
        gp.add(highLowTile, 0, row++, GridPane.REMAINING, 1);

        setCenter(gp);

    }

    private void debug(Object o) {
        try {
            System.out.println(ToStringBuilder.reflectionToString(o, ToStringStyle.JSON_STYLE));
        } catch (Exception e) {
        }
        System.out.println("-".repeat(20));
    }

    private void debug(WeatherStation weatherStation) {
        System.out.println("*".repeat(80));
//        debug(weatherStation);
        debug(weatherStation.getMeasurement());
        debug(weatherStation.getMeasurement().getAir());
        debug(weatherStation.getMeasurement().getMeasureTime());
        debug(weatherStation.getMeasurement().getPrecipitation());
        debug(weatherStation.getMeasurement().getRoad());
        debug(weatherStation.getMeasurement().getWind());
    }

}
