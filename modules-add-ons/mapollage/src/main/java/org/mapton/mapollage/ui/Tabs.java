/*
 * Copyright 2019 Patrik Karlström.
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
package org.mapton.mapollage.ui;

import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Region;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.ValidationSupport;
import org.mapton.mapollage.api.MapollageState;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Tabs extends TabPane {

    private MapollageState mMapollageState;
    private TabSelection mSelectionTab;
    private final ValidationSupport mValidationSupport = new ValidationSupport();
    private TabSources mSourceTab;
    private TabPlacemark mPlacemarkTab;
    private TabPath mPathTab;

    public Tabs() {
        createUI();
    }

    private void createUI() {
        setTabClosingPolicy(TabClosingPolicy.UNAVAILABLE);

        TabBase.setValidationSupport(mValidationSupport);

        mMapollageState = new MapollageState();
        mSelectionTab = new TabSelection(mMapollageState);
        mPlacemarkTab = new TabPlacemark(mMapollageState);
        mPathTab = new TabPath(mMapollageState);
        mSourceTab = new TabSources(mMapollageState);

        getTabs().setAll(mSelectionTab, mPlacemarkTab, mPathTab, mSourceTab);

        final int size = 8;
        getTabs().forEach((tab) -> {
            Insets insets;
            if (tab == mSourceTab) {
                insets = new Insets(0, size, size, size);
            } else {
                insets = new Insets(size);
            }

            FxHelper.setPadding(insets, (Region) tab.getContent());
        });

        mValidationSupport.validationResultProperty().addListener((ObservableValue<? extends ValidationResult> observable, ValidationResult oldValue, ValidationResult newValue) -> {
//            mOkButton.setDisable(mValidationSupport.isInvalid());
        });

        mValidationSupport.initInitialDecoration();
    }
}
