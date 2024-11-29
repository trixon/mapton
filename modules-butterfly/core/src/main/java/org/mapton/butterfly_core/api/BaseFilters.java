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
import java.util.List;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.tools.Borders;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BaseFilters {

    private final SessionCheckComboBox<String> mAlarmNameSccb = new SessionCheckComboBox<>();
    private Node mBaseBorderBox;
    private GridPane mBaseBox;
    private final double mBorderInnerPadding = FxHelper.getUIScaled(8.0);
    private final ResourceBundle mBundle = NbBundle.getBundle(getClass());
    private final SessionCheckComboBox<String> mCategorySccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<Integer> mFrequencySccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mMeasNextSccb = new SessionCheckComboBox<>(true);
    private final SessionCheckComboBox<String> mOperatorSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mOriginSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mStatusSccb = new SessionCheckComboBox<>();
    private final double mTopBorderInnerPadding = FxHelper.getUIScaled(16.0);

    public BaseFilters() {
        createUI();
    }

    public void clear() {
        SessionCheckComboBox.clearChecks(
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb,
                mOperatorSccb,
                mOriginSccb,
                mMeasNextSccb,
                mFrequencySccb
        );
    }

    public SessionCheckComboBox<String> getAlarmNameSccb() {
        mAlarmNameSccb.setDisable(false);
        return mAlarmNameSccb;
    }

    public Node getBaseBorderBox() {
        if (mBaseBorderBox == null) {
            mBaseBorderBox = Borders.wrap(mBaseBox)
                    .etchedBorder()
                    .title("Grunddata")
                    .innerPadding(mTopBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding, mBorderInnerPadding)
                    .outerPadding(0)
                    .raised()
                    .build()
                    .build();
        }
        return mBaseBorderBox;
    }

    public GridPane getBaseBox() {
        return mBaseBox;
    }

    public SessionCheckComboBox<String> getCategorySccb() {
        mCategorySccb.setDisable(false);
        return mCategorySccb;
    }

    public SessionCheckComboBox<Integer> getFrequencySccb() {
        mFrequencySccb.setDisable(false);
        return mFrequencySccb;
    }

    public SessionCheckComboBox<String> getGroupSccb() {
        mGroupSccb.setDisable(false);
        return mGroupSccb;
    }

    public SessionCheckComboBox<String> getMeasNextSccb() {
        mMeasNextSccb.setDisable(false);
        return mMeasNextSccb;
    }

    public SessionCheckComboBox<String> getOperatorSccb() {
        mOperatorSccb.setDisable(false);
        return mOperatorSccb;
    }

    public SessionCheckComboBox<String> getOriginSccb() {
        mOriginSccb.setDisable(false);
        return mOriginSccb;
    }

    public SessionCheckComboBox<String> getStatusSccb() {
        mStatusSccb.setDisable(false);
        return mStatusSccb;
    }

    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.checkedAlarmName", mAlarmNameSccb.checkedStringProperty());
        sessionManager.register("filter.checkedCategory", mCategorySccb.checkedStringProperty());
        sessionManager.register("filter.checkedFrequency", mFrequencySccb.checkedStringProperty());
        sessionManager.register("filter.checkedGroup", mGroupSccb.checkedStringProperty());
        sessionManager.register("filter.checkedOperators", mOperatorSccb.checkedStringProperty());
        sessionManager.register("filter.checkedOrigin", mOriginSccb.checkedStringProperty());
        sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());
        sessionManager.register("filter.measCheckedNextMeas", mMeasNextSccb.checkedStringProperty());
    }

    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb
        );
    }

    public void reset(PropertiesConfiguration filterConfig) {
        BaseFilterPopOver.splitAndCheck(filterConfig.getString("STATUS"), mStatusSccb.getCheckModel());
        BaseFilterPopOver.splitAndCheck(filterConfig.getString("GROUP"), mGroupSccb.getCheckModel());
        BaseFilterPopOver.splitAndCheck(filterConfig.getString("CATEGORY"), mCategorySccb.getCheckModel());
        BaseFilterPopOver.splitAndCheck(filterConfig.getString("OPERATOR"), mOperatorSccb.getCheckModel());
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mMeasNextSccb,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mAlarmNameSccb,
                mOperatorSccb,
                mOriginSccb,
                mFrequencySccb
        );

        mStatusSccb.setDisable(true);
        mGroupSccb.setDisable(true);
        mCategorySccb.setDisable(true);
        mAlarmNameSccb.setDisable(true);
        mOperatorSccb.setDisable(true);
        mOriginSccb.setDisable(true);
        mFrequencySccb.setDisable(true);
        mMeasNextSccb.setDisable(true);

        mMeasNextSccb.setTitle(mBundle.getString("nextMeasCheckComboBoxTitle"));
        mStatusSccb.setTitle(Dict.STATUS.toString());
        mGroupSccb.setTitle(Dict.GROUP.toString());
        mCategorySccb.setTitle(Dict.CATEGORY.toString());
        mAlarmNameSccb.setTitle(SDict.ALARMS.toString());
        mOperatorSccb.setTitle(SDict.OPERATOR.toString());
        mOriginSccb.setTitle(Dict.ORIGIN.toString());
        mFrequencySccb.setTitle(SDict.FREQUENCY.toString());

        mMeasNextSccb.getItems().setAll(List.of(
                "<0",
                "0",
                "1-6",
                "7-14",
                "15-28",
                "29-182",
                "∞"
        ));

        int rowGap = FxHelper.getUIScaled(12);
        mBaseBox = new GridPane(rowGap, rowGap);
        var leftBox = new VBox(rowGap,
                mStatusSccb,
                mFrequencySccb,
                mGroupSccb,
                mOriginSccb
        );

        var rightBox = new VBox(rowGap,
                mAlarmNameSccb,
                mMeasNextSccb,
                mCategorySccb,
                mOperatorSccb
        );

        int row = 1;
        mBaseBox.addRow(row++, leftBox, rightBox);

        FxHelper.autoSizeColumn(mBaseBox, 2);
        FxHelper.bindWidthForChildrens(leftBox, rightBox);
    }

}
