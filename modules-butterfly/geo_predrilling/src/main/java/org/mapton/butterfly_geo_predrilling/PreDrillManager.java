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
package org.mapton.butterfly_geo_predrilling;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MLatLon;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.geo.BGeoPreDrillPoint;
import org.mapton.butterfly_geo_predrilling.chart.PreDrillMultiChartAggregate;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
public class PreDrillManager extends BaseManager<BGeoPreDrillPoint> {

    private final static String DISRUPTOR_NAME = Bundle.CTL_PreDrillAction();
    private final PreDrillMultiChartAggregate mMultiChartAggregate = new PreDrillMultiChartAggregate();
    private final PreDrillOptions mOptions = PreDrillOptions.getInstance();
    private final PreDrillPropertiesBuilder mPropertiesBuilder = new PreDrillPropertiesBuilder();

    public static PreDrillManager getInstance() {
        return Holder.INSTANCE;
    }

    private PreDrillManager() {
        super(BGeoPreDrillPoint.class);
    }

    @Override
    public List<String> getObjectAnnotation(BGeoPreDrillPoint p) {
        if (mOptions.isPlotAnnotation()) {
            var ext = p.extOrNull();
            return List.of(
                    p.getName(),
                    ext.getDateLatest() != null ? ext.getDateLatest().toLocalDate().toString() : "-"
            );
        } else {
            return null;
        }
    }

    @Override
    public Object getObjectChart(BGeoPreDrillPoint selectedObject) {
        return mMultiChartAggregate.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BGeoPreDrillPoint selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.geotechnical().getGeoPreDrillPoints());
            var items = getAllItems();
            var dates = new TreeSet<>(items.stream()
                    .map(p -> p.getDateLatest())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }

            for (var item : items) {
                item.ext().setDateLatest(item.getDateLatest());
                item.ext().setDateFirst(item.getDateLatest());
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
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
    protected void load(ArrayList<BGeoPreDrillPoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class PreDrillDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final PreDrillManager INSTANCE = new PreDrillManager();
    }
}
