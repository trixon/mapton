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
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;
import org.geotools.referencing.CRS;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MCrsManager {

    private static final Logger LOGGER = Logger.getLogger(MCrsManager.class.getName());

    private final String KEY_SELECTED = "crs.selected";
    private final ObservableList<CoordinateReferenceSystem> mAllSystems = FXCollections.observableArrayList();
    private final ObjectProperty<ObservableList<MCooTrans>> mItemsProperty = new SimpleObjectProperty<>();
    private final Preferences mPreferences = NbPreferences.forModule(MCrsManager.class);
    private final ObjectProperty<CoordinateReferenceSystem> mSelectedSystemProperty = new SimpleObjectProperty<>();
    private final ObservableList<CoordinateReferenceSystem> mSelectedSystems = FXCollections.observableArrayList();

    public static MCrsManager getInstance() {
        return Holder.INSTANCE;
    }

    private MCrsManager() {
        mItemsProperty.setValue(FXCollections.observableArrayList());
        new Thread(() -> {
            init();
            initListeners();

            updateProviders();
        }).start();
    }

    public ObservableList<CoordinateReferenceSystem> getAllSystems() {
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
            sb.append("%s\n".formatted(CRS.toSRS(crs)));
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
        Comparator<CoordinateReferenceSystem> comparator = ((o1, o2) -> o1.getName().toString().compareToIgnoreCase(o2.getName().toString()));

        for (var ac : storedSystems) {
            try {
                mSelectedSystems.add(CRS.decode(ac));
            } catch (FactoryException ex) {
                LOGGER.log(Level.SEVERE, null, ex);
            }
        }

        Collections.sort(mSelectedSystems, comparator);

        new Thread(() -> {
            for (var ac : codes.lines().toList()) {
                try {
                    mAllSystems.add(CRS.decode(ac));
                } catch (FactoryException ex) {
                    LOGGER.log(Level.SEVERE, null, ex);
                }
            }

            Collections.sort(mAllSystems, comparator);
        }).start();
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
