/*
 * Copyright 2023 Patrik Karlström.
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
package org.mapton.butterfly_topo;

import com.dlsc.gemsfx.Spacer;
import com.dlsc.gemsfx.util.SessionManager;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;
import org.mapton.api.ui.forms.MBaseFilterSection;
import org.mapton.api.ui.forms.MFilterSectionBasicProvider;
import org.mapton.api.ui.forms.MFilterSectionDate;
import org.mapton.api.ui.forms.MFilterSectionDisruptor;
import org.mapton.butterfly_core.api.BaseFilters;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseTabbedFilterPopOver {

    double vGap = FxHelper.getUIScaled(4.0);

    private final CheckBox mDimens1Checkbox = new CheckBox("1");
    private final CheckBox mDimens2Checkbox = new CheckBox("2");
    private final CheckBox mDimens3Checkbox = new CheckBox("3");
    private final TopoFilter mFilter;
    private final FilterSectionBasic mFilterSectionBasic;
    private final MFilterSectionDate mFilterSectionDate;
    private final MFilterSectionDisruptor mFilterSectionDisruptor;
    private final FilterSectionMeas mFilterSectionMeas;
    private final CheckBox mInvertCheckbox = new CheckBox();
    private final TopoManager mManager = TopoManager.getInstance();
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
    private final CheckBox mSameAlarmCheckbox = new CheckBox();

    public TopoFilterPopOver(TopoFilter filter) {
        mFilterSectionBasic = new FilterSectionBasic();
        mFilterSectionDate = new MFilterSectionDate();
        mFilterSectionDisruptor = new MFilterSectionDisruptor();
        mFilterSectionMeas = new FilterSectionMeas();
        mFilter = filter;
        setFilter(filter);
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()).node(getClass().getSimpleName()));

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        FxHelper.setSelected(false,
                mDimens1Checkbox,
                mDimens2Checkbox,
                mDimens3Checkbox,
                mSameAlarmCheckbox,
                mInvertCheckbox,
                mMeasIncludeWithoutCheckbox
        );

        mFilterSectionBasic.clear();
        mFilterSectionDate.clear();
        mFilterSectionDisruptor.clear();
        mFilterSectionMeas.clear();
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
        //mDateRangePane.reset();
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.topo().getControlPoints();

        mFilterSectionBasic.load(items);
        mFilterSectionDisruptor.load();
        mFilterSectionMeas.load(items);
        mFilterSectionDate.load(mManager.getTemporalRange());
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
//        FxHelper.setVisibleRowCount(25,
//                mMeasOperatorSccb,
//                mAlarmSccb
//        );
        mFilterSectionBasic.onShownFirstTime();
    }

    @Override
    public void reset() {
        clear();

        mFilter.freeTextProperty().set("*");
        mFilterSectionBasic.reset(TopoFilterDefaultsConfig.getInstance().getConfig());
    }

    private void createUI() {
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));

        var bottomBox = new VBox(vGap, new Separator(), mMeasIncludeWithoutCheckbox, mSameAlarmCheckbox);
        bottomBox.setPadding(FxHelper.getUIScaledInsets(8, 16, 8, 16));

        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        root.setBottom(bottomBox);
        getToolBar().getItems().add(new Separator());
        populateToolBar();

        getTabPane().getTabs().addAll(
                mFilterSectionBasic.getTab(),
                mFilterSectionDate.getTab(),
                mFilterSectionMeas.getTab(),
                mFilterSectionDisruptor.getTab()
        );

        setContentNode(root);

        var dimensButton = new Button("Alla dimensioner");
        dimensButton.setOnAction(actionEvent -> {
            List.of(mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox).forEach(cb -> cb.setSelected(false));
        });
        var dimensBox = new HBox(FxHelper.getUIScaled(8), mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox, new Spacer(), dimensButton);
        dimensBox.setAlignment(Pos.CENTER_LEFT);

        mFilterSectionBasic.getRoot().add(dimensBox, 0, 0, GridPane.REMAINING, 1);
    }

    private void initListeners() {
        activateCopyNames(actionEvent -> {
            var names = mManager.getTimeFilteredItems().stream().map(o -> o.getName()).toList();
            copyNames(names);
        });

        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
            mSameAlarmCheckbox.setSelected(true);
        });

        mFilter.invertProperty().bind(mInvertCheckbox.selectedProperty());

        mFilterSectionBasic.initListeners(mFilter);
        mFilterSectionDate.initListeners(mFilter);
        mFilterSectionDisruptor.initListeners(mFilter);
        mFilterSectionMeas.initListeners(mFilter);

        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());
        mFilter.dimens1Property().bind(mDimens1Checkbox.selectedProperty());
        mFilter.dimens2Property().bind(mDimens2Checkbox.selectedProperty());
        mFilter.dimens3Property().bind(mDimens3Checkbox.selectedProperty());

        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.sectionBasicProperty().bind(mFilterSectionBasic.selectedProperty());
        mFilter.sectionDateProperty().bind(mFilterSectionDate.selectedProperty());
        mFilter.sectionDisruptorProperty().bind(mFilterSectionDisruptor.selectedProperty());
        mFilter.sectionMeasProperty().bind(mFilterSectionMeas.selectedProperty());

        mFilter.initCheckModelListeners();
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionBasic.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionDisruptor.initSession(sessionManager);
        mFilterSectionMeas.initSession(sessionManager);

        sessionManager.register("filter.freeText", mFilter.freeTextProperty());

        sessionManager.register("filter.checkedDimension1", mDimens1Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension2", mDimens2Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension3", mDimens3Checkbox.selectedProperty());

        sessionManager.register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());

        sessionManager.register("filter.section.basic", mFilterSectionBasic.selectedProperty());
        sessionManager.register("filter.section.disruptor", mFilterSectionDisruptor.selectedProperty());
        sessionManager.register("filter.section.meas", mFilterSectionMeas.selectedProperty());

        sessionManager.register("filter.invert", mInvertCheckbox.selectedProperty());
        sessionManager.register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());

        return sessionManager;
    }

    private void populateToolBar() {
        var toolBar = getToolBar();
        addToToolBar("mc", ActionTextBehavior.SHOW);
        addToToolBar("mr", ActionTextBehavior.SHOW);
        addToToolBar("mm", ActionTextBehavior.SHOW);
        addToToolBar("mp", ActionTextBehavior.SHOW);
        toolBar.getItems().add(new Separator());
        addToToolBar("copyNames", ActionTextBehavior.HIDE);
        addToToolBar("paste", ActionTextBehavior.HIDE);

        mInvertCheckbox.setText(getBundle().getString("invertCheckBoxText"));
        var internalBox = new HBox(FxHelper.getUIScaled(8.0), mInvertCheckbox);
        internalBox.setPadding(FxHelper.getUIScaledInsets(0, 0, 0, 8.0));
        internalBox.setAlignment(Pos.CENTER_LEFT);
        toolBar.getItems().add(internalBox);
    }

    private class FilterSectionBasic extends MBaseFilterSection {

        private final BaseFilters mBaseFilters = new BaseFilters();

        public FilterSectionBasic() {
            super("Grunddata");
            init();
            setContent(mBaseFilters.getBaseBox());
        }

        public GridPane getRoot() {
            return mBaseFilters.getBaseBox();
        }

        @Override
        public void clear() {
            super.clear();
            mBaseFilters.clear();
        }

        public void initListeners(MFilterSectionBasicProvider filter) {
            filter.setStatusCheckModel(mBaseFilters.getStatusSccb().getCheckModel());
            filter.setGroupCheckModel(mBaseFilters.getGroupSccb().getCheckModel());
            filter.setCategoryCheckModel(mBaseFilters.getCategorySccb().getCheckModel());
            filter.setOperatorCheckModel(mBaseFilters.getOperatorSccb().getCheckModel());
            filter.setOriginCheckModel(mBaseFilters.getOriginSccb().getCheckModel());
            filter.setAlarmNameCheckModel(mBaseFilters.getAlarmNameSccb().getCheckModel());
            filter.setMeasNextCheckModel(mBaseFilters.getMeasNextSccb().getCheckModel());
            filter.setFrequencyCheckModel(mBaseFilters.getFrequencySccb().getCheckModel());
        }

        @Override
        public void initSession(SessionManager sessionManager) {
            mBaseFilters.initSession(sessionManager);
        }

        @Override
        public void onShownFirstTime() {
            mBaseFilters.onShownFirstTime();
        }

        @Override
        public void reset(PropertiesConfiguration filterConfig) {
            if (filterConfig != null) {
                mBaseFilters.reset(filterConfig);
            }
        }

        private void init() {
        }

        private void load(ArrayList<BTopoControlPoint> items) {
            var allAlarmNames = items.stream().map(o -> o.getAlarm1Id()).collect(Collectors.toCollection(HashSet::new));
            allAlarmNames.addAll(items.stream().map(o -> o.getAlarm2Id()).collect(Collectors.toSet()));
            mBaseFilters.getAlarmNameSccb().loadAndRestoreCheckItems(allAlarmNames.stream());
            mBaseFilters.getGroupSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getGroup()));
            mBaseFilters.getCategorySccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getCategory()));
            mBaseFilters.getOperatorSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOperator()));
            mBaseFilters.getOriginSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getOrigin()));
            mBaseFilters.getStatusSccb().loadAndRestoreCheckItems(items.stream().map(o -> o.getStatus()));
            mBaseFilters.getFrequencySccb().loadAndRestoreCheckItems(items.stream()
                    .filter(o -> o.getFrequency() != null)
                    .map(o -> o.getFrequency()));
            mBaseFilters.getMeasNextSccb().loadAndRestoreCheckItems();
        }

    }

}
