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
import java.util.LinkedHashMap;
import java.util.List;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.control.IndexedCheckModel;
import org.controlsfx.tools.Borders;
import org.mapton.api.MTemporalRange;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.butterfly_format.types.BXyzPoint;
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

    private Node mDateFirstBorderBox;
    private Node mDateLastBorderBox;
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

    public boolean filter(BXyzPoint p, LocalDateTime dateFirst) {
        if (isSelected()) {
            return validateDateFromToHas(p.getDateValidFrom(), p.getDateValidTo())
                    && validateDateFromToWithout(p.getDateValidFrom(), p.getDateValidTo())
                    && validateDateFromToIs(p.getDateValidFrom(), p.getDateValidTo())
                    && validateAge(dateFirst, dateFirstLowProperty(), dateFirstHighProperty())
                    && validateAge(p.getDateLatest(), dateLastLowProperty(), dateLastHighProperty())
                    && true;
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

    public DateRangePane getDateRangeFirstPane() {
        return mDateRangeFirstPane;
    }

    public DateRangePane getDateRangeLastPane() {
        return mDateRangeLastPane;
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
        sessionManager.register("filter.checkedDateFromTo", mHasDateFromToSccb.checkedStringProperty());
    }

    public void load(MTemporalRange temporalRange) {
        if (temporalRange != null) {
            getDateRangeFirstPane().setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
            getDateRangeLastPane().setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());
        }
        var sessionManager = getSessionManager();
        sessionManager.register("filter.DateFirstLow", getDateRangeFirstPane().lowStringProperty());
        sessionManager.register("filter.DateFirstHigh", getDateRangeFirstPane().highStringProperty());
        sessionManager.register("filter.DateLastLow", getDateRangeLastPane().lowStringProperty());
        sessionManager.register("filter.DateLastHigh", getDateRangeLastPane().highStringProperty());
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

}
