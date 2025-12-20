/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_remote.insar;

import com.dlsc.gemsfx.util.SessionManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.remote.BRemoteInsarPoint;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;
import se.trixon.almond.util.fx.session.SessionComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    private final ArgBox mAccelerationArgBox = new ArgBox("acceleration");
    private final SliderPane mAccelerationSliderPane = new SliderPane("Acceleration (mm/år^2)", 25, true, true, 1d);
    private List<ArgBox> mArgBoxes;
    private final ArgBox mDisplacementArgBox = new ArgBox("displacement");
    private final SliderPane mDisplacementSliderPane = new SliderPane("Rörelse (mm)", 50, true, true, 1d);
    private final List<Function<BRemoteInsarPoint, Double>> mFunctions;
    private final MeasFilterUI mMeasFilterUI;
    private List<SliderPane> mSliderPanes;
    private final ArgBox mVelocity3ArgBox = new ArgBox("3velocity");
    private final SliderPane mVelocity3SliderPane = new SliderPane("Hastighet 3m (mm/år)", 50, true, true, 1d);
    private final ArgBox mVelocity6ArgBox = new ArgBox("6velocity");
    private final SliderPane mVelocity6SliderPane = new SliderPane("Hastighet 6m (mm/år)", 50, true, true, 1d);
    private final ArgBox mVelocityArgBox = new ArgBox("0velocity");
    private final SliderPane mVelocitySliderPane = new SliderPane("Hastighet (mm/år)", 50, true, true, 1d);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mSliderPanes = List.of(mDisplacementSliderPane, mVelocitySliderPane, mVelocity3SliderPane, mVelocity6SliderPane, mAccelerationSliderPane);
        mArgBoxes = List.of(mDisplacementArgBox, mVelocityArgBox, mVelocity3ArgBox, mVelocity6ArgBox, mAccelerationArgBox);
        final Function<BRemoteInsarPoint, Double> displacement = (var o) -> {
            Double deltaZ = o.ext().deltaZero().getDeltaZ();
            if (deltaZ == null) {
                return null;
            } else {
                return deltaZ * 1000;
            }
        };
        final Function<BRemoteInsarPoint, Double> velocity = (var o) -> o.getVelocity();
        final Function<BRemoteInsarPoint, Double> velocity3 = (var o) -> o.getVelocity3m();
        final Function<BRemoteInsarPoint, Double> velocity6 = (var o) -> o.getVelocity6m();
        final Function<BRemoteInsarPoint, Double> acceleration = (var o) -> o.getAcceleration();

        mFunctions = List.of(displacement, velocity, velocity3, velocity6, acceleration);
        mMeasFilterUI = new MeasFilterUI();
        setContent(mMeasFilterUI.getRoot());
    }

    @Override
    public void clear() {
        super.clear();
        mSliderPanes.forEach(o -> o.clear());
        mArgBoxes.forEach(o -> o.reset());
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toString(), ".");
//        map.put("Period " + Dict.FROM.toString(), levelPeriodDateLowProperty().get() != null ? levelPeriodDateLowProperty().get().toString() : "");
//        map.put("Period " + Dict.TO.toString(), levelPeriodDateHighProperty().get() != null ? levelPeriodDateHighProperty().get().toString() : "");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        mDisplacementSliderPane.initSession(getKeyFilter("displacementValue"), sessionManager);
        mVelocitySliderPane.initSession(getKeyFilter("velocityValue"), sessionManager);
        mVelocity3SliderPane.initSession(getKeyFilter("velocity3Value"), sessionManager);
        mVelocity6SliderPane.initSession(getKeyFilter("velocity6Value"), sessionManager);
        mAccelerationSliderPane.initSession(getKeyFilter("accelerationValue"), sessionManager);

        mDisplacementArgBox.initSession(sessionManager);
        mVelocityArgBox.initSession(sessionManager);
        mVelocity3ArgBox.initSession(sessionManager);
        mVelocity6ArgBox.initSession(sessionManager);
        mAccelerationArgBox.initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
        mSliderPanes.forEach(o -> o.setSelected(false));
    }

    boolean filter(BRemoteInsarPoint p) {
        if (isSelected()) {
            return validate(p);
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        var properties = new ArrayList<ReadOnlyProperty<? extends Serializable>>();
        properties.add(selectedProperty());
        for (int i = 0; i < mSliderPanes.size(); i++) {
            var pane = mSliderPanes.get(i);
            var box = mArgBoxes.get(i);
            properties.add(pane.selectedProperty());
            properties.add(pane.valueProperty());
            properties.add(box.mDirectionScb.getSelectionModel().selectedItemProperty());
            properties.add(box.mGtLtScb.getSelectionModel().selectedItemProperty());
        }
        properties.forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load(ArrayList<BRemoteInsarPoint> items, MTemporalRange temporalRange) {
    }

    private boolean validate(BRemoteInsarPoint p) {
        for (int i = 0; i < mSliderPanes.size(); i++) {
            var result = validate(mSliderPanes.get(i), mArgBoxes.get(i), mFunctions.get(i).apply(p));
            if (!result) {
                return false;
            }
        }
        return true;
    }

    private boolean validate(SliderPane sliderPane, ArgBox argBox, Double value) {
        if (!sliderPane.isSelected()) {
            return true;
        } else {
            if (value == null) {
                return false;
            }
            var min = 0d;
            var max = sliderPane.valueProperty().get();
            switch (argBox.mDirectionScb.getValue()) {
                case EITHER:
                    value = Math.abs(value);
                    break;
                case POS:
                    if (value < 0) {
                        return false;
                    }
                    break;
                case NEG:
                    if (value > 0) {
                        return false;
                    }
                    min = -max;
                    max = 0;
                    break;
                default:
                    throw new AssertionError();
            }

            var gt = argBox.mGtLtScb.getSelectionModel().getSelectedIndex() == 0;
            var valid = inRange(value, min, max);
            if (gt) {
                valid = !valid;
            }

            return valid;
        }
    }

    public class MeasFilterUI {

        private GridPane mRoot;

        public MeasFilterUI() {
            createUI();
        }

        public GridPane getRoot() {
            return mRoot;
        }

        private void createUI() {
            mRoot = new GridPane();
            int row = 0;
            for (int i = 0; i < mSliderPanes.size(); i++) {
                var pane = mSliderPanes.get(i);
                var box = mArgBoxes.get(i);
                mRoot.addRow(row++, pane, box);
                box.disableProperty().bind(pane.selectedProperty().not());
            }

            FxHelper.autoSizeColumn(mRoot, 2);
            GridPane.setValignment(mVelocityArgBox, VPos.CENTER);
        }

    }

    private enum Direction {
        EITHER("±"),
        POS("+"),
        NEG("-");
        private final String mTitle;

        private Direction(String title) {
            mTitle = title;
        }

        @Override
        public String toString() {
            return mTitle;
        }

    }

    private class ArgBox extends HBox {

        private final SessionComboBox<Direction> mDirectionScb = new SessionComboBox<>();
        private final SessionComboBox<String> mGtLtScb = new SessionComboBox<>();
        private final String mTag;

        public ArgBox(String tag) {
            super(FxHelper.getUIScaled(8d));
            mTag = tag;
            createUI();
        }

        private void createUI() {
            setAlignment(Pos.BOTTOM_LEFT);
            setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8));
            mDirectionScb.getItems().setAll(Direction.values());
            mGtLtScb.getItems().setAll(">=", "<=");
            mGtLtScb.getSelectionModel().selectFirst();
            mDirectionScb.getSelectionModel().selectFirst();
            getChildren().setAll(mDirectionScb, mGtLtScb);
        }

        private void initSession(SessionManager sessionManager) {
            sessionManager.register(getKeyFilter(mTag + "Direction"), mDirectionScb.selectedIndexProperty());
            sessionManager.register(getKeyFilter(mTag + "GtLt"), mGtLtScb.selectedIndexProperty());
        }

        private void reset() {
            mGtLtScb.getSelectionModel().selectFirst();
            mDirectionScb.getSelectionModel().selectFirst();
        }

    }
}
