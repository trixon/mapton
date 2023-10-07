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
package org.mapton.worldwind;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.apache.commons.lang3.StringUtils;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MKey;
import org.mapton.api.MWmsStyle;
import org.mapton.api.Mapton;
import org.mapton.worldwind.api.MapStyle;
import org.openide.util.Lookup;
import se.trixon.almond.util.StringHelper;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 */
public class LayerMapStyleManager extends MBaseDataManager<MapStyle> {

    private DelayedResetRunner mDelayedResetRunner;
    private String mFilter = "";

    public static LayerMapStyleManager getInstance() {
        return Holder.INSTANCE;
    }

    private LayerMapStyleManager() {
        super(MapStyle.class);
        initListeners();
        load();
        mDelayedResetRunner.reset();
    }

    public MapStyle getById(String id) {
        return getAllItems().stream().filter(s -> StringUtils.equalsIgnoreCase(id, s.getId())).findAny().orElse(null);
    }

    public void refresh() {
        refresh(mFilter);
    }

    public void refresh(String filter) {
        mFilter = filter;
        mDelayedResetRunner.reset();
    }

    @Override
    protected void applyTemporalFilter() {
        //No dates, all is valid
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<MapStyle> items) {
    }

    private void applyFilter() {
        var filteredMapStyles = new ArrayList<MapStyle>();

        getAllItems().stream()
                .filter(mapStyle -> !StringUtils.isBlank(mapStyle.getCategory()))
                .filter(mapStyle -> valid(mapStyle, mFilter))
                .forEachOrdered(mapStyle -> {
                    filteredMapStyles.add(mapStyle);
                });

        getFilteredItems().setAll(filteredMapStyles);
    }

    private void initListeners() {
        mDelayedResetRunner = new DelayedResetRunner(200, () -> {
            applyFilter();
        });

        Lookup.getDefault().lookupResult(MapStyle.class).addLookupListener(lookupEvent -> {
            load();
        });

        Mapton.getGlobalState().addListener(gsce -> {
            load();
        }, MKey.DATA_SOURCES_WMS_STYLES);

        selectedItemProperty().addListener((p, o, n) -> {
            sendObjectProperties(n);
        });
    }

    private void load() {
        ArrayList<MapStyle> mapStyles = new ArrayList<>(Lookup.getDefault().lookupAll(MapStyle.class));
        ArrayList<MWmsStyle> wmsStyles = Mapton.getGlobalState().get(MKey.DATA_SOURCES_WMS_STYLES);

        if (wmsStyles != null) {
            for (var wmsStyle : wmsStyles) {
                mapStyles.add(MapStyle.createFromWmsStyle(wmsStyle));
            }
        }

        Comparator<MapStyle> c1 = (o1, o2) -> StringUtils.defaultString(o1.getCategory()).compareTo(StringUtils.defaultString(o2.getCategory()));
        Comparator<MapStyle> c2 = (o1, o2) -> o1.getName().compareTo(o2.getName());
        Collections.sort(mapStyles, c1.thenComparing(c2));

        getAllItems().setAll(mapStyles);
    }

    @SuppressWarnings("unchecked")
    private void sendObjectProperties(MapStyle mapStyle) {
        Mapton.getGlobalState().put(MKey.OBJECT_PROPERTIES, mapStyle);
    }

    private boolean valid(MapStyle mapStyle, String filter) {
        boolean valid = StringHelper.matchesSimpleGlob(mapStyle.getCategory(), filter, true, true)
                || StringHelper.matchesSimpleGlob(mapStyle.getName(), filter, true, true)
                || StringHelper.matchesSimpleGlob(mapStyle.getSuppliers(), filter, true, true)
                || StringHelper.matchesSimpleGlob(mapStyle.getDescription(), filter, true, true);

        return valid;
    }

    private static class Holder {

        private static final LayerMapStyleManager INSTANCE = new LayerMapStyleManager();
    }
}
