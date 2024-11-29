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
package org.mapton.api.ui.forms;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import org.controlsfx.tools.Borders;
import org.mapton.api.MTemporalRange;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class MFilterSectionDate extends MBaseFilterSection {

    private Node mDateFirstBorderBox;
    private Node mDateLastBorderBox;
    private final DateRangePane mDateRangeFirstPane = new DateRangePane();
    private final DateRangePane mDateRangeLastPane = new DateRangePane();
    private final SessionCheckComboBox<String> mHasDateFromToSccb = new SessionCheckComboBox<>(true);
    private final GridPane mRoot = new GridPane(columnGap, rowGap);

    public MFilterSectionDate() {
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

    public Node getDateFirstBorderBox() {
        if (mDateFirstBorderBox == null) {
            mDateFirstBorderBox = Borders.wrap(mDateRangeFirstPane.getRoot()).etchedBorder().title("Period för första mätning").innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding).outerPadding(0).raised().build().build();
        }
        return mDateFirstBorderBox;
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

    public void initListeners(MFilterSectionDateProvider filter) {
        filter.measDateFirstLowProperty().bind(getDateRangeFirstPane().lowDateProperty());
        filter.measDateFirstHighProperty().bind(getDateRangeFirstPane().highDateProperty());
        filter.measDateLastLowProperty().bind(getDateRangeLastPane().lowDateProperty());
        filter.measDateLastHighProperty().bind(getDateRangeLastPane().highDateProperty());
        filter.setDateFromToCheckModel(getHasDateFromToSccb().getCheckModel());
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
    public void reset() {
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

}
