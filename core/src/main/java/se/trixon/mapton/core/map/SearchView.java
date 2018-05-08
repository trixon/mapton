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

import com.lynden.gmapsfx.javascript.object.LatLong;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.textfield.CustomTextField;
import org.controlsfx.control.textfield.TextFields;
import org.openide.util.NbBundle;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.mapton.core.api.DecDegDMS;
import static se.trixon.mapton.core.api.Mapton.getIconSizeToolBarInt;

/**
 *
 * @author Patrik Karlström
 */
public class SearchView {

    private static final ResourceBundle mBundle = NbBundle.getBundle(SearchView.class);

    private final MapController mMapController = MapController.getInstance();
    private CustomTextField mSearchTextField;

    public SearchView() {
        mSearchTextField = (CustomTextField) TextFields.createClearableTextField();
        mSearchTextField.setLeft(MaterialIcon._Action.SEARCH.getImageView(getIconSizeToolBarInt() - 4));
        mSearchTextField.setPromptText(mBundle.getString("search_prompt"));
        mSearchTextField.setPrefColumnCount(30);
        mSearchTextField.setOnAction((ActionEvent event) -> {
            final String searchString = mSearchTextField.getText();
            if (!StringUtils.isBlank(searchString)) {
                parse(searchString);
            }
        });
    }

    public Node getNode() {
        return mSearchTextField;
    }

    private void parse(String searchString) {
        LatLong latLong = parseDecimal(searchString);
        if (latLong == null) {
            latLong = parseDegMinSec(searchString);
        }

        if (latLong == null) {
            System.out.println("Not a coordinate, search as text...");
        } else {
            mMapController.panTo(latLong);
        }
    }

    private LatLong parseDecimal(String searchString) {
        LatLong latLong = null;
        String[] coordinate = searchString.replace(",", " ").trim().split("\\s+");
        if (coordinate.length == 2) {
            try {
                final Double lat = NumberUtils.createDouble(coordinate[0]);
                final Double lon = NumberUtils.createDouble(coordinate[1]);
                Wgs84DMS dms = new Wgs84DMS();
                if (dms.isValid(lat, lon)) {
                    latLong = new LatLong(lat, lon);
                }
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

    private LatLong parseDegMinSec(String searchString) {
        LatLong latLong = null;
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
                latLong = new LatLong(dddms.getLatitude(), dddms.getLongitude());
            } catch (Exception e) {
                // nvm
            }
        }

        return latLong;
    }

}
