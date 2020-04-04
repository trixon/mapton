/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.base.ui;

import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import org.controlsfx.control.PlusMinusSlider;
import org.mapton.api.MCooTrans;
import org.mapton.api.MEngine;
import org.mapton.api.MOptions;
import org.mapton.api.MStatusZoomMode;
import org.mapton.api.Mapton;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.GlobalStateChangeEvent;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class StatusBarView extends org.controlsfx.control.StatusBar {

    private final ComboBox<MCooTrans> mComboBox = new ComboBox<>();
    private MCooTrans mCooTrans;
    private HBox mLeftItemsBox;
    private final MOptions mOptions = MOptions.getInstance();
    private HBox mRightItemsBox;
    private final Label mRightLabel = new Label();
    private StatusWindowMode mWindowMode = StatusWindowMode.OTHER;
    private Slider mZoomAbsoluteSlider;
    private MStatusZoomMode mZoomMode = MStatusZoomMode.ABSOLUTE;
    private PlusMinusSlider mZoomRelativeSlider;

    public static StatusBarView getInstance() {
        return Holder.INSTANCE;
    }

    private StatusBarView() {
        super.setText("");
        createUI();
        initListeners();

        updateProviders();
        updateZoomMode();
    }

    public void setMessage(String message) {
        Platform.runLater(() -> {
            mRightLabel.setText(message);
        });
    }

    public void updateMousePositionData() {
        MEngine engine = Mapton.getEngine();

        if (engine != null) {
            if (engine.getLatitude() != null) {
                double latitude = engine.getLatitude();
                double longitude = engine.getLongitude();

                if (latitude != 0 && longitude != 0) {
                    String altitude = "";
                    if (engine.getAltitude() != null) {
                        double metersAltitude = engine.getAltitude();
                        if (Math.abs(metersAltitude) >= 1000) {
                            altitude = String.format("%s %,7d km, ", Dict.ALTITUDE.toString(), (int) Math.round(metersAltitude / 1e3));
                        } else {
                            altitude = String.format("%s %,7d m, ", Dict.ALTITUDE.toString(), (int) Math.round(metersAltitude));
                        }
                    }

                    String elevation = "";
                    if (engine.getElevation() != null) {
                        elevation = String.format("%s %,6d %s, ", Dict.ELEVATION.toString(), (int) engine.getElevation().doubleValue(), Dict.METERS.toString().toLowerCase());
                    }

                    String cooString = mCooTrans.getString(latitude, longitude);
                    String lat = String.format("%9.6f°%s", Math.abs(latitude), latitude < 0 ? "S" : "N");
                    String lon = String.format("%10.6f°%s", Math.abs(longitude), longitude < 0 ? "W" : "E");
                    String latLon = String.format("%s  %s WGS 84 DD, %s", lat, lon, cooString);

                    setMessage(altitude + elevation + latLon);
                }
            } else {
                setMessage("");
                setProgress(0);
            }
        }
    }

    public void setWindowMode(StatusWindowMode windowMode) {
        mWindowMode = windowMode;

        Platform.runLater(() -> {
            boolean mapMode = windowMode == StatusWindowMode.MAP;
            mRightLabel.setVisible(mapMode);
            mComboBox.setVisible(mapMode);

            updateZoomMode();

            setProgress(0);
        });
    }

    private void createUI() {
        final int sliderWidth = FxHelper.getUIScaled(200);

        mZoomAbsoluteSlider = new Slider(0, 1, 0.5);
        mZoomAbsoluteSlider.setPadding(FxHelper.getUIScaledInsets(4, 0, 0, 0));
        mZoomAbsoluteSlider.setPrefWidth(sliderWidth);
        mZoomAbsoluteSlider.setBlockIncrement(0.05);

        mZoomRelativeSlider = new PlusMinusSlider();
        mZoomRelativeSlider.setPrefWidth(sliderWidth);
        mZoomRelativeSlider.setDisable(true);

        mLeftItemsBox = new HBox(FxHelper.getUIScaled(16));
        mLeftItemsBox.setFillHeight(true);
        mLeftItemsBox.setAlignment(Pos.CENTER_LEFT);

        mRightItemsBox = new HBox(FxHelper.getUIScaled(16), new Label(""), mRightLabel, mComboBox);
        mRightItemsBox.setFillHeight(true);
        mRightItemsBox.setAlignment(Pos.CENTER_RIGHT);
        mRightItemsBox.prefHeightProperty().bind(heightProperty());

        mRightLabel.setStyle("-fx-font-family: 'monospaced';");
        getLeftItems().addAll(mLeftItemsBox);
        getRightItems().addAll(mRightItemsBox);
        setStyle("-fx-background-insets: 0, 0;");

        mRightLabel.setVisible(false);
        mComboBox.setVisible(false);
        //mComboBox.setStyle("-fx-background-color: transparent;-fx-border-color: gray;");
        mComboBox.setStyle("-fx-background-color: transparent;");
    }

    private void initListeners() {
        Lookup.getDefault().lookupResult(MCooTrans.class).addLookupListener((LookupEvent ev) -> {
            updateProviders();
        });

        mComboBox.setOnAction((ActionEvent event) -> {
            mCooTrans = mComboBox.getSelectionModel().getSelectedItem();
            mOptions.setMapCooTrans(mCooTrans.getName());
            updateMousePositionData();
        });

        MOptions.getInstance().engineProperty().addListener((ObservableValue<? extends String> ov, String t, String t1) -> {
            setMessage("");
            Platform.runLater(() -> {
                updateZoomMode();
            });
        });

        Mapton.getGlobalState().addListener((GlobalStateChangeEvent evt) -> {
            Platform.runLater(() -> {
                switch (evt.getKey()) {
                    case MEngine.KEY_STATUS_COORDINATE:
                        updateMousePositionData();
                        break;

                    case MEngine.KEY_STATUS_PROGRESS:
                        setProgress(evt.getValue());
                        break;
                }
            });
        }, MEngine.KEY_STATUS_COORDINATE, MEngine.KEY_STATUS_PROGRESS);

        mZoomAbsoluteSlider.valueProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number newValue) -> {
            Mapton.getEngine().zoomTo(newValue.doubleValue());
        });

        Mapton.getInstance().zoomProperty().addListener((ObservableValue<? extends Number> ov, Number t, Number t1) -> {
            mZoomAbsoluteSlider.setValue(t1.doubleValue());
        });
    }

    private void updateProviders() {
        Platform.runLater(() -> {
            mComboBox.getItems().setAll(MCooTrans.getCooTrans());
            mComboBox.setItems(mComboBox.getItems().sorted());

            for (MCooTrans cooTrans : mComboBox.getItems()) {
                Mapton.logLoading("Coordinate Transformation", cooTrans.getName());
            }

            if (!mComboBox.getItems().isEmpty()) {
                MCooTrans cooTrans = MCooTrans.getCooTrans(mOptions.getMapCooTransName());

                if (cooTrans == null) {
                    cooTrans = mComboBox.getItems().get(0);
                }

                mComboBox.getSelectionModel().select(cooTrans);
                mCooTrans = mComboBox.getSelectionModel().getSelectedItem();
                updateMousePositionData();
            }
        });
    }

    private void updateZoomMode() {
        mLeftItemsBox.getChildren().clear();

        mZoomMode = Mapton.getEngine().getStatusZoomMode();
        if (mWindowMode == StatusWindowMode.MAP) {
            if (mZoomMode == MStatusZoomMode.ABSOLUTE) {
                mLeftItemsBox.getChildren().add(mZoomAbsoluteSlider);
            } else {
                mLeftItemsBox.getChildren().add(mZoomRelativeSlider);
            }
        }
    }

    private static class Holder {

        private static final StatusBarView INSTANCE = new StatusBarView();
    }

    public static enum StatusWindowMode {
        MAP, OTHER;
    }
}
