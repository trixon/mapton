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
import javafx.scene.control.CheckBox;
import javafx.scene.control.Spinner;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.mapton.api.ui.forms.NegPosStringConverterInteger;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.SDict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;
import se.trixon.almond.util.fx.session.SessionIntegerSpinner;

/**
 *
 * @author Patrik Karlström
 */
public class BSubFilterMeasAlarmLevel<T extends BXyzPoint> extends BSubFilterMeasBase {

    private Function<T, Integer> mAlarmAgeFunction;
    private Function<T, Integer> mAlarmLevelFunction;
    private final SessionCheckComboBox<BAlarmLevel> mAlarmSccb = new SessionCheckComboBox<>(true);
    private final int mDefaultAlarmLevelAgeValue = -7;
    private final CheckBox mMeasAlarmLevelAgeCheckbox = new CheckBox();
    private final SessionIntegerSpinner mMeasAlarmLevelAgeSis = new SessionIntegerSpinner(Integer.MIN_VALUE, Integer.MAX_VALUE, mDefaultAlarmLevelAgeValue);

    private final VBox mRoot = new VBox(GAP_V);
    private Node mRootBordered;

    public BSubFilterMeasAlarmLevel() {
        super(SDict.ALARM_LEVEL.toString());
        createUI();
    }

    @Override
    public void clear() {
        SessionCheckComboBox.clearChecks(
                mAlarmSccb
        );
        FxHelper.setSelected(false,
                mMeasAlarmLevelAgeCheckbox
        );
        mMeasAlarmLevelAgeSis.getValueFactory().setValue(mDefaultAlarmLevelAgeValue);

    }

    @Override
    public void createInfoContent(LinkedHashMap map) {
        map.put(getTitle(), "TODO");
    }

    @Override
    public boolean filter(BXyzPoint p) {
        return validateAlarmLevel((T) p)
                && validateMeasAlarmLevelAge((T) p);
    }

    public Function<T, Integer> getAlarmAgeFunction() {
        return mAlarmAgeFunction;
    }

    public Function<T, Integer> getAlarmLevelFunction() {
        return mAlarmLevelFunction;
    }

    public VBox getRoot() {
        return mRoot;
    }

    public Node getRootBordered() {
        if (mRootBordered == null) {
            mRootBordered = wrapInTitleBorder(getTitle(), mRoot);
        }

        return mRootBordered;
    }

    @Override
    public void initListeners(ChangeListener changeListener, ListChangeListener listChangeListener) {
        List.of(
                mMeasAlarmLevelAgeCheckbox.selectedProperty(),
                mMeasAlarmLevelAgeSis.valueProperty()
        ).forEach(o -> o.addListener(changeListener));

        List.of(
                mAlarmSccb.getCheckModel().getCheckedItems()
        ).forEach(o -> o.addListener(listChangeListener));
    }

    @Override
    public void initSession(SessionManager sessionManager) {
        sessionManager.register("filter.checkedNextAlarm", mAlarmSccb.checkedStringProperty());
        sessionManager.register("filter.measAlarmLevelAge", mMeasAlarmLevelAgeCheckbox.selectedProperty());
        sessionManager.register("filter.measAlarmLevelAgeValue", mMeasAlarmLevelAgeSis.sessionValueProperty());
    }

    @Override
    public void load(ArrayList items) {
        mAlarmSccb.loadAndRestoreCheckItems();
        mMeasAlarmLevelAgeSis.load();

        mMeasAlarmLevelAgeSis.disableProperty().bind(mMeasAlarmLevelAgeCheckbox.selectedProperty().not());
    }

    @Override
    public void onShownFirstTime() {
    }

    @Override
    public void reset(PropertiesConfiguration filterConfig) {
    }

    public void setAlarmAgeFunction(Function<T, Integer> alarmAgeFunction) {
        this.mAlarmAgeFunction = alarmAgeFunction;
    }

    public void setAlarmLevelFunction(Function<T, Integer> alarmLevelFunction) {
        this.mAlarmLevelFunction = alarmLevelFunction;
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mAlarmSccb
        );
        mAlarmSccb.setTitle(getTitle());
        mAlarmSccb.getItems().setAll(BAlarmLevel.values());
        mMeasAlarmLevelAgeCheckbox.setText("Ålder på larmnivå");
        mMeasAlarmLevelAgeSis.getValueFactory().setConverter(new NegPosStringConverterInteger());

        mRoot.getChildren().addAll(
                mAlarmSccb,
                new VBox(titleGap, mMeasAlarmLevelAgeCheckbox, mMeasAlarmLevelAgeSis)
        );
//        , alcGridPane);

        var spinners = new Spinner[]{mMeasAlarmLevelAgeSis};

        FxHelper.setEditable(true, spinners);
        FxHelper.autoCommitSpinners(spinners);
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

    private boolean validateMeasAlarmLevelAge(T p) {
        if (!mMeasAlarmLevelAgeCheckbox.isSelected()) {
            return true;
        }

        var value = mAlarmAgeFunction.apply(p);
        if (value == null) {
            return true;
        }

        var lim = mMeasAlarmLevelAgeSis.getValue();

        value = Math.abs(value);

        if (lim == 0) {
            return value == 0;
        } else if (lim < 0) {
            return value <= Math.abs(lim) && value != 0;
        } else if (lim > 0) {
            return value >= lim;
        }

        return true;
    }

}
