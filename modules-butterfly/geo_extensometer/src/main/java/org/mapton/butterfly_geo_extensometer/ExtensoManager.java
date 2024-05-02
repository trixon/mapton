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
package org.mapton.butterfly_geo_extensometer;

import java.util.ArrayList;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.geo.BGeoExtensometer;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ExtensoManager extends BaseManager<BGeoExtensometer> {

    private final ExtensoPropertiesBuilder mPropertiesBuilder = new ExtensoPropertiesBuilder();

    public static ExtensoManager getInstance() {
        return Holder.INSTANCE;
    }

    private ExtensoManager() {
        super(BGeoExtensometer.class);
        initListeners();
    }

    @Override
    public Object getObjectProperties(BGeoExtensometer selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.geotechnical().getExtensometers());
            initObjectToItemMap();
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BGeoExtensometer> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private void initListeners() {
    }

    private static class Holder {

        private static final ExtensoManager INSTANCE = new ExtensoManager();
    }
}
