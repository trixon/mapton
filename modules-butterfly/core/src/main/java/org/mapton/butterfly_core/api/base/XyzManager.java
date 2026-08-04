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
package org.mapton.butterfly_core.api.base;

import java.util.ArrayList;
import java.util.List;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MLatLon;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
public class XyzManager extends BaseManager<BXyzPoint> {

    private final static String DISRUPTOR_NAME = "Baspunkt";
//    private final XyzLayerOptions mLayerOptions = XyzLayerOptions.getInstance();
    private final XyzPropertiesBuilder mPropertiesBuilder = new XyzPropertiesBuilder();

    public static XyzManager getInstance() {
        return Holder.INSTANCE;
    }

    private XyzManager() {
        super(BXyzPoint.class);
    }

    @Override
    public List<String> getObjectAnnotation(BXyzPoint p) {
//        if (mLayerOptions.isPlotAnnotation()) {
//            var ext = p.extOrNull();
//            return List.of(
//                    p.getName(),
//                    ext.getDateLatest() != null ? ext.getDateLatest().toLocalDate().toString() : "-"
//            );
//        } else {
//            return null;
//        }
        return null;
    }

    @Override
    public Object getObjectChart(BXyzPoint selectedObject) {
//        return mMultiChartAggregate.build(selectedObject);
        return null;

    }

    @Override
    public Object getObjectProperties(BXyzPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
    }

    @Override
    protected void applyTemporalFilter() {
        var timeFilteredItems = getFilteredItems().stream()
                .filter(p -> p.getDateLatest() == null ? true : getTemporalManager().isValid(p.getDateLatest()))
                .toList();

        var latLonDisruptors = timeFilteredItems.stream().map(p -> new MLatLon(p.getLat(), p.getLon())).toList();
        mDisruptorManager.putLatLons(DISRUPTOR_NAME, latLonDisruptors);
        setItemsTimeFiltered(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BXyzPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class XyzDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final XyzManager INSTANCE = new XyzManager();
    }
}
