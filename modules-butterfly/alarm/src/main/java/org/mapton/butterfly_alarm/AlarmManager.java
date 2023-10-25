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
package org.mapton.butterfly_alarm;

import java.util.ArrayList;
import org.mapton.butterfly_api.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAlarm;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class AlarmManager extends BaseManager<BAlarm> {

    public static AlarmManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlarmManager() {
        super(BAlarm.class);
    }

    @Override
    public Object getObjectProperties(BAlarm selectedObject) {
        return selectedObject;
    }

    @Override
    public void initObjectToItemMap() {
        getAllItemsMap().clear();
        for (var item : getAllItems()) {
            getAllItemsMap().put(item.getId(), item);
        }
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getAlarms());
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
    protected void load(ArrayList<BAlarm> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final AlarmManager INSTANCE = new AlarmManager();
    }
}