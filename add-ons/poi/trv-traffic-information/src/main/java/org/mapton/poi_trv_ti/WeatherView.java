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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MDict;
import org.mapton.api.MGenericLoader;
import org.openide.util.Exceptions;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Measurement;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Precipitation;
import se.trixon.trv_traffic_information.road.weatherstation.v1.WeatherStation;

/**
 *
 * @author Patrik Karlström
 */
public class WeatherView extends BorderPane implements MGenericLoader<WeatherStation> {

    private static final double TILE_HEIGHT = 150;
    private static final double TILE_SIZE = 150;
    private static final double TILE_WIDTH = 150;
    private Tile barGaugeTile;
    private Tile highLowTile;
    private Tile mAirHumidityTile;
    private Tile mAirTemperatureTile;
    private Tile mCameraImageTile;
    private final DateTimeFormatter mDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm");
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private Tile mPrecipitationTile;
    private Label mTimeLabel;
    private Label mTitleLabel;
    private final ZoneId mZoneOffset = ZoneOffset.systemDefault();

    public WeatherView() {
        createUI();
    }

    @Override
    public void load(WeatherStation weatherStation) {
        FxHelper.runLater(() -> {
            final Measurement measurement = weatherStation.getMeasurement();
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(measurement.getMeasureTime().toString());

            mTitleLabel.setText(String.format("%s %d, %s",
                    MDict.ROAD.toString(),
                    weatherStation.getRoadNumberNumeric(),
                    weatherStation.getName()
            ));
            final LocalDateTime measLocalDateTime = offsetDateTime.atZoneSameInstant(mZoneOffset).toLocalDateTime();
            mTimeLabel.setText(measLocalDateTime.format(mDtf));

            mAirHumidityTile.setValue(measurement.getAir().getRelativeHumidity());
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

            loadTemperature(weatherStation);
            loadPreciptation(weatherStation);
            loadImage(weatherStation);

            debug(weatherStation);
        });
    }

    private void createUI() {
        setPadding(FxHelper.getUIScaledInsets(8));
        String style = "-fx-font-weight: %s; -fx-font-style: %s; -fx-font-size: %.0fpx";
        final double defaultSize = Font.getDefault().getSize();
        mTimeLabel = new Label();
        mTitleLabel = new Label();

        mTitleLabel.setStyle(String.format(style, "bold", "normal", defaultSize * 1.2));
        setTop(mTitleLabel);

        mAirTemperatureTile = TileBuilder.create()
                .skinType(Tile.SkinType.NUMBER)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .title("TEMPERATURE")
                .unit("° C")
                .description("AIR")
                .build();

        mPrecipitationTile = TileBuilder.create()
                .skinType(Tile.SkinType.IMAGE_COUNTER)
                .prefSize(2 * TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .title("PRECIPITATION")
                .descriptionAlignment(Pos.BASELINE_LEFT)
                .descriptionAlignment(Pos.BOTTOM_LEFT)
                .imageMask(Tile.ImageMask.NONE)
                .unit("mm/h")
                .build();

        mAirHumidityTile = TileBuilder.create()
                .skinType(Tile.SkinType.FLUID)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .title("HUMIDITY")
                .unit("\u0025")
                .decimals(0)
                .barColor(Tile.BLUE)
                .animated(true)
                .build();

        barGaugeTile = TileBuilder.create()
                .skinType(Tile.SkinType.BAR_GAUGE)
                .prefSize(2 * TILE_WIDTH, TILE_HEIGHT)
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
                .build();

        mCameraImageTile = TileBuilder.create()
                .skinType(Tile.SkinType.IMAGE)
                .prefSize(3 * TILE_WIDTH, TILE_HEIGHT * 1.2)
                .imageMask(Tile.ImageMask.NONE)
                .textAlignment(TextAlignment.CENTER)
                .roundedCorners(true)
                .build();

        VBox vbox = new VBox(FxHelper.getUIScaled(8),
                mTimeLabel,
                new HBox(FxHelper.getUIScaled(8),
                        mAirTemperatureTile,
                        mPrecipitationTile
                ),
                new HBox(FxHelper.getUIScaled(8),
                        mAirHumidityTile,
                        barGaugeTile
                ),
                mCameraImageTile
        );

        setCenter(vbox);
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

    private void loadImage(WeatherStation weatherStation) {
        String url = mManager.getCameraGroupToPhotoUrl().getOrDefault(weatherStation.getId(), null);

        Image image = null;
        if (url != null) {
            image = new Image(url);
        }
        mCameraImageTile.setImage(image);
    }

    private void loadPreciptation(WeatherStation weatherStation) {
        Precipitation precipitation = weatherStation.getMeasurement().getPrecipitation();
        String url = String.format("%s%s.png", SystemHelper.getPackageAsPath(TrafficInfoPoiProvider.class), "precipitationNoPrecipitation");

        try {
            final Float amount = precipitation.getAmount();
            if (amount != null) {
                mPrecipitationTile.setValue(amount);
            }
            mPrecipitationTile.setValueVisible(amount != null);

            mPrecipitationTile.setDescription(String.format("%s\n%s", precipitation.getType(), precipitation.getAmountName()));
            url = mManager.getIcon(weatherStation.getMeasurement());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        mPrecipitationTile.setImage(new Image(url));
    }

    private void loadTemperature(WeatherStation weatherStation) {
        final Measurement measurement = weatherStation.getMeasurement();
        try {
            mAirTemperatureTile.setValue(measurement.getAir().getTemp());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

}
