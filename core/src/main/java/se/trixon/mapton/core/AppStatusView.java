/*
 * Copyright 2018 Patrik Karlström.
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
package se.trixon.mapton.core;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.text.Font;
import org.controlsfx.control.StatusBar;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import se.trixon.mapton.core.api.CooTransProvider;
import se.trixon.mapton.core.api.MapController;
import se.trixon.mapton.core.api.Mapton;
import se.trixon.mapton.core.api.MaptonOptions;

/**
 *
 * @author Patrik Karlström
 */
public class AppStatusView extends StatusBar {

    private final ComboBox<CooTransProvider> mComboBox = new ComboBox();
    private CooTransProvider mCooTrans;
    private final Label mLabel = new Label();
//    private final MapController mMapController = MapController.getInstance();
    private final MaptonOptions mOptions = MaptonOptions.getInstance();

    public AppStatusView() {
        super.setText("");
        getRightItems().addAll(mLabel, mComboBox);

        mLabel.prefHeightProperty().bind(heightProperty());
        mLabel.setPadding(new Insets(0, 8, 0, 8));
        mLabel.setFont(Font.font("monospaced"));

        Lookup.getDefault().lookupResult(CooTransProvider.class).addLookupListener((LookupEvent ev) -> {
            updateProviders();
        });

        mComboBox.setOnAction((ActionEvent event) -> {
            mCooTrans = mComboBox.getSelectionModel().getSelectedItem();
            mOptions.setMapCooTrans(mCooTrans.getName());
            updateLatLong();
        });

        updateProviders();

    }

    public void setMessage(String message) {
        mLabel.setText(message);
    }

    public void updateLatLong() {
        MapController mapController = Mapton.getController();

        if (mapController != null) {
            final double latitude = mapController.getLatitude();
            final double longitude = mapController.getLongitude();

            if (latitude != 0 && longitude != 0) {
                String cooString = mCooTrans.getString(latitude, longitude);

                String lat = String.format("%9.6f°%s", Math.abs(latitude), latitude < 0 ? "S" : "N");
                String lon = String.format("%10.6f°%s", Math.abs(longitude), longitude < 0 ? "W" : "E");

                setMessage(String.format("%s  %s WGS 84 DD, %s", lat, lon, cooString));
            }
        }
    }

    private void updateProviders() {
        Platform.runLater(() -> {
            mComboBox.getItems().clear();
            for (CooTransProvider cooTrans : Lookup.getDefault().lookupAll(CooTransProvider.class)) {
                mComboBox.getItems().add(cooTrans);
            }
            mComboBox.setItems(mComboBox.getItems().sorted());

            if (!mComboBox.getItems().isEmpty()) {
                CooTransProvider cooTrans = CooTransProvider.getCooTrans(mOptions.getMapCooTransName());

                if (cooTrans == null) {
                    cooTrans = mComboBox.getItems().get(0);
                }

                mComboBox.getSelectionModel().select(cooTrans);
                mCooTrans = mComboBox.getSelectionModel().getSelectedItem();
                updateLatLong();
            }
        });
    }
}
