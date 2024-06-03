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
package org.mapton.butterfly_acoustic.blast;

import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import org.controlsfx.tools.Borders;
import static org.mapton.api.ui.MPopOver.GAP;
import org.mapton.api.ui.forms.DateRangePane;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class BlastFilterPopOver extends BaseFilterPopOver {

    private final DateRangePane mDateRangePane = new DateRangePane();
    private final BlastFilter mFilter;
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final BlastManager mManager = BlastManager.getInstance();

    public BlastFilterPopOver(BlastFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");
        SessionCheckComboBox.clearChecks(
                mGroupSccb
        );
        mDateRangePane.reset();
    }

    @Override
    public void load(Butterfly butterfly) {
        var groups = butterfly.acoustic().getBlasts().stream().map(b -> b.getGroup());
        mGroupSccb.loadAndRestoreCheckItems(groups);

        var temporalRange = mManager.getTemporalRange();
        if (temporalRange != null) {
            mDateRangePane.setMinMaxDate(temporalRange.getFromLocalDate(), temporalRange.getToLocalDate());

            var sessionManager = getSessionManager();
            sessionManager.register("filter.DateLow", mDateRangePane.lowStringProperty());
            sessionManager.register("filter.DateHigh", mDateRangePane.highStringProperty());
        }
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        var dropDownCount = 25;
        FxHelper.getComboBox(mGroupSccb).setVisibleRowCount(dropDownCount);
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        SessionCheckComboBox.clearChecks(
                mGroupSccb
        );
    }

    private void createUI() {
        mGroupSccb.setShowCheckedCount(true);
        mGroupSccb.setTitle(Dict.GROUP.toString());

        double borderInnerPadding = FxHelper.getUIScaled(8.0);
        double topBorderInnerPadding = FxHelper.getUIScaled(16.0);
        var wrappedDateBox = Borders.wrap(mDateRangePane.getRoot())
                .etchedBorder()
                .title("Period för sprängning")
                .innerPadding(topBorderInnerPadding, borderInnerPadding, borderInnerPadding, borderInnerPadding)
                .outerPadding(0)
                .raised()
                .build()
                .build();

        var vBox = new VBox(GAP,
                mGroupSccb,
                wrappedDateBox
        );

        FxHelper.bindWidthForChildrens(vBox);
        int prefWidth = FxHelper.getUIScaled(250);
        vBox.setPrefWidth(prefWidth);

        var root = new BorderPane(vBox);
        root.setTop(getToolBar());
        setContentNode(root);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mGroupCheckModel = mGroupSccb.getCheckModel();
        mFilter.dateLowProperty().bind(mDateRangePane.lowDateProperty());
        mFilter.dateHighProperty().bind(mDateRangePane.highDateProperty());

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.blast.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.blast.checkedGroup", mGroupSccb.checkedStringProperty());
    }

}
