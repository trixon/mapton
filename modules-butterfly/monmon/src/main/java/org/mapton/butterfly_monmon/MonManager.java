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
package org.mapton.butterfly_monmon;

import java.util.ArrayList;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.monmon.BMonmon;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class MonManager extends BaseManager<BMonmon> {

    private final MonPropertiesBuilder mPropertiesBuilder = new MonPropertiesBuilder();

    public static MonManager getInstance() {
        return ActManagerHolder.INSTANCE;
    }

    private MonManager() {
        super(BMonmon.class);
    }

    @Override
    public Object getObjectProperties(BMonmon selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getMonmons());
            initObjectToItemMap();

//            var dates = new TreeSet<>(getAllItems().stream()
//                    .map(aa -> aa.getDatFrom())
//                    .filter(d -> d != null)
//                    .collect(Collectors.toSet()));
//
//            dates.addAll(getAllItems().stream()
//                    .map(aa -> aa.getDatTo())
//                    .filter(d -> d != null)
//                    .collect(Collectors.toSet()));
//
//            if (!dates.isEmpty()) {
//                setTemporalRange(new MTemporalRange(dates.first(), dates.last()));
//            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BMonmon> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class ActManagerHolder {

        private static final MonManager INSTANCE = new MonManager();
    }
}
