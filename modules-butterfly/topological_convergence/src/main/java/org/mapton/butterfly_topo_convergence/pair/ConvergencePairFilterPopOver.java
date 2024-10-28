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
package org.mapton.butterfly_topo_convergence.pair;

import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergencePairFilterPopOver extends BaseFilterPopOver {

    private final ConvergencePairFilter mFilter;
    private final SessionCheckComboBox<String> mGroupSCCB = new SessionCheckComboBox<>();

    public ConvergencePairFilterPopOver(ConvergencePairFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");
        mGroupSCCB.getCheckModel().clearChecks();

    }

    @Override
    public void load(Butterfly butterfly) {
        var groups = butterfly.noise().getMeasuringPoints().stream().map(b -> b.getGroup());
        mGroupSCCB.loadAndRestoreCheckItems(groups);
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        var dropDownCount = 25;
        FxHelper.getComboBox(mGroupSCCB).setVisibleRowCount(dropDownCount);
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        mGroupSCCB.getCheckModel().clearChecks();
    }

    private void createUI() {
        mGroupSCCB.setShowCheckedCount(true);
        mGroupSCCB.setTitle(Dict.GROUP.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mGroupSCCB
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.setCheckModelGroup(mGroupSCCB.getCheckModel());

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.convergence.pair.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.convergence.pair.checkedGroup", mGroupSCCB.checkedStringProperty());
    }

}
