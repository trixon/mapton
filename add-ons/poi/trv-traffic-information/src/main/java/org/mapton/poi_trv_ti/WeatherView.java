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
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import static javafx.scene.layout.GridPane.REMAINING;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MDict;
import org.mapton.api.MGenericLoader;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Measurement;
import se.trixon.trv_traffic_information.road.weatherstation.v1.Precipitation;
import se.trixon.trv_traffic_information.road.weatherstation.v1.WeatherStation;

/**
 *
 * @author Patrik Karlström
 */
public class WeatherView extends BorderPane implements MGenericLoader<WeatherStation> {

    private static final double TILE_HEIGHT = FxHelper.getUIScaled(150.0);
    private static final double TILE_WIDTH = FxHelper.getUIScaled(150.0);
    private Tile mAirHumidityTile;
    private Tile mAirTemperatureTile;
    private final ResourceBundle mBundle = NbBundle.getBundle(WeatherView.class);
    private Tile mCameraImageTile;
    private final DateTimeFormatter mDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH.mm");
    private final HashMap<String, Image> mIconIdToImage = new HashMap<>();
    private final TrafficInformationManager mManager = TrafficInformationManager.getInstance();
    private Image mNullImage;
    private Tile mPrecipitationTile;
    private Label mTimeLabel;
    private Label mTitleLabel;
    private Tile mWindTile;
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

            loadTemperature(weatherStation);
            loadPreciptation(weatherStation);
            loadHumidity(weatherStation);
            loadWind(weatherStation);
            loadImage(weatherStation);

            //debug(weatherStation);
        });
    }

    private void createUI() {
        mIconIdToImage.put("null", mNullImage = new Pane().snapshot(null, null));
        setPadding(FxHelper.getUIScaledInsets(8));
        String style = "-fx-font-weight: %s; -fx-font-style: %s; -fx-font-size: %.0fpx";
        final double defaultSize = Font.getDefault().getSize();
        mTimeLabel = new Label();
        mTitleLabel = new Label();

        mTitleLabel.setStyle(String.format(style, "bold", "normal", defaultSize * 1.2));
        setTop(mTitleLabel);

        mAirTemperatureTile = TileBuilder.create()
                .skinType(Tile.SkinType.FIRE_SMOKE)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .minValue(-999)
                .decimals(0)
                .threshold(100)
                .thresholdVisible(true)
                .build();

        mPrecipitationTile = TileBuilder.create()
                .skinType(Tile.SkinType.IMAGE_COUNTER)
                .prefSize(2 * TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .descriptionAlignment(Pos.BASELINE_LEFT)
                .descriptionAlignment(Pos.BOTTOM_LEFT)
                .imageMask(Tile.ImageMask.ROUND)
                .minValue(-1)
                .build();

        mWindTile = TileBuilder.create()
                .skinType(Tile.SkinType.IMAGE_COUNTER)
                .prefSize(2 * TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .descriptionAlignment(Pos.BASELINE_LEFT)
                .descriptionAlignment(Pos.BOTTOM_LEFT)
                .imageMask(Tile.ImageMask.ROUND)
                .build();

        mAirHumidityTile = TileBuilder.create()
                .skinType(Tile.SkinType.FIRE_SMOKE)
                .prefSize(TILE_WIDTH, TILE_HEIGHT)
                .textSize(Tile.TextSize.BIGGER)
                .unit("\u0025")
                .decimals(0)
                .threshold(100)
                .thresholdVisible(true)
                .build();

        mCameraImageTile = TileBuilder.create()
                .skinType(Tile.SkinType.IMAGE)
                .prefSize(3 * TILE_WIDTH, TILE_HEIGHT * 1.2)
                .imageMask(Tile.ImageMask.NONE)
                .textAlignment(TextAlignment.CENTER)
                .roundedCorners(true)
                .build();

        GridPane gp = new GridPane();
        gp.setPadding(FxHelper.getUIScaledInsets(8));
        gp.setHgap(FxHelper.getUIScaled(8));
        gp.setVgap(FxHelper.getUIScaled(8));
        int col = 0;
        int row = 0;

        gp.add(mTimeLabel, col, row, REMAINING, 1);
        gp.add(mAirTemperatureTile, col, ++row, 1, 1);
        gp.add(mPrecipitationTile, ++col, row, REMAINING, 1);
        gp.add(mAirHumidityTile, col = 0, ++row, 1, 1);
        gp.add(mWindTile, ++col, row, REMAINING, 1);
        gp.add(mCameraImageTile, col = 0, ++row, REMAINING, 1);

        GridPane.setValignment(mTimeLabel, VPos.CENTER);
        GridPane.setHgrow(mPrecipitationTile, Priority.ALWAYS);
        GridPane.setHgrow(mWindTile, Priority.ALWAYS);

        GridPane.setFillWidth(mPrecipitationTile, true);
        GridPane.setFillWidth(mWindTile, true);
        mCameraImageTile.setMaxWidth(Double.MAX_VALUE);

        double width = 100.0 / 3.0;
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(width);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(width * 2);
        gp.getColumnConstraints().addAll(col1, col2);

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
//        debug(weatherStation.getMeasurement());
//        debug(weatherStation.getMeasurement().getAir());
        debug(weatherStation.getMeasurement().getPrecipitation());
//        debug(weatherStation.getMeasurement().getRoad());
//        debug(weatherStation.getMeasurement().getWind());
    }

    private Image getImageForIconId(String iconId) {
        return mIconIdToImage.computeIfAbsent(iconId, k -> {
            var imageView = new ImageView(k);
//        var pane = new StackPane(imageView);
//        pane.setPadding(new Insets(148));
            var snapshotParameters = new SnapshotParameters();
            snapshotParameters.setFill(Color.WHITESMOKE);
            WritableImage image = imageView.snapshot(snapshotParameters, null);

            return image;
        });
    }

    private String getMessage(String key) {
        try {
            return mBundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }

    private void loadHumidity(WeatherStation weatherStation) {
        try {
            mAirHumidityTile.setValue(weatherStation.getMeasurement().getAir().getRelativeHumidity());
        } catch (NullPointerException e) {
            mAirHumidityTile.setValue(-1);
        }
    }

    private void loadImage(WeatherStation weatherStation) {
        String url = mManager.getCameraGroupToPhotoUrl().getOrDefault(weatherStation.getId(), null);

        Image image = mNullImage;
        if (url != null) {
            image = new Image(url);
        }

        mCameraImageTile.setImage(image);
    }

    private void loadPreciptation(WeatherStation weatherStation) {
        Precipitation precipitation = weatherStation.getMeasurement().getPrecipitation();

        try {
            final Float amount = precipitation.getAmount();
            var hasValue = amount != null;
            if (amount != null) {
                mPrecipitationTile.setValue(amount);
            }
            mPrecipitationTile.setValueVisible(hasValue);
            mPrecipitationTile.setUnit(hasValue ? "mm/h" : "");

            String description = precipitation.getAmountName();
            if (StringUtils.equalsAnyIgnoreCase(precipitation.getType(), "Hagel", "Underkylt regn")) {
                description = precipitation.getType();
            }
            mPrecipitationTile.setDescription(getMessage(description));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        mPrecipitationTile.setImage(getImageForIconId(mManager.getIconUrl(precipitation)));
        setTooltipText(mPrecipitationTile, precipitation);
    }

    private void loadTemperature(WeatherStation weatherStation) {
        final Measurement measurement = weatherStation.getMeasurement();
        var hasValue = measurement != null && measurement.getAir() != null;

        if (hasValue) {
            mAirTemperatureTile.setValue(measurement.getAir().getTemp());
        }

        mAirTemperatureTile.setValueVisible(hasValue);
        mAirTemperatureTile.setUnit(hasValue ? "°C" : "");
        setTooltipText(mAirTemperatureTile, measurement.getAir());
    }

    private void loadWind(WeatherStation weatherStation) {
        var wind = weatherStation.getMeasurement().getWind();
        var hasValue = wind != null && wind.getForce() != null;
//        var hasValue = wind != null && wind.getForce() != null && wind.getForce() > 0;

        if (hasValue) {
            mWindTile.setValue(wind.getForce());
            mWindTile.setDescription(String.format("%s: %.1f m/s", "Max", wind.getForceMax()));
            mWindTile.setImage(getImageForIconId(mManager.getIconUrl(wind)));
        } else {
            mWindTile.setValue(-1);
            mWindTile.setDescription("");
            mWindTile.setImage(mNullImage);
        }

        mWindTile.setValueVisible(hasValue);
        mWindTile.setUnit(hasValue ? "m/s" : "");
        setTooltipText(mWindTile, wind);
    }

    private void setTooltipText(Tile tile, Object o) {
        String s = null;
        if (o != null) {
            s = ToStringBuilder.reflectionToString(o, ToStringStyle.NO_CLASS_NAME_STYLE);
        }

        tile.setTooltipText(s);
    }

}
