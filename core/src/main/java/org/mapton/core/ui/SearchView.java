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
package org.mapton.core.ui;

import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;
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
import org.mapton.api.MOptions;
import org.mapton.api.MSearchEngine;
import org.mapton.api.Mapton;
import static org.mapton.api.Mapton.getIconSizeToolBarInt;
import org.mapton.core.Wgs84DMS;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlström
 */
public class SearchView {

    private static final Logger LOGGER = Logger.getLogger(SearchView.class.getName());
    private static final String PROVIDER_PREFIX = "> > > ";
    private final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);
    private final ArrayList<MSearchEngine> mInstantEngines = new ArrayList<>();
    private int mInstantProviderCount;
    private final ArrayList<MBookmark> mInstantResults = new ArrayList<>();
    private final ObservableList<MBookmark> mItems = FXCollections.observableArrayList();
    private final MOptions mOptions = MOptions.getInstance();
    private final ArrayList<MSearchEngine> mRegularEngines = new ArrayList<>();
    private int mRegularProviderCount;
    private PopOver mResultPopOver;
    private final ListView<MBookmark> mResultView = new ListView();
    private CustomTextField mSearchTextField;

    public SearchView() {
        createUI();
        initListeners();
        new Thread(() -> {
            try {
                Thread.sleep(4000);
                ArrayList<MSearchEngine> searchEngines = new ArrayList<>(Lookup.getDefault().lookupAll(MSearchEngine.class));
                searchEngines.sort((MSearchEngine o1, MSearchEngine o2) -> o1.getName().compareTo(o2.getName()));
                searchEngines.forEach((searchEngine) -> {
                    Mapton.logLoading("Search engine", searchEngine.getName());
                });
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }).start();

        populateEngines();
    }

    public Node getPresenter() {
        return mSearchTextField;
    }

    private void createUI() {
        mSearchTextField = (CustomTextField) TextFields.createClearableTextField();
        mSearchTextField.setLeft(MaterialIcon._Action.SEARCH.getImageView(getIconSizeToolBarInt() - 4));
        mSearchTextField.setPromptText(mBundle.getString("search_prompt"));
        mSearchTextField.setPrefColumnCount(20);
        mSearchTextField.setText("");

        mResultPopOver = new PopOver();
        mResultPopOver.setTitle("");
        mResultPopOver.setArrowLocation(PopOver.ArrowLocation.TOP_CENTER);
        mResultPopOver.setHeaderAlwaysVisible(true);
        mResultPopOver.setCloseButtonEnabled(false);
        mResultPopOver.setDetachable(false);
        mResultPopOver.setContentNode(mResultView);
        mResultPopOver.setAnimated(true);
        int fadeDuration = 800;
        mResultPopOver.setFadeInDuration(Duration.millis(fadeDuration));
        mResultPopOver.setFadeOutDuration(Duration.millis(fadeDuration));

        mResultView.prefWidthProperty().bind(mSearchTextField.widthProperty());
        mResultView.setItems(mItems);
        mResultView.setCellFactory((ListView<MBookmark> param) -> new SearchResultListCell());
    }

    private void initListeners() {
        mSearchTextField.textProperty().addListener((observable, oldValue, searchString) -> {
            if (StringUtils.isNotBlank(searchString)) {
                searchInstantly(searchString);
            } else {
                mResultPopOver.hide();
            }
        });

        mResultView.setOnKeyPressed((KeyEvent keyEvent) -> {
            if (keyEvent.getCode() == KeyCode.ENTER) {
                String searchString = mSearchTextField.getText();
                if (StringUtils.isNotBlank(searchString)) {
                    searchInstantly(searchString);
                    parse(searchString);
                }
            } else if (keyEvent.getCode() == KeyCode.ESCAPE) {
                mResultPopOver.hide();
            }
        });

        mResultView.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends MBookmark> observable, MBookmark oldValue, MBookmark bookmark) -> {
            try {
                if (!bookmark.isCategory()) {
                    if (bookmark.getZoom() != null) {
                        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()), bookmark.getZoom());
                    } else {
                        Mapton.getEngine().panTo(new MLatLon(bookmark.getLatitude(), bookmark.getLongitude()));
                    }
                }
            } catch (Exception e) {
            }
        });

        mResultView.setOnMousePressed((MouseEvent event) -> {
            if (event.isPrimaryButtonDown()) {
                MBookmark bookmark = mResultView.getSelectionModel().getSelectedItem();
                if (bookmark != null && !bookmark.isCategory()) {
                    mResultPopOver.hide();
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

        Lookup.getDefault().lookupResult(MSearchEngine.class).addLookupListener((LookupEvent ev) -> {
            populateEngines();
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
                searchRegular(searchString);
            }
        }

        if (latLong != null) {
            panTo(latLong);
        }
    }

    private MLatLon parseDecimal(String searchString) {
        MLatLon latLon = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");

        if (coordinate.length == 2) {
            try {
                final Double lat = NumberUtils.createDouble(coordinate[0]);
                final Double lon = NumberUtils.createDouble(coordinate[1]);
                Wgs84DMS dms = new Wgs84DMS();
                if (dms.isWithinWgs84Bounds(lon, lat)) {
                    latLon = new MLatLon(lat, lon);
                } else {
                    MCooTrans cooTrans = mOptions.getMapCooTrans();
                    if (cooTrans.isWithinProjectedBounds(lat, lon)) {
                        Point2D p = cooTrans.toWgs84(lat, lon);
                        latLon = new MLatLon(p.getY(), p.getX());
                    }
                }
            } catch (Exception e) {
                // nvm
            }
        }

        if (latLon != null) {
            mResultPopOver.hide();
        }

        return latLon;
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

    private void populateEngines() {
        mInstantEngines.clear();
        mRegularEngines.clear();

        ArrayList<MSearchEngine> engines = new ArrayList<>(Lookup.getDefault().lookupAll(MSearchEngine.class));
        engines.sort((MSearchEngine o1, MSearchEngine o2) -> o1.getName().compareTo(o2.getName()));

        engines.forEach((engine) -> {
            if (engine.isInstantSearch()) {
                mInstantEngines.add(engine);
            } else {
                mRegularEngines.add(engine);
            }
        });
    }

    private synchronized void search(String searchString, ArrayList<MSearchEngine> engines) {
        new Thread(() -> {
            for (MSearchEngine engine : engines) {
                ArrayList<MBookmark> bookmarks = engine.getResults(searchString);
                if (!bookmarks.isEmpty()) {
                    MBookmark b = new MBookmark();
                    b.setName(PROVIDER_PREFIX + engine.getName());
                    b.setId(new Long(bookmarks.size()));

                    if (engine == mInstantEngines) {
                        mInstantProviderCount++;
                        mInstantResults.add(b);
                        mInstantResults.addAll(bookmarks);
                    } else {
                        mRegularProviderCount++;
                        mItems.add(b);
                        mItems.addAll(bookmarks);
                    }
                }
            }

            mItems.addAll(mInstantResults);

            int hitCount = mItems.size() - mInstantProviderCount - mRegularProviderCount;

            Platform.runLater(() -> {
                if (!mResultPopOver.isShowing()) {
                    mResultPopOver.show(mSearchTextField);
                }
                mResultPopOver.setTitle(String.format("%d %s", hitCount, Dict.HITS.toString()));
            });
        }).start();
    }

    private synchronized void searchInstantly(String searchString) {
        mItems.clear();
        mInstantResults.clear();
        mInstantProviderCount = 0;
        mRegularProviderCount = 0;
        search(searchString, mInstantEngines);
    }

    private synchronized void searchRegular(String searchString) {
        mItems.clear();
        mItems.addAll(mInstantResults);
        mRegularProviderCount = 0;
        search(searchString, mRegularEngines);
    }

    class SearchResultListCell extends ListCell<MBookmark> {

        private final VBox mBox = new VBox();
        private final Font mDefaultFont = Font.getDefault();
        private final Font mHeaderFont = new Font(mDefaultFont.getSize() * 1.5);
        private final Label mLabel = new Label();

        public SearchResultListCell() {
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
                name = String.format("%s (%d)", StringUtils.removeStart(name, PROVIDER_PREFIX), bookmark.getId());
            } else {
                mLabel.setFont(mDefaultFont);
            }

            String nname = name;
            Platform.runLater(() -> {
                mLabel.setText(nname);
                setGraphic(mBox);
            });
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
