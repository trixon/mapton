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
package org.mapton.butterfly_topo.pair;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import org.mapton.api.ui.forms.FormFilter;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;

/**
 *
 * @author Patrik Karlström
 */
public class Pair1Filter extends FormFilter<Pair1Manager> {

    private final Pair1Manager mManager = Pair1Manager.getInstance();
    SimpleBooleanProperty mDeltaHSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mDeltaHMinProperty = new SimpleDoubleProperty();
    DoubleProperty mDeltaHMaxProperty = new SimpleDoubleProperty();
    SimpleBooleanProperty mDeltaRSelectedProperty = new SimpleBooleanProperty();
    DoubleProperty mDeltaRMinProperty = new SimpleDoubleProperty();
    DoubleProperty mDeltaRMaxProperty = new SimpleDoubleProperty();

    public Pair1Filter() {
        super(Pair1Manager.getInstance());

        initListeners();
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateFreeText(p.getName()))
                .filter(p -> validateDeltaH(p))
                .filter(p -> validateDeltaR(p))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    private boolean inRange(double value, DoubleProperty minProperty, DoubleProperty maxProperty) {
        return value >= minProperty.get() && value <= maxProperty.get();
    }

    private void initListeners() {
    }

    public void initPropertyListeners() {
        mDeltaHSelectedProperty.addListener(mChangeListenerObject);
        mDeltaHMinProperty.addListener(mChangeListenerObject);
        mDeltaHMaxProperty.addListener(mChangeListenerObject);

        mDeltaRSelectedProperty.addListener(mChangeListenerObject);
        mDeltaRMinProperty.addListener(mChangeListenerObject);
        mDeltaRMaxProperty.addListener(mChangeListenerObject);
    }

    private boolean validateDeltaH(BTopoPointPair p) {
        if (mDeltaHSelectedProperty.get()) {
            return inRange(p.getDistanceHeight(), mDeltaHMinProperty, mDeltaHMaxProperty);
        } else {
            return true;
        }
    }

    private boolean validateDeltaR(BTopoPointPair p) {
        if (mDeltaRSelectedProperty.get()) {
            return inRange(p.getDistancePlane(), mDeltaRMinProperty, mDeltaRMaxProperty);
        } else {
            return true;
        }
    }
}
