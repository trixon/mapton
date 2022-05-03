/*
 * Copyright 2022 Patrik Karlström.
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
package org.mapton.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.apache.commons.lang3.StringUtils;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MCrsManager {

    private final String KEY_SELECTED = "crs.selected";
    private final ArrayList<CoordinateReferenceSystem> mAllSystems = new ArrayList<>();
    private final ObjectProperty<ObservableList<MCooTrans>> mItemsProperty = new SimpleObjectProperty<>();
    private final Preferences mPreferences = NbPreferences.forModule(MCrsManager.class);
    private final ObjectProperty<CoordinateReferenceSystem> mSelectedSystemProperty = new SimpleObjectProperty<>();
    private final ObservableList<CoordinateReferenceSystem> mSelectedSystems = FXCollections.observableArrayList();

    public static MCrsManager getInstance() {
        return Holder.INSTANCE;
    }

    private MCrsManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        init();
        initListeners();

        updateProviders();
    }

    public ArrayList<CoordinateReferenceSystem> getAllSystems() {
        return mAllSystems;
    }

    public final ObservableList<MCooTrans> getItems() {
        return mItemsProperty.get();
    }

    public CoordinateReferenceSystem getSelectedSystem() {
        return mSelectedSystemProperty.get();
    }

    public ObservableList<CoordinateReferenceSystem> getSelectedSystems() {
        return mSelectedSystems;
    }

    public final ObjectProperty<ObservableList<MCooTrans>> itemsProperty() {
        return mItemsProperty;
    }

    public void save(ObservableList<CoordinateReferenceSystem> coordinateReferenceSystems) {
        var sb = new StringBuilder();
        for (var crs : coordinateReferenceSystems) {
            sb.append(String.format("%s\n", CRS.toSRS(crs)));
        }
        mPreferences.put(KEY_SELECTED, sb.toString());
        mSelectedSystems.setAll(coordinateReferenceSystems);

        updateProviders();
    }

    public ObjectProperty<CoordinateReferenceSystem> selectedSystemProperty() {
        return mSelectedSystemProperty;
    }

    public void setSelectedSystem(CoordinateReferenceSystem crs) {
        mSelectedSystemProperty.set(crs);
    }

    private void init() {
        var defaultSystems = """
                             EPSG:3007
                             EPSG:3008
                             EPSG:3012
                             EPSG:3009
                             EPSG:3013
                             EPSG:3010
                             EPSG:3014
                             EPSG:3011
                             EPSG:3015
                             EPSG:3016
                             EPSG:3017
                             EPSG:3018
                             EPSG:3006
                             """;
        var items = StringUtils.split(mPreferences.get(KEY_SELECTED, defaultSystems), "\n");
        var storedSystems = new HashSet<String>(Arrays.asList(items));
        var codes = SystemHelper.getResourceAsString(getClass(), "AuthoritiesCodes.txt");

        for (var ac : codes.lines().toList()) {
            try {
                var crs = CRS.decode(ac);
                mAllSystems.add(crs);
                if (storedSystems.contains(ac)) {
                    mSelectedSystems.add(crs);
                }
            } catch (FactoryException ex) {
                Logger.getLogger(MCrsManager.class.getName()).log(Level.SEVERE, null, ex);
            }

            Comparator<CoordinateReferenceSystem> c = ((o1, o2) -> o1.getName().toString().compareToIgnoreCase(o2.getName().toString()));
            Collections.sort(mAllSystems, c);
            Collections.sort(mSelectedSystems, c);
        }
    }

    private void initListeners() {
        Lookup.getDefault().lookupResult(MCooTrans.class).addLookupListener(lookupEvent -> {
            updateProviders();
        });

    }

    private void updateProviders() {
        var lookupItems = new ArrayList<>(Lookup.getDefault().lookupAll(MCooTrans.class));
        var items = new ArrayList<MCooTrans>(lookupItems);
        for (var crs : mSelectedSystems) {
            items.add(new GeoToolsCooTrans(CRS.toSRS(crs)));
        }

        mItemsProperty.get().setAll(items);
    }

    private static class Holder {

        private static final MCrsManager INSTANCE = new MCrsManager();
    }
}
