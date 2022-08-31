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
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeSet;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.ListChangeListener;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.Lookup;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class MPoiManager extends MBaseDataManager<MPoi> {

    private final ObjectProperty<TreeSet<String>> mCategoriesProperty = new SimpleObjectProperty<>();
    private DelayedResetRunner mDelayedResetRunner;
    private String mFilter = "";
    private final MPolygonFilterManager mPolygonFilterManager = MPolygonFilterManager.getInstance();
    private final BooleanProperty mPolygonFilterProperty = new SimpleBooleanProperty(false);
    private final SimpleObjectProperty<Long> mTrigRefreshCategoriesProperty = new SimpleObjectProperty<>();

    public static MPoiManager getInstance() {
        return Holder.INSTANCE;
    }

    private MPoiManager() {
        super(MPoi.class);
        mCategoriesProperty.set(new TreeSet<>());
        initListeners();

        mDelayedResetRunner.reset();
    }

    public ObjectProperty<TreeSet<String>> categoriesProperty() {
        return mCategoriesProperty;
    }

    public BooleanProperty polygonFilterProperty() {
        return mPolygonFilterProperty;
    }

    public void refresh() {
        refresh(mFilter);
    }

    public void refresh(String filter) {
        mFilter = filter;
        mDelayedResetRunner.reset();
    }

    public SimpleObjectProperty<Long> trigRefreshCategoriesProperty() {
        return mTrigRefreshCategoriesProperty;
    }

    @Override
    protected void applyTemporalFilter() {
        //No dates for pois yet, all is valid
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<MPoi> items) {
    }

    private void applyFilter() {
        var filteredPois = new ArrayList<MPoi>();

        getAllItems().stream()
                .filter(poi -> (validPoi(poi, mFilter)))
                .forEachOrdered(poi -> {
                    filteredPois.add(poi);
                });

        Comparator<MPoi> comparator = Comparator.comparing(MPoi::getProvider)
                .thenComparing(Comparator.comparing(MPoi::getCategory))
                .thenComparing(Comparator.comparing(MPoi::getName));
        filteredPois.sort(comparator);

        getFilteredItems().setAll(filteredPois);
    }

    private void initListeners() {
        mDelayedResetRunner = new DelayedResetRunner(200, () -> {
            var allPois = new ArrayList<MPoi>();
            Lookup.getDefault().lookupAll(MPoiProvider.class).forEach(poiProvider -> {
                try {
                    for (var poi : poiProvider.getPois()) {
                        poi.setCategory(StringUtils.defaultIfBlank(poi.getCategory(), "_DEFAULT"));
                        poi.setProvider(poiProvider.getName());
                        poi.setZoom(poi.getZoom() != null ? poi.getZoom() : 0.9);
                        allPois.add(poi);
                    }
                } catch (Exception e) {
                    System.err.println("Failed to load POI from " + poiProvider.getName());
                }
            });

            getAllItems().setAll(allPois);
            //Hand off PoiCategoryCheckTreeView to return refreshed categories
            mTrigRefreshCategoriesProperty.set(System.currentTimeMillis());
        });

        Lookup.getDefault().lookupResult(MPoiProvider.class).addLookupListener(lookupEvent -> {
            mDelayedResetRunner.reset();
        });

        // Gets updated in PoiCategoryCheckTreeView
        mCategoriesProperty.addListener((observable, oldValue, newValue) -> {
            applyFilter();
        });

        selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            sendObjectProperties(newValue);
        });

        MBookmarkManager.getInstance().getItems().addListener((ListChangeListener.Change<? extends MBookmark> c) -> {
            refresh();
        });

        mPolygonFilterManager.addListener(() -> {
            refresh();
        });
    }

    private void sendObjectProperties(MPoi poi) {
        Object propertyPresenter = null;

        if (poi != null) {
            if (poi.getPropertyNode() != null) {
                propertyPresenter = poi.getPropertyNode();
                if (propertyPresenter instanceof MGenericLoader genericLoader) {
                    genericLoader.load(poi.getPropertySource());
                }
            } else {
                Map<String, Object> propertyMap = new LinkedHashMap<>();
                if (poi.getPropertyMap() != null) {
                    propertyMap = poi.getPropertyMap();
                } else {
                    Mapton.getGlobalState().put(MKey.BACKGROUND_IMAGE, new MBackgroundImage(poi.getExternalImageUrl(), 0.95));
                    propertyMap.put(Dict.NAME.toString(), poi.getName());
                    propertyMap.put(Dict.CATEGORY.toString(), poi.getCategory());
                    propertyMap.put(Dict.SOURCE.toString(), poi.getProvider());
                    propertyMap.put(Dict.DESCRIPTION.toString(), poi.getDescription());
                    propertyMap.put(Dict.TAGS.toString(), poi.getTags());
                    propertyMap.put(Dict.COLOR.toString(), javafx.scene.paint.Color.web(poi.getColor()));
                    propertyMap.put("URL", poi.getUrl());
                }
                propertyPresenter = propertyMap;
            }
        }

        Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, propertyPresenter);
    }

    private boolean validPoi(MPoi poi, String filter) {
        String category = "%s/%s".formatted(poi.getProvider(), poi.getCategory());
        boolean valid
                = mCategoriesProperty.get().contains(category)
                && ObjectUtils.allNotNull(poi.getLatitude(), poi.getLongitude())
                && (StringHelper.matchesSimpleGlob(poi.getProvider(), filter, true, true)
                || StringHelper.matchesSimpleGlob(poi.getUrl(), filter, true, true)
                || StringHelper.matchesSimpleGlob(poi.getName(), filter, true, true)
                || StringHelper.matchesSimpleGlob(poi.getCategory(), filter, true, true)
                || StringHelper.matchesSimpleGlob(poi.getGroup(), filter, true, true)
                || StringHelper.matchesSimpleGlob(poi.getDescription(), filter, true, true));

        boolean validCoordinate = !mPolygonFilterManager.hasItems() || !mPolygonFilterProperty.get() || mPolygonFilterProperty.get() && mPolygonFilterManager.contains(poi.getLatitude(), poi.getLongitude());

        return valid && validCoordinate;
    }

    private static class Holder {

        private static final MPoiManager INSTANCE = new MPoiManager();
    }
}
