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
package org.mapton.butterfly_activities;

import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class ActFilterPopOver extends BaseFilterPopOver {

    private final ActFilter mFilter;
    private final SessionCheckComboBox<String> mStatusSCCB = new SessionCheckComboBox<>();

    public ActFilterPopOver(ActFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mStatusSCCB.getCheckModel().clearChecks();
    }

    @Override
    public void load(Butterfly butterfly) {
        var statuses = butterfly.getAreaActivities().stream()
                .map(aa -> ActHelper.getStatusAsString(aa.getStatus()));

        mStatusSCCB.loadAndRestoreCheckItems(statuses);
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        mStatusSCCB.getCheckModel().clearChecks();
    }

    private void createUI() {
        mStatusSCCB.setShowCheckedCount(true);
        mStatusSCCB.setTitle(Dict.STATUS.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusSCCB
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

        mFilter.mStatusCheckModel = mStatusSCCB.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        getSessionManager().register("freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.checkedGroup", mStatusSCCB.checkedStringProperty());

    }

}
