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
package org.mapton.butterfly_topo.xdev;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.ArrayUtils;
import org.mapton.api.ui.forms.MBaseFilterSection;
import static org.mapton.api.ui.forms.MBaseFilterSection.wrapInTitleBorder;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BSubFilterMeasAlarmLevel<T extends BXyzPoint> extends BSubFilterMeasBase {

    private Function<T, Integer> mAlarmLevelFunction;
    private final SessionCheckComboBox<BAlarmLevel> mAlarmSccb = new SessionCheckComboBox<>(true);

    private final VBox mRoot = new VBox(MBaseFilterSection.GAP_V);
    private Node mRootBordered;

    public BSubFilterMeasAlarmLevel() {
        createUI();
    }

    @Override
    public void clear() {
        SessionCheckComboBox.clearChecks(
                mAlarmSccb
        );

    }

    @Override
    public void createInfoContent(LinkedHashMap map) {
        map.put(SDict.ALARM_LEVEL.toString(), "TODO");
    }

    @Override
    public boolean filter(BXyzPoint p) {
        return validateAlarmLevel((T) p);
    }

    public Function<T, Integer> getAlarmLevelFunction() {
        return mAlarmLevelFunction;
    }

    public VBox getRoot() {
        return mRoot;
    }

    public Node getRootBordered() {
        if (mRootBordered == null) {
            mRootBordered = wrapInTitleBorder(SDict.ALARM_LEVEL.toString(), mRoot);
        }

        return mRootBordered;
    }

    @Override
    public void initListeners(ChangeListener changeListener, ListChangeListener listChangeListener) {
        List.of(
                mAlarmSccb.getCheckModel().getCheckedItems()
        ).forEach(o -> o.addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
    }

    @Override
    public void load(ArrayList items) {
        mAlarmSccb.loadAndRestoreCheckItems();
    }

    @Override
    public void onShownFirstTime() {
    }

    public void setAlarmLevelFunction(Function<T, Integer> alarmLevelFunction) {
        this.mAlarmLevelFunction = alarmLevelFunction;
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mAlarmSccb
        );
        mAlarmSccb.setTitle(SDict.ALARM_LEVEL.toString());
        mAlarmSccb.getItems().setAll(BAlarmLevel.values());

        mRoot.getChildren().addAll(
                mAlarmSccb
        );
//        var alarmBox = new VBox(GAP_V, mAlarmSccb, new VBox(titleGap, mMeasAlarmLevelAgeCheckbox, mMeasAlarmLevelAgeSis), alcGridPane);
    }

    private boolean validateAlarmLevel(T p) {
        var alarmLevelCheckModel = mAlarmSccb.getCheckModel();
        if (alarmLevelCheckModel.isEmpty()) {
            return true;
        }

        var level = mAlarmLevelFunction.apply(p);
        var selectedLevels = alarmLevelCheckModel.getCheckedItems().stream().mapToInt(a -> a.getLevel()).toArray();

        return ArrayUtils.contains(selectedLevels, level);
    }

}
