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
package org.mapton.api.ui.forms;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import org.mapton.api.MAreaFilterManager;
import org.mapton.api.MBaseDataManager;
import org.mapton.api.MPolygonFilterManager;
import se.trixon.almond.util.fx.DelayedResetRunner;

/**
 *
 * @author Patrik Karlström
 * @param <ManagerType>
 */
public abstract class FormFilter<ManagerType extends MBaseDataManager> {

    protected ChangeListener<Boolean> mChangeListenerBoolean;
    protected ChangeListener<String> mChangeListenerString;
    private final MAreaFilterManager mAreaFilterManager = MAreaFilterManager.getInstance();
    private final DelayedResetRunner mDelayedResetRunner;
    private final StringProperty mFreeTextProperty = new SimpleStringProperty();
    private final MBaseDataManager mManager;
    private final MPolygonFilterManager mPolygonFilterManager = MPolygonFilterManager.getInstance();
    private final BooleanProperty mPolygonFilterProperty = new SimpleBooleanProperty(false);

    public FormFilter(MBaseDataManager manager) {
        mManager = manager;
        mDelayedResetRunner = new DelayedResetRunner(200, () -> {
            update();
        });

        initListeners();
    }

    public void bindFreeTextProperty(StringProperty freeTextProperty) {
        freeTextProperty.bindBidirectional(freeTextProperty());
    }

    public StringProperty freeTextProperty() {
        return mFreeTextProperty;
    }

    public String getFreeText() {
        return freeTextProperty().get();
    }

    public BooleanProperty polygonFilterProperty() {
        return mPolygonFilterProperty;
    }

    public abstract void update();

    public boolean validateCoordinateArea(Double lat, Double lon) {
        boolean valid = mAreaFilterManager.isValidCoordinate(lat, lon);

        return valid;
    }

    public boolean validateCoordinateRuler(Double lat, Double lon) {
        boolean valid = !mPolygonFilterManager.hasItems()
                || !polygonFilterProperty().get()
                || polygonFilterProperty().get() && mPolygonFilterManager.contains(lat, lon);

        return valid;
    }

    private void initListeners() {
        mManager.getAllItems().addListener((ListChangeListener.Change c) -> {
            mDelayedResetRunner.reset();
        });

        mChangeListenerBoolean = (p, o, n) -> {
            mDelayedResetRunner.reset();
        };

        mChangeListenerString = (p, o, n) -> {
            mDelayedResetRunner.reset();
        };

        freeTextProperty().addListener(mChangeListenerString);
    }

}
