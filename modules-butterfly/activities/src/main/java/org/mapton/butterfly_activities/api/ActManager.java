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
package org.mapton.butterfly_activities.api;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_activities.ActPropertiesBuilder;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAreaActivity;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ActManager extends BaseManager<BAreaActivity> {

    private final ActPropertiesBuilder mPropertiesBuilder = new ActPropertiesBuilder();

    public static ActManager getInstance() {
        return ActManagerHolder.INSTANCE;
    }

    private ActManager() {
        super(BAreaActivity.class);
    }

    @Override
    public Object getObjectProperties(BAreaActivity selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getAreaActivities());
            initObjectToItemMap();

            var dates = new TreeSet<>(getAllItems().stream()
                    .map(aa -> aa.getDatFrom())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            dates.addAll(getAllItems().stream()
                    .map(aa -> aa.getDatTo())
                    .filter(d -> d != null)
                    .collect(Collectors.toSet()));

            if (!dates.isEmpty()) {
                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        var timeFilteredItems = new ArrayList<BAreaActivity>();
        for (var aa : getFilteredItems()) {
            var validFrom = aa.getDatFrom() == null || getTemporalManager().isValid(aa.getDatFrom());
            var validTo = aa.getDatTo() == null || getTemporalManager().isValid(aa.getDatTo());

            if (validFrom || validTo) {
                timeFilteredItems.add(aa);
            }
        }

        getTimeFilteredItems().setAll(timeFilteredItems);
    }

    @Override
    protected void load(ArrayList<BAreaActivity> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class ActManagerHolder {

        private static final ActManager INSTANCE = new ActManager();
    }
}
