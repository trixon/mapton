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
package se.trixon.mapton.core.map;

import java.util.Arrays;
import java.util.ResourceBundle;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.openide.util.NbBundle;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.core.api.CooTransProvider;
import se.trixon.mapton.core.api.DecDegDMS;
import se.trixon.mapton.core.api.LatLon;
import se.trixon.mapton.core.api.Mapton;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBarInt;
import se.trixon.mapton.core.api.MaptonOptions;
import se.trixon.mapton.core.api.geocode.GeocoderStatus;
import se.trixon.mapton.core.api.geocode.GeocodingResult;
import se.trixon.mapton.core.api.geocode.GeocodingService;
import se.trixon.mapton.core.api.geocode.GeocodingServiceCallback;

/**
 *
 * @author Patrik Karlström
 */
public class SearchView {

    private static final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);
    private final ObservableList<GeocodingResult> mItems = FXCollections.observableArrayList();
    private final MaptonOptions mOptions = MaptonOptions.getInstance();
    private final PopOver mResultPopOver;
    private final ListView<GeocodingResult> mResultView = new ListView();
    private CustomTextField mSearchTextField;

    public SearchView() {
        mSearchTextField = (CustomTextField) TextFields.createClearableTextField();
        mSearchTextField.setLeft(MaterialIcon._Action.SEARCH.getImageView(getIconSizeToolBarInt() - 4));
        mSearchTextField.setPromptText(mBundle.getString("search_prompt"));
        mSearchTextField.setPrefColumnCount(30);
        mSearchTextField.setText("");
        mSearchTextField.setOnAction((ActionEvent event) -> {
            final String searchString = mSearchTextField.getText();
            if (!StringUtils.isBlank(searchString)) {
                parse(searchString);
            }
        });

        mResultPopOver = new PopOver();
        mResultPopOver.setTitle("");
        mResultPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        mResultPopOver.setHeaderAlwaysVisible(true);
        mResultPopOver.setCloseButtonEnabled(false);
        mResultPopOver.setDetachable(false);
        mResultPopOver.setContentNode(mResultView);
        mResultPopOver.setAnimated(false);

        mResultView.prefWidthProperty().bind(mSearchTextField.widthProperty());
        mResultView.setItems(mItems);
        mResultView.setCellFactory((ListView<GeocodingResult> param) -> new GeocodingResultListCell());
        mResultView.setOnMousePressed((MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                mResultPopOver.hide();
                GeocodingResult result = mResultView.getSelectionModel().getSelectedItem();
                if (result != null) {
                    mSearchTextField.setText(result.getFormattedAddress());
//TODO Replace me
//                    Mapton.getInstance().getMapController().fitBounds(result.getGeometry());
                }
            }
        });
    }

    public Node getPresenter() {
        return mSearchTextField;
    }

    private void panTo(LatLon latLon) {
        Mapton.getEngine().panTo(latLon);
    }

    private void parse(String searchString) {
        LatLon latLong = parseDecimal(searchString);
        if (latLong == null) {
            latLong = parseDegMinSec(searchString);
            if (latLong == null) {
                parseString(searchString);
            }
        }

        if (latLong != null) {
            panTo(latLong);
        }
    }

    private LatLon parseDecimal(String searchString) {
        LatLon latLong = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");

        if (coordinate.length == 2) {
            try {
                final Double lat = NumberUtils.createDouble(coordinate[0]);
                final Double lon = NumberUtils.createDouble(coordinate[1]);
                Wgs84DMS dms = new Wgs84DMS();

                if (dms.isWithinWgs84Bounds(lat, lon)) {
                    latLong = new LatLon(lat, lon);
                } else {
                    CooTransProvider cooTrans = mOptions.getMapCooTrans();
                    if (cooTrans.isWithinProjectedBounds(lat, lon)) {
                        Point2D p = cooTrans.toWgs84(lat, lon);
                        latLong = new LatLon(p.getY(), p.getX());
                    }
                }
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

    private LatLon parseDegMinSec(String searchString) {
        LatLon latLong = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");

        if (coordinate.length == 2
                && StringUtils.countMatches(searchString, '°') == 2
                && StringUtils.countMatches(searchString, '\'') == 2
                && StringUtils.countMatches(searchString, '"') == 2) {
            try {
                final String latString = coordinate[0];
                int latDeg = Integer.valueOf(StringUtils.substringBefore(latString, "°"));
                int latMin = Integer.valueOf(StringUtils.substringBetween(latString, "°", "'"));
                double latSec = Double.valueOf(StringUtils.substringBetween(latString, "'", "\""));

                if (StringUtils.endsWithIgnoreCase(latString, "s")) {
                    latDeg = latDeg * -1;
                }

                final String lonString = StringUtils.removeStart(coordinate[1], "0");
                int lonDeg = Integer.valueOf(StringUtils.substringBefore(lonString, "°"));
                int lonMin = Integer.valueOf(StringUtils.substringBetween(lonString, "°", "'"));
                double lonSec = Double.valueOf(StringUtils.substringBetween(lonString, "'", "\""));

                if (StringUtils.endsWithIgnoreCase(lonString, "w")) {
                    lonDeg = lonDeg * -1;
                }

                DecDegDMS dddms = new DecDegDMS(latDeg, latMin, latSec, lonDeg, lonMin, lonSec);
                latLong = new LatLon(dddms.getLatitude(), dddms.getLongitude());
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

    private void parseString(String searchString) {
        GeocodingServiceCallback callback = (GeocodingResult[] results, GeocoderStatus status) -> {
            mItems.clear();
            if (status != GeocoderStatus.OK) {
                mResultPopOver.hide();
                return;
            }

            mResultPopOver.setTitle("" + results.length + " TRÄFFAR");
            mResultPopOver.show(mSearchTextField);
            mItems.addAll(Arrays.asList(results));
        };

        GeocodingService service = new GeocodingService();
        service.geocode(searchString, callback);
    }

    class GeocodingResultListCell extends ListCell<GeocodingResult> {

        private final Label mLabel = new Label();
        private VBox mBox = new VBox();

        public GeocodingResultListCell() {
            createUI();
        }

        private void addContent(GeocodingResult result) {
            setText(null);
            mLabel.setText(result.getFormattedAddress());

            setGraphic(mBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        @Override
        protected void updateItem(GeocodingResult result, boolean empty) {
            super.updateItem(result, empty);

            if (result == null || empty) {
                clearContent();
            } else {
                addContent(result);
            }
        }

        private void selectListItem() {
            mResultView.getSelectionModel().select(this.getIndex());
            mResultView.requestFocus();
        }

        private void createUI() {
            mBox.getChildren().add(mLabel);
            mBox.setOnMouseEntered((MouseEvent event) -> {
                selectListItem();
            });
        }
    }
}
