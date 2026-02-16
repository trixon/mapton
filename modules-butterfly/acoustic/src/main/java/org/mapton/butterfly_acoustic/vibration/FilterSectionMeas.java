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
package org.mapton.butterfly_acoustic.vibration;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.tools.Borders;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.acoustic.BAcousticVibrationPoint;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.BindingHelper;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.control.SliderPane;

/**
 *
 * @author Patrik Karlström
 */
public class FilterSectionMeas extends MBaseFilterSection {

    public static final int DEFAULT_COUNT = 10;
    public static final int DEFAULT_SPEED = 10;
    private final MeasFilterUI mMeasFilterUI;
    private final SliderPane mSpeedCountSliderPane = new SliderPane("Min. antal i perioden", 1, 50, true, false, 1d);
    private final CheckBox mSpeedPeriodCheckbox = new CheckBox();
    private final DateRangePane mSpeedPeriodDateRangePane = new DateRangePane();
    private final SliderPane mSpeedValueSliderPane = new SliderPane("Min. svängningshastighet (mm/s)", 1, 50, true, false, 1d);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mMeasFilterUI = new MeasFilterUI();
        setContent(mMeasFilterUI.getRoot());
    }

    @Override
    public void clear() {
        super.clear();
        mSpeedValueSliderPane.valueProperty().setValue(DEFAULT_SPEED);
        mSpeedCountSliderPane.valueProperty().setValue(DEFAULT_COUNT);
        mSpeedPeriodDateRangePane.reset();
        FxHelper.setSelected(false, mSpeedPeriodCheckbox);
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toString(), ".");
        map.put("Period " + Dict.FROM.toString(), speedPeriodDateLowProperty().get() != null ? speedPeriodDateLowProperty().get().toString() : "");
        map.put("Period " + Dict.TO.toString(), speedPeriodDateHighProperty().get() != null ? speedPeriodDateHighProperty().get().toString() : "");
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register(getKeyFilter("section"), selectedProperty());
        sessionManager.register(getKeyFilter("speedPeriod"), mSpeedPeriodCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("speed.Value"), mSpeedValueSliderPane.valueProperty());
        sessionManager.register(getKeyFilter("speed.Count"), mSpeedCountSliderPane.valueProperty());
        sessionManager.register(getKeyFilter("speed.DateLow"), mSpeedPeriodDateRangePane.lowStringProperty());
        sessionManager.register(getKeyFilter("speed.DateHigh"), mSpeedPeriodDateRangePane.highStringProperty());
    }

    @Override
    public void onShownFirstTime() {
        mMeasFilterUI.onShownFirstTime();
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    boolean filter(BAcousticVibrationPoint p) {
        if (isSelected()) {
            return validatePeriodChanges(p)
                    && true;
        } else {
            return true;
        }
    }

    void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                //
                mSpeedPeriodCheckbox.selectedProperty(),
                speedPeriodDateHighProperty(),
                speedPeriodDateLowProperty(),
                mSpeedValueSliderPane.valueProperty(),
                mSpeedCountSliderPane.valueProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load(ArrayList<BAcousticVibrationPoint> items, MTemporalRange temporalRange) {
        if (temporalRange != null) {
            mSpeedPeriodDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }

        var sessionManager = getSessionManager();
        sessionManager.register(getKeyFilter("dateSpeedPeriodLow"), mSpeedPeriodDateRangePane.lowStringProperty());
        sessionManager.register(getKeyFilter("dateSpeedPeriodHigh"), mSpeedPeriodDateRangePane.highStringProperty());
    }

    private SimpleObjectProperty<LocalDate> speedPeriodDateHighProperty() {
        return mSpeedPeriodDateRangePane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> speedPeriodDateLowProperty() {
        return mSpeedPeriodDateRangePane.lowDateProperty();
    }

    private boolean validatePeriodChanges(BAcousticVibrationPoint p) {
        if (!mSpeedPeriodCheckbox.isSelected()) {
            return true;
        }
        return mSpeedCountSliderPane.valueProperty().get() <= p.ext().getObservationsTimeFiltered().stream()
                .filter(o -> DateHelper.isBetween(speedPeriodDateLowProperty().get(), speedPeriodDateHighProperty().get(), o.getDate().toLocalDate()))
                .filter(o -> o.getMeasuredZ() >= mSpeedValueSliderPane.valueProperty().get())
                .count();
    }

    public class MeasFilterUI {

        private GridPane mRoot;

        public MeasFilterUI() {
            createUI();
        }

        public GridPane getRoot() {
            return mRoot;
        }

        public void onShownFirstTime() {
        }

        private void createUI() {
            mSpeedPeriodCheckbox.setText("Svängningshastighetsförändring");
            double borderInnerPadding = FxHelper.getUIScaled(8.0);
            double topBorderInnerPadding = FxHelper.getUIScaled(16.0);

            var wrappedDateBox = Borders.wrap(mSpeedPeriodDateRangePane.getRoot())
                    .etchedBorder()
                    .title("Tidsperiod")
                    .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();

            var leftBox = new VBox(rowGap,
                    mSpeedPeriodCheckbox,
                    mSpeedValueSliderPane,
                    mSpeedCountSliderPane,
                    wrappedDateBox
            );

            var rightBox = new VBox(rowGap,
                    new Label("")
            );

            var row = 1;
            mRoot = new GridPane();
            mRoot.addRow(row++, leftBox, rightBox);

            FxHelper.autoSizeColumn(mRoot, 2);
            BindingHelper.bindWidthForChildrens(leftBox, rightBox);

            mSpeedCountSliderPane.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());
            mSpeedValueSliderPane.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());
            wrappedDateBox.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());
        }
    }
}
