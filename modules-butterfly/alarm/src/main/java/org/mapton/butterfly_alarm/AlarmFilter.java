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

import org.mapton.butterfly_alarm.api.AlarmManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.mapton.api.ui.forms.FormFilter;

/**
 *
 * @author Patrik Karlström
 */
public class AlarmFilter extends FormFilter<AlarmManager> {

    private final AlarmManager mManager = AlarmManager.getInstance();
    private final BooleanProperty mProperty = new SimpleBooleanProperty();

    public AlarmFilter() {
        super(AlarmManager.getInstance());

        initListeners();
    }

    public BooleanProperty property() {
        return mProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(o -> validateFreeText(o.getName(), o.getDescription(), o.getId()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    private void initListeners() {
        mProperty.addListener(mChangeListenerObject);
    }
}
