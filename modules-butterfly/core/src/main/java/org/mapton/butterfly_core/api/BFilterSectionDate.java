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
package org.mapton.butterfly_core.api;

import com.dlsc.gemsfx.util.SessionManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.tools.Borders;
import org.mapton.api.MDateFormula;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import static org.mapton.butterfly_core.api.BFilterSectionDate.DateElement.*;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.core.api.ui.MFilterPresetPopOver;
import se.trixon.almond.util.DateHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BFilterSectionDate extends MBaseFilterSection {

    public static final String KEY_DATE_FIRST_FROM_START = "filter.date.FirstFromStart";
    public static final String KEY_DATE_FIRST_HIGH = "filter.date.FirstHigh";
    public static final String KEY_DATE_FIRST_LOW = "filter.date.FirstLow";
    public static final String KEY_DATE_FIRST_TO_END = "filter.date.FirstToEnd";
    public static final String KEY_DATE_LAST_FROM_START = "filter.date.LastFromStart";
    public static final String KEY_DATE_LAST_HIGH = "filter.date.LastHigh";
    public static final String KEY_DATE_LAST_LOW = "filter.date.LastLow";
    public static final String KEY_DATE_LAST_TO_END = "filter.date.LastToEnd";
    private static final String KEY_DATE_FORMULA_FIRST = "filter.date.formula.first";
    private static final String KEY_DATE_FORMULA_LAST = "filter.date.formula.last";
    private Node mDateFirstBorderBox;
    private boolean mDateFirstFromStart;
    private boolean mDateFirstToEnd;
    private String mDateFormulaFirst;
    private String mDateFormulaLast;
    private Node mDateLastBorderBox;
    private boolean mDateLastFromStart;
    private boolean mDateLastToEnd;
    private final DateRangePane mDateRangeFirstPane = new DateRangePane();
    private final DateRangePane mDateRangeLastPane = new DateRangePane();
    private final SessionCheckComboBox<String> mHasDateFromToSccb = new SessionCheckComboBox<>(true);
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public BFilterSectionDate() {
        super(Dict.DATE.toString());
        createUI();
        setContent(mRoot);
    }

    @Override
    public void clear() {
        super.clear();
        mDateRangeFirstPane.reset();
        mDateRangeLastPane.reset();
        mDateFormulaFirst = "";
        mDateFormulaLast = "";
        mDateFirstFromStart = true;
        mDateFirstToEnd = true;
        mDateLastFromStart = true;
        mDateLastToEnd = true;
        SessionCheckComboBox.clearChecks(
                mHasDateFromToSccb
        );
    }

    @Override
    public void createInfoContent(LinkedHashMap<String, String> map) {
        if (!isSelected()) {
            return;
        }
        map.put(Dict.DATE.toUpper(), ".");
        map.put(SDict.VALID_FROM_TO.toString(), makeInfo(getDateFromToCheckModel().getCheckedItems()));
        map.put("Första " + Dict.FROM.toString(), dateFirstLowProperty().get() != null ? dateFirstLowProperty().get().toString() : "");
        map.put("Första " + Dict.TO.toString(), dateFirstHighProperty().get() != null ? dateFirstHighProperty().get().toString() : "");
        map.put("Senaste " + Dict.FROM.toString(), dateLastLowProperty().get() != null ? dateLastLowProperty().get().toString() : "");
        map.put("Senaste " + Dict.TO.toString(), dateLastHighProperty().get() != null ? dateLastHighProperty().get().toString() : "");
    }

    public void disable(DateElement... elements) {
        var map = new HashMap<DateElement, Node>();
        map.put(FIRST, mDateFirstBorderBox);
        map.put(LAST, mDateLastBorderBox);
        map.put(HAS_FROM_TO, mHasDateFromToSccb);

        for (var element : elements) {
            map.get(element).setDisable(true);
        }
    }

    public boolean filter(BXyzPoint p, LocalDateTime dateFirst) {
        if (isSelected()) {
            var valid = validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo())
                    && validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo())
                    && validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo());

            if (valid && mDateFirstBorderBox.isDisabled() == false) {
                valid = validateAge(dateFirst, dateFirstLowProperty(), dateFirstHighProperty());
            }

            if (valid && mDateLastBorderBox.isDisabled() == false) {
                valid = validateAge(p.getDateLatest(), dateLastLowProperty(), dateLastHighProperty());
            }

            return valid;
        } else {
            return true;
        }
    }

    public Node getDateFirstBorderBox() {
        if (mDateFirstBorderBox == null) {
            mDateFirstBorderBox = Borders.wrap(mDateRangeFirstPane.getRoot()).etchedBorder().title("Period för första mätning").innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding).outerPadding(0).raised().build().build();
        }
        return mDateFirstBorderBox;
    }

    public IndexedCheckModel getDateFromToCheckModel() {
        return mHasDateFromToSccb.getCheckModel();
    }

    public Node getDateLastBorderBox() {
        if (mDateLastBorderBox == null) {
            mDateLastBorderBox = Borders.wrap(mDateRangeLastPane.getRoot()).etchedBorder().title("Period för senaste mätning").innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding).outerPadding(0).raised().build().build();
        }
        return mDateLastBorderBox;
    }

    public SessionCheckComboBox<String> getHasDateFromToSccb() {
        return mHasDateFromToSccb;
    }

    public void initListeners(ChangeListener changeListenerObject, ListChangeListener<Object> listChangeListener) {
        List.of(
                selectedProperty(),
                //
                dateFirstHighProperty(),
                dateFirstLowProperty(),
                dateLastHighProperty(),
                dateLastLowProperty()
        ).forEach(propertyBase -> propertyBase.addListener(changeListenerObject));

        List.of(
                getDateFromToCheckModel()
        ).forEach(cm -> cm.getCheckedItems().addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        setSessionManager(sessionManager);
        sessionManager.register("filter.section.date", selectedProperty());
        var preferences = sessionManager.getPreferences();
        String dateFormulaFirst = null;
        String dateFormulaLast = null;
        var dateFirstFromStart = true;
        var dateFirstToEnd = true;
        var dateLastFromStart = true;
        var dateLastToEnd = true;
        var isPreset = StringUtils.containsIgnoreCase(preferences.absolutePath(), MFilterPresetPopOver.FILTER_PRESET_NODE);

        if (isPreset) {
            mDateFirstToEnd = preferences.getBoolean(KEY_DATE_FIRST_TO_END, true);
            mDateFirstFromStart = preferences.getBoolean(KEY_DATE_FIRST_FROM_START, true);
            mDateLastToEnd = preferences.getBoolean(KEY_DATE_LAST_TO_END, true);
            mDateLastFromStart = preferences.getBoolean(KEY_DATE_LAST_FROM_START, true);
            dateFirstFromStart = mDateFirstFromStart;
            dateFirstToEnd = mDateFirstToEnd;
            dateLastFromStart = mDateLastFromStart;
            dateLastToEnd = mDateLastToEnd;

            mDateFormulaFirst = preferences.get(KEY_DATE_FORMULA_FIRST, "");
            mDateFormulaLast = preferences.get(KEY_DATE_FORMULA_LAST, "");
            dateFormulaFirst = mDateFormulaFirst;
            dateFormulaLast = mDateFormulaLast;
        } else {
            mDateFirstFromStart = true;
            mDateFirstToEnd = true;
            mDateLastFromStart = true;
            mDateLastToEnd = true;
            mDateFormulaFirst = mDateRangeFirstPane.dateFormulaProperty().get();
            mDateFormulaLast = mDateRangeLastPane.dateFormulaProperty().get();
        }

        sessionManager.register("filter.checkedDateFromTo", mHasDateFromToSccb.checkedStringProperty());

        sessionManager.register(KEY_DATE_FORMULA_FIRST, mDateRangeFirstPane.dateFormulaProperty());
        sessionManager.register(KEY_DATE_FIRST_HIGH, mDateRangeFirstPane.highStringProperty());
        sessionManager.register(KEY_DATE_FIRST_LOW, mDateRangeFirstPane.lowStringProperty());
        sessionManager.register(KEY_DATE_FIRST_FROM_START, mDateRangeFirstPane.selectedFromStartProperty());
        sessionManager.register(KEY_DATE_FIRST_TO_END, mDateRangeFirstPane.selectedToEndProperty());

        sessionManager.register(KEY_DATE_FORMULA_LAST, mDateRangeLastPane.dateFormulaProperty());
        sessionManager.register(KEY_DATE_LAST_HIGH, mDateRangeLastPane.highStringProperty());
        sessionManager.register(KEY_DATE_LAST_LOW, mDateRangeLastPane.lowStringProperty());
        sessionManager.register(KEY_DATE_LAST_FROM_START, mDateRangeLastPane.selectedFromStartProperty());
        sessionManager.register(KEY_DATE_LAST_TO_END, mDateRangeLastPane.selectedToEndProperty());

        if (isPreset) {
            mDateFirstFromStart = dateFirstFromStart;
            mDateFirstToEnd = dateFirstToEnd;
            mDateLastFromStart = dateLastFromStart;
            mDateLastToEnd = dateLastToEnd;

            mDateFormulaFirst = dateFormulaFirst;
            mDateFormulaLast = dateFormulaLast;
        }

        if (StringUtils.isNotBlank(mDateFormulaFirst)) {
            mDateFirstFromStart = false;
            mDateFirstToEnd = false;
        }
        if (StringUtils.isNotBlank(mDateFormulaLast)) {
            mDateLastFromStart = false;
            mDateLastToEnd = false;
        }

        restoreCustomDates();
    }

    public void load(MTemporalRange temporalRange) {
        var sessionManager = getSessionManager();
        List.of(
                mDateRangeFirstPane.selectedFromStartProperty(),
                mDateRangeFirstPane.selectedToEndProperty(),
                mDateRangeFirstPane.lowStringProperty(),
                mDateRangeFirstPane.highStringProperty(),
                mDateRangeLastPane.lowStringProperty(),
                mDateRangeLastPane.highStringProperty(),
                mDateRangeLastPane.selectedFromStartProperty(),
                mDateRangeLastPane.selectedToEndProperty()
        ).forEach(property -> sessionManager.unregister(property));

        if (temporalRange != null) {
            mDateRangeFirstPane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
            mDateRangeLastPane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }
        sessionManager.register(KEY_DATE_FIRST_LOW, mDateRangeFirstPane.lowStringProperty());
        sessionManager.register(KEY_DATE_FIRST_HIGH, mDateRangeFirstPane.highStringProperty());
        sessionManager.register(KEY_DATE_FIRST_FROM_START, mDateRangeFirstPane.selectedFromStartProperty());
        sessionManager.register(KEY_DATE_FIRST_TO_END, mDateRangeFirstPane.selectedToEndProperty());

        sessionManager.register(KEY_DATE_LAST_LOW, mDateRangeLastPane.lowStringProperty());
        sessionManager.register(KEY_DATE_LAST_HIGH, mDateRangeLastPane.highStringProperty());
        sessionManager.register(KEY_DATE_LAST_FROM_START, mDateRangeLastPane.selectedFromStartProperty());
        sessionManager.register(KEY_DATE_LAST_TO_END, mDateRangeLastPane.selectedToEndProperty());

        var p = getSessionManager().getPreferences();
        mDateFirstFromStart = p.getBoolean(KEY_DATE_FIRST_FROM_START, true);
        mDateFirstToEnd = p.getBoolean(KEY_DATE_FIRST_TO_END, true);
        mDateLastFromStart = p.getBoolean(KEY_DATE_LAST_FROM_START, true);
        mDateLastToEnd = p.getBoolean(KEY_DATE_LAST_TO_END, true);
        mDateFormulaFirst = p.get(KEY_DATE_FORMULA_FIRST, "");
        mDateFormulaLast = p.get(KEY_DATE_FORMULA_LAST, "");
        initSession(sessionManager);
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mHasDateFromToSccb
        );
        mHasDateFromToSccb.setTitle(SDict.VALID_FROM_TO.toString());
        mHasDateFromToSccb.getItems().setAll(List.of(
                SDict.HAS_VALID_FROM.toString(),
                SDict.HAS_VALID_TO.toString(),
                SDict.WITHOUT_VALID_FROM.toString(),
                SDict.WITHOUT_VALID_TO.toString(),
                SDict.IS_VALID.toString(),
                SDict.IS_INVALID.toString()
        ));

        mRoot.setMaxWidth(getMaxWidth());
        int row = 0;
        mRoot.addRow(row++, getDateFirstBorderBox(), getDateLastBorderBox());
        mRoot.addRow(row++, getHasDateFromToSccb());
        FxHelper.autoSizeColumn(mRoot, 2);
    }

    private SimpleObjectProperty<LocalDate> dateFirstHighProperty() {
        return mDateRangeFirstPane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> dateFirstLowProperty() {
        return mDateRangeFirstPane.lowDateProperty();
    }

    private SimpleObjectProperty<LocalDate> dateLastHighProperty() {
        return mDateRangeLastPane.highDateProperty();
    }

    private SimpleObjectProperty<LocalDate> dateLastLowProperty() {
        return mDateRangeLastPane.lowDateProperty();
    }

    private void restoreCustomDates() {
        if (StringUtils.isNotBlank(mDateFormulaFirst)) {
            var datePreset = new MDateFormula(mDateFormulaFirst);
            mDateRangeFirstPane.getDatePane().getDateRangeSlider().setLowHighDate(datePreset.getStartDate(), datePreset.getEndDate());
            mDateRangeFirstPane.dateFormulaProperty().set(mDateFormulaFirst);
        }

        if (StringUtils.isNotBlank(mDateFormulaLast)) {
            var datePreset = new MDateFormula(mDateFormulaLast);
            mDateRangeLastPane.getDatePane().getDateRangeSlider().setLowHighDate(datePreset.getStartDate(), datePreset.getEndDate());
            mDateRangeLastPane.dateFormulaProperty().set(mDateFormulaLast);
        }

        if (mDateFirstFromStart) {
            var dateRangeSlider = mDateRangeFirstPane.getDatePane().getDateRangeSlider();
            dateRangeSlider.setLowDate(dateRangeSlider.getMinDate());
        }

        if (mDateFirstToEnd) {
            var dateRangeSlider = mDateRangeFirstPane.getDatePane().getDateRangeSlider();
            dateRangeSlider.setHighDate(dateRangeSlider.getMaxDate());
        }

        if (mDateLastFromStart) {
            var dateRangeSlider = mDateRangeLastPane.getDatePane().getDateRangeSlider();
            dateRangeSlider.setLowDate(dateRangeSlider.getMinDate());
        }

        if (mDateLastToEnd) {
            var dateRangeSlider = mDateRangeLastPane.getDatePane().getDateRangeSlider();
            dateRangeSlider.setHighDate(dateRangeSlider.getMaxDate());
        }
    }

    private boolean validateAge(LocalDateTime dateTime, SimpleObjectProperty<LocalDate> low, SimpleObjectProperty<LocalDate> high) {
        if (null != dateTime) {
            var lowDate = low.get();
            var highDate = high.get();
            var valid = DateHelper.isBetween(lowDate, highDate, dateTime.toLocalDate());

            return valid;
        } else {
            return false;
        }
    }

    private boolean validateDateFromToHas(LocalDate fromDate, LocalDate toDate) {
        var validFromChecked = getDateFromToCheckModel().isChecked(SDict.HAS_VALID_FROM.toString());
        var validToChecked = getDateFromToCheckModel().isChecked(SDict.HAS_VALID_TO.toString());
        var valid = (!validFromChecked && !validToChecked)
                || (fromDate != null && validFromChecked)
                || (toDate != null && validToChecked);

        return valid;
    }

    private boolean validateDateFromToIs(LocalDate fromDate, LocalDate toDate) {
        var now = LocalDate.now();
        var validChecked = getDateFromToCheckModel().isChecked(SDict.IS_VALID.toString());
        var invalidChecked = getDateFromToCheckModel().isChecked(SDict.IS_INVALID.toString());

        if (validChecked && invalidChecked) {
            return false;
        } else if (!validChecked && !invalidChecked) {
            return true;
        }

        if (validChecked) {
            var validFromDate = fromDate == null ? false : DateHelper.isAfterOrEqual(now, fromDate);
            var validToDate = toDate == null ? false : DateHelper.isBeforeOrEqual(now, toDate);
            return validFromDate || validToDate;
        } else {//invalidChecked
            var invalidFromDate = fromDate == null ? true : DateHelper.isAfterOrEqual(now, fromDate);
            var invalidToDate = toDate == null ? true : DateHelper.isBeforeOrEqual(now, toDate);
            return !invalidFromDate || !invalidToDate;
        }
    }

    private boolean validateDateFromToWithout(LocalDate fromDate, LocalDate toDate) {
        var validFromChecked = getDateFromToCheckModel().isChecked(SDict.WITHOUT_VALID_FROM.toString());
        var validToChecked = getDateFromToCheckModel().isChecked(SDict.WITHOUT_VALID_TO.toString());
        var valid = (!validFromChecked && !validToChecked)
                || (fromDate == null && validFromChecked)
                || (toDate == null && validToChecked);

        return valid;
    }

    public enum DateElement {
        FIRST,
        LAST,
        HAS_FROM_TO;
    }
}
