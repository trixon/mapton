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
package org.mapton.core.ui;

import fr.dudie.nominatim.model.Address;
import fr.dudie.nominatim.model.BoundingBox;
import java.io.IOException;
import java.util.ArrayList;
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
import javafx.scene.text.Font;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.PopOver;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.mapton.api.MBookmark;
import org.mapton.api.MCooTrans;
import org.mapton.api.MDecDegDMS;
import org.mapton.api.MLatLon;
import org.mapton.api.MLatLonBox;
import org.mapton.api.MNominatim;
import org.mapton.api.MOptions;
import org.mapton.api.MSearchEngine;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core.Wgs84DMS;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import se.trixon.almond.nbp.NbLog;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class SearchView {

    private final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);
    private final ObservableList<MBookmark> mItems = FXCollections.observableArrayList();
    private final MNominatim mNominatim = MNominatim.getInstance();
    private final MOptions mOptions = MOptions.getInstance();
    private PopOver mResultPopOver;
    private final ListView<MBookmark> mResultView = new ListView();
    private CustomTextField mSearchTextField;
    private static final String PROVIDER_PREFIX = "> > > ";

    public SearchView() {
        createUI();
        initListeners();
        new Thread(() -> {
            Lookup.getDefault().lookupAll(MSearchEngine.class).forEach((searchEngine) -> {
                NbLog.i(getClass(), "Loading search engine: " + searchEngine.getName());
            });
        }).start();
    }

    public Node getPresenter() {
        return mSearchTextField;
    }

    private void createUI() {
        mSearchTextField = (CustomTextField) TextFields.createClearableTextField();
        mSearchTextField.setLeft(MaterialIcon._Action.SEARCH.getImageView(getIconSizeToolBarInt() - 4));
        mSearchTextField.setPromptText(mBundle.getString("search_prompt"));
        mSearchTextField.setPrefColumnCount(30);
        mSearchTextField.setText("");

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
        mResultView.setCellFactory((ListView<MBookmark> param) -> new GeocodingResultListCell());
    }

    private void initListeners() {
        mSearchTextField.setOnAction((ActionEvent event) -> {
            final String searchString = mSearchTextField.getText();
            if (!StringUtils.isBlank(searchString)) {
                parse(searchString);
            }
        });

        mResultView.setOnMousePressed((MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                mResultPopOver.hide();
                MBookmark bookmark = mResultView.getSelectionModel().getSelectedItem();
                if (bookmark != null) {
                    mSearchTextField.setText(bookmark.getName());
                    if (bookmark.getLatLonBox() != null) {
                        Mapton.getEngine().fitToBounds(bookmark.getLatLonBox());
                    } else if (bookmark.getZoom() != null) {
                        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()), bookmark.getZoom());
                    } else if (ObjectUtils.allNotNull(bookmark.getLatitude(), bookmark.getLongitude())) {
                        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()));
                    }
                }
            }
        });
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
                    int providerCount = 0;
                    mResultPopOver.show(mSearchTextField);
                    ArrayList< MSearchEngine> searchers = new ArrayList<>(Lookup.getDefault().lookupAll(MSearchEngine.class));
                    searchers.sort((MSearchEngine o1, MSearchEngine o2) -> o1.getName().compareTo(o2.getName()));

                    for (MSearchEngine searcher : searchers) {
                        ArrayList<MBookmark> bookmarks = searcher.getResults(searchString);
                        if (!bookmarks.isEmpty()) {
                            providerCount++;
                            MBookmark b = new MBookmark();
                            b.setName(PROVIDER_PREFIX + searcher.getName());
                            mItems.add(b);

                            for (MBookmark bookmark : bookmarks) {
                                mItems.add(bookmark);
                            }
                        }
                    }

                    boolean addHeader = true;
                    for (Address address : addresses) {
                        if (addHeader) {
                            providerCount++;
                            MBookmark b = new MBookmark();
                            b.setName(PROVIDER_PREFIX + "Nominatim");
                            mItems.add(b);
                            addHeader = false;
                        }

                        MBookmark bookmark = new MBookmark();
                        bookmark.setName(address.getDisplayName());
                        bookmark.setLatitude(address.getLatitude());
                        bookmark.setLongitude(address.getLongitude());
                        BoundingBox bb = address.getBoundingBox();
                        MLatLonBox latLonBox = new MLatLonBox(
                                new MLatLon(bb.getSouth(), bb.getWest()),
                                new MLatLon(bb.getNorth(), bb.getEast())
                        );
                        bookmark.setLatLonBox(latLonBox);

                        mItems.add(bookmark);
                    }

                    mResultPopOver.setTitle(String.format("%d %s", mItems.size() - providerCount, Dict.HITS.toString()));
                });
            } catch (IOException ex) {
                mResultPopOver.hide();
                Exceptions.printStackTrace(ex);
            }
        }).start();
    }

    class GeocodingResultListCell extends ListCell<MBookmark> {

        private final VBox mBox = new VBox();
        private final Font mDefaultFont = Font.getDefault();
        private final Font mHeaderFont = new Font(mDefaultFont.getSize() * 1.5);
        private final Label mLabel = new Label();

        public GeocodingResultListCell() {
            createUI();
        }

        @Override
        protected void updateItem(MBookmark bookmark, boolean empty) {
            super.updateItem(bookmark, empty);

            if (bookmark == null || empty) {
                clearContent();
            } else {
                addContent(bookmark);
            }
        }

        private void addContent(MBookmark bookmark) {
            setText(null);
            String name = bookmark.getName();
            if (StringUtils.startsWith(name, PROVIDER_PREFIX)) {
                mLabel.setFont(mHeaderFont);
                name = StringUtils.removeStart(name, PROVIDER_PREFIX);
            } else {
                mLabel.setFont(mDefaultFont);

            }
            mLabel.setText(name);

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
