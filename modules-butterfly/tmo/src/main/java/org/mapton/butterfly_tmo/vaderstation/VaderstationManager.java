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
package org.mapton.butterfly_tmo.vaderstation;

import java.util.ArrayList;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.mapton.api.MTemporalRange;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.tmo.BVaderstation;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class VaderstationManager extends BaseManager<BVaderstation> {

    private final VaderstationPropertiesBuilder mPropertiesBuilder = new VaderstationPropertiesBuilder();

    public static VaderstationManager getInstance() {
        return Holder.INSTANCE;
    }

    private VaderstationManager() {
        super(BVaderstation.class);
    }

    @Override
    public Object getObjectProperties(BVaderstation selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.tmo().getVaderstation());

            var dates = new TreeSet<>(getAllItems().stream()
                    .map(o -> o.getInstallationsdatum()) //TODO use latest observation date
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
//        var timeFilteredItems = getFilteredItems().stream()
//                .filter(o -> o.getDateTime() == null ? true : getTemporalManager().isValid(o.getDateTime()))
//                .toList();

        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BVaderstation> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final VaderstationManager INSTANCE = new VaderstationManager();
    }
}
