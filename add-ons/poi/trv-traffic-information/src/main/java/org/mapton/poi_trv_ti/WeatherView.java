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
package org.mapton.poi_trv_ti;

import eu.hansolo.tilesfx.Tile;
import eu.hansolo.tilesfx.TileBuilder;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import static javafx.scene.layout.GridPane.REMAINING;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.mapton.api.MGenericLoader;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.Observation;
import se.trixon.trv_traffic_information.road.weathermeasurepoint.v2_1.WeatherMeasurepoint;

/**
 *
 * @author Patrik Karlström
 */
public class WeatherView extends BorderPane implements MGenericLoader<WeatherMeasurepoint> {

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
    public void load(WeatherMeasurepoint weatherMeasurepoint) {
        FxHelper.runLater(() -> {
            var observation = weatherMeasurepoint.getObservation();
            mTitleLabel.setText(weatherMeasurepoint.getName());

            var time = "NODATA";
            if (observation != null) {
                var offsetDateTime = OffsetDateTime.parse(observation.getSample().toString());
                var measLocalDateTime = offsetDateTime.atZoneSameInstant(mZoneOffset).toLocalDateTime();
                time = measLocalDateTime.format(mDtf);

                loadTemperature(observation);
                loadPreciptation(observation);
                loadHumidity(observation);
                loadWind(observation);
                loadImage(weatherMeasurepoint);
            } else {
                mAirTemperatureTile.setValue(999);
                setTooltipText(mAirTemperatureTile, "");

                mPrecipitationTile.setValueVisible(false);
                mPrecipitationTile.setUnit("");
                mPrecipitationTile.setImage(mNullImage);
                setTooltipText(mPrecipitationTile, "");

                mAirHumidityTile.setValue(-1);

                mWindTile.setValue(-1);
                mWindTile.setDescription("");
                mWindTile.setImage(mNullImage);
                mWindTile.setValueVisible(false);
                mWindTile.setUnit("");

                mCameraImageTile.setImage(mNullImage);
            }

            //debug(weatherStation);
            mTimeLabel.setText(time);
        });
    }

    private void createUI() {
        mIconIdToImage.put("null", mNullImage = new Pane().snapshot(null, null));
        setPadding(FxHelper.getUIScaledInsets(8));
        String style = "-fx-font-weight: %s; -fx-font-style: %s; -fx-font-size: %.0fpx";
        final double defaultSize = Font.getDefault().getSize();
        mTimeLabel = new Label();
        mTitleLabel = new Label();

        mTitleLabel.setStyle(style.formatted("bold", "normal", defaultSize * 1.2));
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

        var gp = new GridPane();
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
        var col1 = new ColumnConstraints();
        col1.setPercentWidth(width);
        var col2 = new ColumnConstraints();
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

    private void debug(WeatherMeasurepoint weatherMeasurepoint) {
        System.out.println("*".repeat(80));
//        debug(weatherStation);
//        debug(weatherStation.getObservation());
//        debug(weatherStation.getObservation().getAir());
//        debug(weatherMeasurepoint.getObservation().getPrecipitation());
//        debug(weatherStation.getObservation().getRoad());
//        debug(weatherStation.getObservation().getWind());
    }

    private Image getImageForIconId(String iconId) {
        return mIconIdToImage.computeIfAbsent(iconId, k -> {
            var imageView = new ImageView(k);
//        var pane = new StackPane(imageView);
//        pane.setPadding(new Insets(148));
            var snapshotParameters = new SnapshotParameters();
            snapshotParameters.setFill(Color.WHITESMOKE);
            var image = imageView.snapshot(snapshotParameters, null);

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

    private void loadHumidity(Observation observation) {
        try {
            var value = observation.getAir().getRelativeHumidity().getValue().getValue().doubleValue();
            mAirHumidityTile.setValue(value);
        } catch (NullPointerException e) {
            mAirHumidityTile.setValue(-1);
        }
    }

    private void loadImage(WeatherMeasurepoint weatherMeasurepoint) {
        String url = mManager.getCameraGroupToPhotoUrl().getOrDefault(weatherMeasurepoint.getId(), null);
        var image = url != null ? new Image(url) : mNullImage;

        mCameraImageTile.setImage(image);
    }

    private void loadPreciptation(Observation observation) {
        var precipitation = observation.getAggregated30Minutes().getPrecipitation();
//        var precipType = observation.getWeather().getPrecipitation();
        var amount = precipitation.getTotalWaterEquivalent().getValue().getValue();
        var hasValue = amount != null;

        try {
            if (hasValue) {
                mPrecipitationTile.setValue(amount.doubleValue());
            }
            mPrecipitationTile.setValueVisible(hasValue);
            mPrecipitationTile.setUnit(hasValue ? "mm/h" : "");

//            if (Strings.CI.equalsAny(precipitation.getType(), "Hagel", "Underkylt regn")) {
//                description = precipitation.getType();
//            }
            mPrecipitationTile.setDescription(getMessage(WeatherHelper.getPrecipType(precipitation)));
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }

        mPrecipitationTile.setImage(getImageForIconId(mManager.getIconUrl(precipitation)));
        setTooltipText(mPrecipitationTile, "precipitation");
    }

    private void loadTemperature(Observation observation) {
        var hasValue = observation.getAir() != null;
        if (hasValue) {
            mAirTemperatureTile.setValue(observation.getAir().getTemperature().getValue().getValue().doubleValue());
        }

        mAirTemperatureTile.setValueVisible(hasValue);
        mAirTemperatureTile.setUnit(hasValue ? "°C" : "");
        setTooltipText(mAirTemperatureTile, observation.getAir());
    }

    private void loadWind(Observation observation) {
        var windCondition = observation.getWind().getFirst();
        if (windCondition != null) {
            var hasValue = windCondition.getSpeed() != null;
            if (hasValue) {
                var speed = windCondition.getSpeed().getValue().getValue().doubleValue();
                mWindTile.setValue(speed);
                mWindTile.setImage(getImageForIconId(mManager.getIconUrl(windCondition)));
                var windAgg = observation.getAggregated30Minutes().getWind();
                try {
                    var speedMax = windAgg.getSpeedMax().getValue().getValue().doubleValue();
                    mWindTile.setDescription("%s: %.1f m/s".formatted("Max", speedMax));
                } catch (Exception e) {
                    //
                }
            } else {
                mWindTile.setValue(-1);
                mWindTile.setDescription("");
                mWindTile.setImage(mNullImage);
            }

            mWindTile.setValueVisible(hasValue);
            mWindTile.setUnit(hasValue ? "m/s" : "");
        }

        setTooltipText(mWindTile, "wind");
    }

    private void setTooltipText(Tile tile, Object o) {
        String s = null;
        if (o != null) {
            s = ToStringBuilder.reflectionToString(o, ToStringStyle.NO_CLASS_NAME_STYLE);
        }

        tile.setTooltipText(s);
    }

}
