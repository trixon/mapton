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
package org.mapton.butterfly_topo.grade;

import com.dlsc.gemsfx.util.SessionManager;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class SliderPane extends VBox {

    private final CheckBox mCheckBox = new CheckBox();
    private final double mMaxValue;
    private final BooleanProperty mSelectedProperty = new SimpleBooleanProperty();
    private Slider mSlider;
    private Spinner<Double> mSpinner;
    private final DoubleProperty mValueProperty = new SimpleDoubleProperty();

    public SliderPane(String title, double maxValue) {
        super(FxHelper.getUIScaled(8));
        mMaxValue = maxValue;
        mCheckBox.setText(title);

        createUI();
    }

    public void clear() {
        mCheckBox.setSelected(false);
        mSlider.setValue(0);
    }

    public BooleanProperty selectedProperty() {
        return mSelectedProperty;
    }

    public DoubleProperty valueProperty() {
        return mValueProperty;
    }

    void initSession(String prefix, SessionManager sessionManager) {
        var key = "filter.%s.".formatted(prefix);
        sessionManager.register(key + "enabled", mCheckBox.selectedProperty());
        sessionManager.register(key + "value", mSlider.valueProperty());
    }

    private void createUI() {
        mSlider = new Slider(0, mMaxValue, 0.1);
        mSlider.setBlockIncrement(1.0);
        mSlider.setShowTickLabels(true);
        mSlider.setShowTickMarks(true);
        mSpinner = new Spinner<>(0, mMaxValue, 0, 0.1);

        var box = new HBox(mSpinner);
        mSlider.disableProperty().bind(mCheckBox.selectedProperty().not());
        box.disableProperty().bind(mCheckBox.selectedProperty().not());
        getChildren().addAll(
                mCheckBox,
                mSlider,
                box
        );

        mSlider.valueProperty().addListener((p, o, n) -> {
            mSpinner.getValueFactory().setValue(n.doubleValue());
        });
        mSpinner.valueProperty().addListener((p, o, n) -> {
            mSlider.setValue(n);
        });

        FxHelper.setEditable(true, mSpinner);
        FxHelper.autoCommitSpinners(mSpinner);

        mSelectedProperty.bind(mCheckBox.selectedProperty());
        mValueProperty.bind(mSlider.valueProperty());
    }

}
