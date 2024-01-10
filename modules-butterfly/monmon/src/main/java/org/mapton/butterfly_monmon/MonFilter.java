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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ListChangeListener;
import org.controlsfx.control.IndexedCheckModel;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;

/**
 *
 * @author Patrik Karlström
 */
public class MonFilter extends FormFilter<MonManager> {

    IndexedCheckModel<String> mStatusCheckModel;
    private final MonManager mManager = MonManager.getInstance();
    private final BooleanProperty mProperty = new SimpleBooleanProperty();
    private final TopoManager mTopoManager = TopoManager.getInstance();

    public MonFilter() {
        super(MonManager.getInstance());

        initListeners();
    }

    public BooleanProperty property() {
        return mProperty;
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(mon -> validateFreeText(mon.getName(), mon.getStationName()))
                .filter(mon -> mTopoManager.getTimeFilteredItemsMap().containsKey(mon.getName()))
                //                .filter(mon -> validateCheck(mStatusCheckModel, ActHelper.getStatusAsString(mon.getStatus())))
                .filter(mon -> validateCoordinateArea(mon.getLat(), mon.getLon()))
                .filter(mon -> validateCoordinateRuler(mon.getLat(), mon.getLon()))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    void initCheckModelListeners() {
        mStatusCheckModel.getCheckedItems().addListener(mListChangeListener);
    }

    private void initListeners() {
        mProperty.addListener(mChangeListenerObject);
        mTopoManager.getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            update();
        });
    }
}
