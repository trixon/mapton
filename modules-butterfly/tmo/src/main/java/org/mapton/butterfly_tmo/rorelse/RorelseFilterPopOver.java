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
package org.mapton.butterfly_tmo.rorelse;

import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class RorelseFilterPopOver extends BaseFilterPopOver {

    private final RorelseFilter mFilter;
    private final SessionCheckComboBox<String> mFixpunktSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mInformationskallorSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mPlaceringSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mStatusSCCB = new SessionCheckComboBox<>();

    public RorelseFilterPopOver(RorelseFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mFixpunktSCCB.clearChecks();
        mPlaceringSCCB.clearChecks();
        mInformationskallorSCCB.clearChecks();
        mStatusSCCB.clearChecks();
    }

    @Override
    public void load(Butterfly butterfly) {
        var rorelse = butterfly.tmo().getRörelse();
        mFixpunktSCCB.loadAndRestoreCheckItems(rorelse.stream().map(o -> o.getFixpunkt()));
        mStatusSCCB.loadAndRestoreCheckItems(rorelse.stream().map(o -> o.getStatus()));
        mPlaceringSCCB.loadAndRestoreCheckItems(rorelse.stream().map(o -> o.getPlacering()));
        mInformationskallorSCCB.loadAndRestoreCheckItems(rorelse.stream().map(o -> o.getInformationskällor()));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mPlaceringSCCB,
                mFixpunktSCCB,
                mInformationskallorSCCB,
                mStatusSCCB
        );
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mPlaceringSCCB,
                mFixpunktSCCB,
                mInformationskallorSCCB,
                mStatusSCCB
        );

        mPlaceringSCCB.setTitle("Placering");
        mFixpunktSCCB.setTitle("Fixpunkt");
        mStatusSCCB.setTitle("Status");
        mInformationskallorSCCB.setTitle("Informationskallor");

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mPlaceringSCCB,
                mFixpunktSCCB,
                mInformationskallorSCCB,
                mStatusSCCB
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

        mFilter.mPlaceringCheckModel = mPlaceringSCCB.getCheckModel();
        mFilter.mFixpunktCheckModel = mFixpunktSCCB.getCheckModel();
        mFilter.mStatusCheckModel = mStatusSCCB.getCheckModel();
        mFilter.mInformationskallorCheckModel = mInformationskallorSCCB.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.rorelse.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.rorelse.checkedPlacering", mPlaceringSCCB.checkedStringProperty());
        sessionManager.register("filter.rorelse.checkedFixpunkt", mFixpunktSCCB.checkedStringProperty());
        sessionManager.register("filter.rorelse.checkedStatus", mStatusSCCB.checkedStringProperty());
        sessionManager.register("filter.rorelse.checkedInformationskallor", mInformationskallorSCCB.checkedStringProperty());
    }

}
