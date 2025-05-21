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
import java.util.List;
import java.util.prefs.Preferences;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDisruptor;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.FilterSectionMisc;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class TopoFilterPopOver extends BaseTabbedFilterPopOver {

    private final CheckBox m1dCloseToAutoCheckbox = new CheckBox("Dubbar Auto 10");
    private final CheckBox mDimens1Checkbox = new CheckBox("1");
    private final CheckBox mDimens2Checkbox = new CheckBox("2");
    private final CheckBox mDimens3Checkbox = new CheckBox("3");
    private final TopoFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final BFilterSectionDisruptor mFilterSectionDisruptor;
    private final FilterSectionMeas mFilterSectionMeas;
    private final FilterSectionMisc mFilterSectionMisc;
    private final BFilterSectionPoint mFilterSectionPoint;
    private final TopoManager mManager = TopoManager.getInstance();
    private final CheckBox mMeasIncludeWithoutCheckbox = new CheckBox();
    private final CheckBox mSameAlarmCheckbox = new CheckBox();

    public TopoFilterPopOver(TopoFilter filter) {
        mFilterSectionPoint = new BFilterSectionPoint();
        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionDisruptor = new BFilterSectionDisruptor();
        mFilterSectionMeas = new FilterSectionMeas();
        mFilterSectionMisc = new FilterSectionMisc();

        mFilter = filter;
        mFilter.setFilterSection(mFilterSectionPoint);
        mFilter.setFilterSection(mFilterSectionDate);
        mFilter.setFilterSection(mFilterSectionDisruptor);
        mFilter.setFilterSection(mFilterSectionMeas);

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
                m1dCloseToAutoCheckbox,
                mSameAlarmCheckbox,
                mMeasIncludeWithoutCheckbox
        );

        mFilterSectionPoint.clear();
        mFilterSectionDate.clear();
        mFilterSectionDisruptor.clear();
        mFilterSectionMeas.clear();
        mFilterSectionMisc.clear();
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

        mFilterSectionPoint.load(items);
        mFilterSectionDisruptor.load();
        mFilterSectionMeas.load(items);
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        mFilterSectionPoint.onShownFirstTime();
    }

    @Override
    public void reset() {
        clear();

        mFilter.freeTextProperty().set("*");
        mFilterSectionPoint.reset(TopoFilterDefaultsConfig.getInstance().getConfig());
        mFilterSectionMisc.reset(null);
    }

    private void createUI() {
        mMeasIncludeWithoutCheckbox.setText(getBundle().getString("measIncludeWithoutCheckboxText"));
        mSameAlarmCheckbox.setText(getBundle().getString("sameAlarmCheckBoxText"));

        var bottomBox = new VBox(FxHelper.getUIScaled(4.0), new Separator(), mMeasIncludeWithoutCheckbox, mSameAlarmCheckbox);
        bottomBox.setPadding(FxHelper.getUIScaledInsets(8, 16, 8, 16));

        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        root.setBottom(bottomBox);
        populateToolBar(mFilterSectionMisc.getInvertCheckboxToolBarItem());

        getTabPane().getTabs().addAll(
                mFilterSectionPoint.getTab(),
                mFilterSectionDate.getTab(),
                mFilterSectionMeas.getTab(),
                mFilterSectionDisruptor.getTab()
        );

        setContentNode(root);

        var dimensButton = new Button("Alla dimensioner");
        dimensButton.setOnAction(actionEvent -> {
            List.of(mDimens1Checkbox, mDimens2Checkbox, mDimens3Checkbox).forEach(cb -> cb.setSelected(false));
        });
        var dimensBox = new HBox(FxHelper.getUIScaled(8),
                mDimens1Checkbox,
                mDimens2Checkbox,
                mDimens3Checkbox,
                dimensButton,
                new Spacer(),
                m1dCloseToAutoCheckbox);
        dimensBox.setAlignment(Pos.CENTER_LEFT);

        mFilterSectionPoint.getRoot().add(dimensBox, 0, 0, GridPane.REMAINING, 1);
    }

    private void initListeners() {
        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
            mSameAlarmCheckbox.setSelected(true);
        });

        mFilterSectionMeas.initListeners(mFilter);
        mFilterSectionMisc.initListeners(mFilter);

        mFilter.measIncludeWithoutProperty().bind(mMeasIncludeWithoutCheckbox.selectedProperty());
        mFilter.dimens1Property().bind(mDimens1Checkbox.selectedProperty());
        mFilter.dimens2Property().bind(mDimens2Checkbox.selectedProperty());
        mFilter.dimens3Property().bind(mDimens3Checkbox.selectedProperty());
        mFilter.closeToAutoProperty().bind(m1dCloseToAutoCheckbox.selectedProperty());

        mFilter.sameAlarmProperty().bind(mSameAlarmCheckbox.selectedProperty());

        mFilter.sectionMeasProperty().bind(mFilterSectionMeas.selectedProperty());

        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.initCheckModelListeners();
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionDisruptor.initSession(sessionManager);
        mFilterSectionMeas.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        sessionManager.register("filter.freeText", mFilter.freeTextProperty());

        sessionManager.register("filter.checkedDimension1", mDimens1Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension2", mDimens2Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension3", mDimens3Checkbox.selectedProperty());
        sessionManager.register("filter.checkedDimension1dCloseToAuto", m1dCloseToAutoCheckbox.selectedProperty());

        sessionManager.register("filter.measIncludeWithout", mMeasIncludeWithoutCheckbox.selectedProperty());

        sessionManager.register("filter.sameAlarm", mSameAlarmCheckbox.selectedProperty());

        return sessionManager;
    }

}
