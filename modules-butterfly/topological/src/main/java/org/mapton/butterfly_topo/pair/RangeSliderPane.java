/*
 * Copyright 2024 Patrik Karlström.
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

import com.dlsc.gemsfx.util.SessionManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.controlsfx.control.RangeSlider;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class RangeSliderPane extends VBox {

    private final CheckBox mCheckBox = new CheckBox();
    private final DoubleProperty mMaxProperty = new SimpleDoubleProperty();
    private Spinner<Double> mMaxSpinner;
    private final double mMaxValue;
    private final DoubleProperty mMinProperty = new SimpleDoubleProperty();
    private Spinner<Double> mMinSpinner;
    private final BooleanProperty mSelectedProperty = new SimpleBooleanProperty();
    private RangeSlider mSlider;

    public RangeSliderPane(String title, double maxValue) {
        super(FxHelper.getUIScaled(8));
        mMaxValue = maxValue;
        mCheckBox.setText(title);

        createUI();
    }

    public void clear() {
        mCheckBox.setSelected(false);
        mSlider.setLowValue(0);
        mSlider.setHighValue(mMaxValue);
    }

    public DoubleProperty maxProperty() {
        return mMaxProperty;
    }

    public DoubleProperty minProperty() {
        return mMinProperty;
    }

    public BooleanProperty selectedProperty() {
        return mSelectedProperty;
    }

    void initSession(String prefix, SessionManager sessionManager) {
        var key = "filter.%s.".formatted(prefix);
        sessionManager.register(key + "enabled", mCheckBox.selectedProperty());
        sessionManager.register(key + "min", mSlider.lowValueProperty());
        sessionManager.register(key + "max", mSlider.highValueProperty());
    }

    private void createUI() {
        mSlider = new RangeSlider(0, mMaxValue, 0, mMaxValue);
        mSlider.setBlockIncrement(1.0);
        mSlider.setShowTickLabels(true);
        mSlider.setShowTickMarks(true);
        mMinSpinner = new Spinner<>(0, mMaxValue, 0, 0.1);
        mMaxSpinner = new Spinner<>(0, mMaxValue, mMaxValue, 0.1);

        var box = new HBox(mMinSpinner, mMaxSpinner);
        mSlider.disableProperty().bind(mCheckBox.selectedProperty().not());
        box.disableProperty().bind(mCheckBox.selectedProperty().not());
        getChildren().addAll(
                mCheckBox,
                mSlider,
                box
        );

        mSlider.lowValueProperty().addListener((p, o, n) -> {
            mMinSpinner.getValueFactory().setValue(n.doubleValue());
        });
        mMinSpinner.valueProperty().addListener((p, o, n) -> {
            mSlider.setLowValue(n);
        });

        mSlider.highValueProperty().addListener((p, o, n) -> {
            mMaxSpinner.getValueFactory().setValue(n.doubleValue());
        });
        mMaxSpinner.valueProperty().addListener((p, o, n) -> {
            mSlider.setHighValue(n);
        });

        FxHelper.setEditable(true, mMinSpinner, mMaxSpinner);
        FxHelper.autoCommitSpinners(mMinSpinner, mMaxSpinner);

        mSelectedProperty.bind(mCheckBox.selectedProperty());
        mMinProperty.bind(mSlider.lowValueProperty());
        mMaxProperty.bind(mSlider.highValueProperty());
    }

}
