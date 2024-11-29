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
package org.mapton.butterfly_structural.crack;

import java.util.ResourceBundle;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import org.mapton.api.ui.forms.MFilterSectionDate;
import org.mapton.butterfly_core.api.BaseFilters;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class CrackFilterPopOver extends BaseTabbedFilterPopOver {

    private final BaseFilters mBaseFilters = new BaseFilters();
    private final ResourceBundle mBundle = NbBundle.getBundle(CrackFilterPopOver.class);
    private final CrackFilter mFilter;
    private CrackManager mManager = CrackManager.getInstance();
    private final MFilterSectionDate mFilterSectionDate;

    public CrackFilterPopOver(CrackFilter filter) {
        mFilter = filter;
        mFilterSectionDate = new MFilterSectionDate();
//        mFilter = filter;
        setFilter(filter);
        createUI();
        initListeners();
        initSession();

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        mFilterSectionDate.clear();
        mBaseFilters.clear();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.structural().getCrackPoints();
        mBaseFilters.getGroupSccb().loadAndRestoreCheckItems(items.stream().map(p -> p.getGroup()));
        mBaseFilters.getStatusSccb().loadAndRestoreCheckItems(items.stream().map(p -> p.getStatus()));
        mBaseFilters.getOperatorSccb().loadAndRestoreCheckItems(items.stream().map(p -> p.getOperator()));
        mBaseFilters.getOriginSccb().loadAndRestoreCheckItems(items.stream().map(p -> p.getOrigin()));
        mBaseFilters.getAlarmNameSccb().loadAndRestoreCheckItems(items.stream().map(p -> p.getAlarm1Id()));
        mBaseFilters.getFrequencySccb().loadAndRestoreCheckItems(items.stream()
                .filter(p -> p.getFrequency() != null)
                .map(p -> p.getFrequency()));

        mFilterSectionDate.load(mManager.getTemporalRange());
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        mBaseFilters.onShownFirstTime();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");

//        mBaseFilters.reset(TopoFilterDefaultsConfig.getInstance().getConfig());
    }

    private void createUI() {
//        var leftBox = new VBox(GAP,
//                mBaseFilters.getBaseBorderBox(),
//                new Spacer(),
//                mBaseFilters.getDateLastBorderBox()
//        );
//
//        var rightBox = new BorderPane();
//        var row = 0;
//        var gridPane = new GridPane(GAP, GAP);
//        gridPane.setPadding(FxHelper.getUIScaledInsets(GAP));
//
//        gridPane.addRow(row++, leftBox, rightBox);
        ////        gridPane.add(mMeasIncludeWithoutCheckbox, 0, row++, GridPane.REMAINING, 1);
////        gridPane.add(mSameAlarmCheckbox, 0, row++, GridPane.REMAINING, 1);
//        FxHelper.autoSizeColumn(gridPane, 2);
//
//        var root = new BorderPane(gridPane);
//        root.setTop(getToolBar());
//
//        FxHelper.bindWidthForChildrens(leftBox, mBaseFilters.getBaseBox());
//        FxHelper.bindWidthForRegions(leftBox);
//
//        int prefWidth = FxHelper.getUIScaled(250);
//        leftBox.setPrefWidth(prefWidth);
//        rightBox.setPrefWidth(prefWidth);
//
//        setContentNode(root);
        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
//        root.setBottom(bottomBox);
        getToolBar().getItems().add(new Separator());
        populateToolBar();

        getTabPane().getTabs().addAll(
                //                mFilterSectionBasic.getTab(),
                mFilterSectionDate.getTab()
        //                mFilterSectionMeas.getTab(),
        //                mFilterSectionDisruptor.getTab()
        );

        setContentNode(root);

    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mStatusCheckModel = mBaseFilters.getStatusSccb().getCheckModel();
        mFilter.mGroupCheckModel = mBaseFilters.getGroupSccb().getCheckModel();
        mFilter.mCategoryCheckModel = mBaseFilters.getCategorySccb().getCheckModel();
        mFilter.mOperatorCheckModel = mBaseFilters.getOperatorSccb().getCheckModel();
        mFilter.mOriginCheckModel = mBaseFilters.getOriginSccb().getCheckModel();
        mFilter.mAlarmNameCheckModel = mBaseFilters.getAlarmNameSccb().getCheckModel();
        mFilter.mFrequencyCheckModel = mBaseFilters.getFrequencySccb().getCheckModel();

        mFilterSectionDate.initListeners(mFilter);

        mFilter.initCheckModelListeners();
//                mFilter.sectionDateProperty().bind(mFilterSectionDate.selectedProperty());

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        mFilterSectionDate.initSession(sessionManager);

        sessionManager.register("filter.measPoint.freeText", mFilter.freeTextProperty());

        mBaseFilters.initSession(sessionManager);
    }

    private void populateToolBar() {
    }

}
