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
package org.mapton.butterfly_alarms;

import java.util.ArrayList;
import org.openide.util.Exceptions;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BAlarm;
import org.mapton.butterfly_api.api.BaseManager;

/**
 *
 * @author Patrik Karlström
 */
public class AlarmsManager extends BaseManager<BAlarm> {

    public static AlarmsManager getInstance() {
        return Holder.INSTANCE;
    }

    private AlarmsManager() {
        super(BAlarm.class);
    }

    @Override
    public Object getObjectProperties(BAlarm selectedObject) {
        return selectedObject;
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.getAlarms());
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

        private static final AlarmsManager INSTANCE = new AlarmsManager();
    }
}
