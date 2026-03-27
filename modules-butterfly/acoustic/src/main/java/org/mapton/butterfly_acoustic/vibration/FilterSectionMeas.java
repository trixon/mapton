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
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ObjectUtils;
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

    public static final int DEFAULT_RELATIVE_COUNT = 1;
    public static final int DEFAULT_RELATIVE_VALUE = 100;
    public static final int DEFAULT_SPEED_COUNT = 10;
    public static final int DEFAULT_SPEED_VALUE = 10;
    private final MeasFilterUI mMeasFilterUI;
    private final SliderPane mRelativeCountSliderPane = new SliderPane("Min. antal i perioden", 1, 50, true, false, 1d);
    private final CheckBox mRelativePeriodCheckbox = new CheckBox();
    private final DateRangePane mRelativePeriodDateRangePane = new DateRangePane();
    private final SliderPane mRelativeValueSliderPane = new SliderPane("Min. förbrukning i procent", 0, 150, true, false, 1d);
    private final GridPane mRoot = new GridPane(GAP_H, GAP_V * 4);
    private final SliderPane mSpeedCountSliderPane = new SliderPane("Min. antal i perioden", 1, 50, true, false, 1d);
    private final CheckBox mSpeedPeriodCheckbox = new CheckBox();
    private final DateRangePane mSpeedPeriodDateRangePane = new DateRangePane();
    private final SliderPane mSpeedValueSliderPane = new SliderPane("Min. svängningshastighet (mm/s)", 1, 50, true, false, 1d);

    public FilterSectionMeas() {
        super(SDict.MEASUREMENTS.toString());
        mMeasFilterUI = new MeasFilterUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mSpeedValueSliderPane.setValue(DEFAULT_SPEED_VALUE);
        mSpeedCountSliderPane.setValue(DEFAULT_SPEED_COUNT);
        mSpeedPeriodDateRangePane.reset();

        mRelativeValueSliderPane.setValue(DEFAULT_RELATIVE_VALUE);
        mRelativeCountSliderPane.setValue(DEFAULT_RELATIVE_COUNT);
        mRelativePeriodDateRangePane.reset();

        FxHelper.setSelected(false,
                mSpeedPeriodCheckbox,
                mRelativePeriodCheckbox
        );
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }

        map.put(SDict.MEASUREMENTS.toString(), ".");
        map.put("H Period " + Dict.FROM.toString(), speedPeriodDateLowProperty().get() != null ? speedPeriodDateLowProperty().get().toString() : "");
        map.put("H Period " + Dict.TO.toString(), speedPeriodDateHighProperty().get() != null ? speedPeriodDateHighProperty().get().toString() : "");
        map.put("R Period " + Dict.FROM.toString(), relativePeriodDateLowProperty().get() != null ? relativePeriodDateLowProperty().get().toString() : "");
        map.put("R Period " + Dict.TO.toString(), relativePeriodDateHighProperty().get() != null ? relativePeriodDateHighProperty().get().toString() : "");
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
        sessionManager.register(getKeyFilter("relativePeriod"), mRelativePeriodCheckbox.selectedProperty());
        sessionManager.register(getKeyFilter("relative.Value"), mRelativeValueSliderPane.valueProperty());
        sessionManager.register(getKeyFilter("relative.Count"), mRelativeCountSliderPane.valueProperty());
        sessionManager.register(getKeyFilter("relative.DateLow"), mRelativePeriodDateRangePane.lowStringProperty());
        sessionManager.register(getKeyFilter("relative.DateHigh"), mRelativePeriodDateRangePane.highStringProperty());
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
            return validateSpeedPeriodChanges(p)
                    && validateRelativePeriodChanges(p)
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
                mSpeedCountSliderPane.valueProperty(),
                //
                mRelativePeriodCheckbox.selectedProperty(),
                relativePeriodDateHighProperty(),
                relativePeriodDateLowProperty(),
                mRelativeValueSliderPane.valueProperty(),
                mRelativeCountSliderPane.valueProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));
    }

    void load(ArrayList<BAcousticVibrationPoint> items, MTemporalRange temporalRange) {
        if (temporalRange != null) {
            mSpeedPeriodDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
            mRelativePeriodDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }

        var sessionManager = getSessionManager();
        sessionManager.register(getKeyFilter("dateSpeedPeriodLow"), mSpeedPeriodDateRangePane.lowStringProperty());
        sessionManager.register(getKeyFilter("dateSpeedPeriodHigh"), mSpeedPeriodDateRangePane.highStringProperty());
        sessionManager.register(getKeyFilter("dateRelativePeriodLow"), mRelativePeriodDateRangePane.lowStringProperty());
        sessionManager.register(getKeyFilter("dateRelativePeriodHigh"), mRelativePeriodDateRangePane.highStringProperty());
    }

    private SimpleObjectProperty<LocalDate> relativePeriodDateHighProperty() {
        return mRelativePeriodDateRangePane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> relativePeriodDateLowProperty() {
        return mRelativePeriodDateRangePane.lowDateProperty();
    }

    private SimpleObjectProperty<LocalDate> speedPeriodDateHighProperty() {
        return mSpeedPeriodDateRangePane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> speedPeriodDateLowProperty() {
        return mSpeedPeriodDateRangePane.lowDateProperty();
    }

    private boolean validateRelativePeriodChanges(BAcousticVibrationPoint p) {
        if (!mRelativePeriodCheckbox.isSelected()) {
            return true;
        }

        return mRelativeCountSliderPane.getValue() <= p.ext().getObservationsTimeFiltered().stream()
                .filter(o -> DateHelper.isBetween(speedPeriodDateLowProperty().get(), speedPeriodDateHighProperty().get(), o.getDate().toLocalDate()))
                .filter(o -> ObjectUtils.allNotNull(o.getLimit(), o.getMeasuredZ()))
                .filter(o -> (o.getMeasuredZ() / o.getLimit()) >= mRelativeValueSliderPane.getValue() / 100.0)
                .count();
    }

    private boolean validateSpeedPeriodChanges(BAcousticVibrationPoint p) {
        if (!mSpeedPeriodCheckbox.isSelected()) {
            return true;
        }
        return mSpeedCountSliderPane.getValue() <= p.ext().getObservationsTimeFiltered().stream()
                .filter(o -> DateHelper.isBetween(speedPeriodDateLowProperty().get(), speedPeriodDateHighProperty().get(), o.getDate().toLocalDate()))
                .filter(o -> o.getMeasuredZ() >= mSpeedValueSliderPane.getValue())
                .count();
    }

    public class MeasFilterUI {

        public MeasFilterUI() {
            createUI();
        }

        public void onShownFirstTime() {
        }

        private void createUI() {
            mSpeedPeriodCheckbox.setText("Svängningshastighetsförändring");
            mRelativePeriodCheckbox.setText("Relativ riktvärdesförbruktning");
            double borderInnerPadding = FxHelper.getUIScaled(8.0);
            double topBorderInnerPadding = FxHelper.getUIScaled(16.0);

            var wrappedSpeedDateBox = Borders.wrap(mSpeedPeriodDateRangePane.getRoot())
                    .etchedBorder()
                    .title("Tidsperiod")
                    .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();

            var wrappedRelativeDateBox = Borders.wrap(mRelativePeriodDateRangePane.getRoot())
                    .etchedBorder()
                    .title("Tidsperiod")
                    .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();

            var absoluteBox = new VBox(rowGap,
                    mSpeedPeriodCheckbox,
                    mSpeedValueSliderPane,
                    mSpeedCountSliderPane,
                    wrappedSpeedDateBox
            );

            var leftBox = new VBox(rowGap,
                    absoluteBox
            );

            var relativeBox = new VBox(rowGap,
                    mRelativePeriodCheckbox,
                    mRelativeValueSliderPane,
                    mRelativeCountSliderPane,
                    wrappedRelativeDateBox
            );

            var rightBox = new VBox(rowGap,
                    relativeBox
            );

            var row = 0;
            mRoot.addRow(row++,
                    wrapInTitleBorder("Absolut", leftBox),
                    wrapInTitleBorder("Relativ", rightBox)
            );

            BindingHelper.bindWidthForChildrens(leftBox, rightBox);

            mSpeedCountSliderPane.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());
            mSpeedValueSliderPane.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());
            wrappedSpeedDateBox.disableProperty().bind(mSpeedPeriodCheckbox.selectedProperty().not());

            mRelativeCountSliderPane.disableProperty().bind(mRelativePeriodCheckbox.selectedProperty().not());
            mRelativeValueSliderPane.disableProperty().bind(mRelativePeriodCheckbox.selectedProperty().not());
            wrappedRelativeDateBox.disableProperty().bind(mRelativePeriodCheckbox.selectedProperty().not());

            FxHelper.autoSizeColumn(mRoot, 2);
            mRoot.setMaxWidth(getMaxWidth());
        }
    }
}
