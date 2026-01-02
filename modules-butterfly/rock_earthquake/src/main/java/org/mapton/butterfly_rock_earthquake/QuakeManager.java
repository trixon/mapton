/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_rock_earthquake;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MDisruptorProvider;
import org.mapton.api.MLatLon;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.rock.BRockEarthquake;
import org.mapton.butterfly_rock_earthquake.chart.QuakeMultiChartAggregate;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
public class QuakeManager extends BaseManager<BRockEarthquake> {

    private final static String DISRUPTOR_NAME = Bundle.CTL_EarthquakeAction();
    private final QuakeMultiChartAggregate mMultiChartAggregate = new QuakeMultiChartAggregate();
    private final QuakePropertiesBuilder mPropertiesBuilder = new QuakePropertiesBuilder();

    public static QuakeManager getInstance() {
        return Holder.INSTANCE;
    }

    private QuakeManager() {
        super(BRockEarthquake.class);
    }

    @Override
    public Object getObjectChart(BRockEarthquake selectedObject) {
        return mMultiChartAggregate.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BRockEarthquake selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.rock().getEarthquakes());
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
    protected void load(ArrayList<BRockEarthquake> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @ServiceProvider(service = MDisruptorProvider.class)
    public static class QuakeDisruptorProvider implements MDisruptorProvider {

        @Override
        public String getName() {
            return DISRUPTOR_NAME;
        }
    }

    private static class Holder {

        private static final QuakeManager INSTANCE = new QuakeManager();
    }
}
