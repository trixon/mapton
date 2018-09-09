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
package se.trixon.mapton.core.ui;

import fr.dudie.nominatim.model.Address;
import fr.dudie.nominatim.model.BoundingBox;
import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
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
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.api.MDecDegDMS;
import se.trixon.mapton.api.MLatLon;
import se.trixon.mapton.api.MLatLonBox;
import se.trixon.mapton.api.Mapton;
import static se.trixon.mapton.api.Mapton.getIconSizeToolBarInt;
import se.trixon.mapton.api.MOptions;
import se.trixon.mapton.api.MNominatim;
import se.trixon.mapton.api.MCooTrans;
import se.trixon.mapton.core.Wgs84DMS;

/**
 *
 * @author Patrik Karlström
 */
public class SearchView {

    private static final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);
    private final ObservableList<Address> mItems = FXCollections.observableArrayList();
    private final MNominatim mNominatim = MNominatim.getInstance();
    private final MOptions mOptions = MOptions.getInstance();
    private final PopOver mResultPopOver;
    private final ListView<Address> mResultView = new ListView();
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
        mResultView.setCellFactory((ListView<Address> param) -> new GeocodingResultListCell());
        mResultView.setOnMousePressed((MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                mResultPopOver.hide();
                Address address = mResultView.getSelectionModel().getSelectedItem();
                if (address != null) {
                    mSearchTextField.setText(address.getDisplayName());
                    BoundingBox bb = address.getBoundingBox();
                    MLatLonBox latLonBox = new MLatLonBox(
                            new MLatLon(bb.getSouth(), bb.getWest()),
                            new MLatLon(bb.getNorth(), bb.getEast())
                    );

                    Mapton.getEngine().fitToBounds(latLonBox);
                }
            }
        });
    }

    public Node getPresenter() {
        return mSearchTextField;
    }

    private void panTo(MLatLon latLon) {
        Mapton.getEngine().panTo(latLon);
    }

    private void parse(String searchString) {
        MLatLon latLong = parseDecimal(searchString);
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

    private MLatLon parseDecimal(String searchString) {
        MLatLon latLong = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");

        if (coordinate.length == 2) {
            try {
                final Double lat = NumberUtils.createDouble(coordinate[0]);
                final Double lon = NumberUtils.createDouble(coordinate[1]);
                Wgs84DMS dms = new Wgs84DMS();

                if (dms.isWithinWgs84Bounds(lat, lon)) {
                    latLong = new MLatLon(lat, lon);
                } else {
                    MCooTrans cooTrans = mOptions.getMapCooTrans();
                    if (cooTrans.isWithinProjectedBounds(lat, lon)) {
                        Point2D p = cooTrans.toWgs84(lat, lon);
                        latLong = new MLatLon(p.getY(), p.getX());
                    }
                }
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

    private MLatLon parseDegMinSec(String searchString) {
        MLatLon latLong = null;
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

                MDecDegDMS dddms = new MDecDegDMS(latDeg, latMin, latSec, lonDeg, lonMin, lonSec);
                latLong = new MLatLon(dddms.getLatitude(), dddms.getLongitude());
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

    private void parseString(String searchString) {
        new Thread(() -> {
            try {
                mItems.clear();

                final List<Address> addresses = mNominatim.search(searchString);
                Platform.runLater(() -> {
                    mResultPopOver.setTitle("" + addresses.size() + " TRÄFFAR");
                    mResultPopOver.show(mSearchTextField);
                    mItems.addAll(addresses);
                });

                for (final Address address : addresses) {
                    NbLog.v(getClass(), ToStringBuilder.reflectionToString(address, ToStringStyle.MULTI_LINE_STYLE));
                }
            } catch (IOException ex) {
                mResultPopOver.hide();
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }

    class GeocodingResultListCell extends ListCell<Address> {

        private VBox mBox = new VBox();
        private final Label mLabel = new Label();

        public GeocodingResultListCell() {
            createUI();
        }

        @Override
        protected void updateItem(Address result, boolean empty) {
            super.updateItem(result, empty);

            if (result == null || empty) {
                clearContent();
            } else {
                addContent(result);
            }
        }

        private void addContent(Address result) {
            setText(null);
            mLabel.setText(result.getDisplayName());

            setGraphic(mBox);
        }

        private void clearContent() {
            setText(null);
            setGraphic(null);
        }

        private void createUI() {
            mBox.getChildren().add(mLabel);
            mBox.setOnMouseEntered((MouseEvent event) -> {
                selectListItem();
            });
        }

        private void selectListItem() {
            mResultView.getSelectionModel().select(this.getIndex());
            mResultView.requestFocus();
        }
    }
}
