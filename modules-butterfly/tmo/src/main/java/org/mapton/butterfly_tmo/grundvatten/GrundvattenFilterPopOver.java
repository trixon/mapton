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
package org.mapton.butterfly_tmo.grundvatten;

import java.util.TreeSet;
import java.util.stream.Collectors;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import org.controlsfx.control.CheckComboBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.CheckModelSession;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenFilterPopOver extends BaseFilterPopOver {

    private final GrundvattenFilter mFilter;
    private final CheckComboBox<String> mGrundvattenmagasinCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mGrundvattenmagasinCheckModelSession = new CheckModelSession(mGrundvattenmagasinCheckComboBox);
    private final CheckComboBox<String> mStatusCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mStatusCheckModelSession = new CheckModelSession(mStatusCheckComboBox);

    public GrundvattenFilterPopOver(GrundvattenFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mGrundvattenmagasinCheckComboBox.getCheckModel().clearChecks();
        mStatusCheckComboBox.getCheckModel().clearChecks();
    }

    @Override
    public void load(Butterfly butterfly) {
        var grundvatten = butterfly.tmo().getGrundvatten();
//
        var magasinCheckModel = mGrundvattenmagasinCheckComboBox.getCheckModel();
        var checkedMagasin = magasinCheckModel.getCheckedItems();
        var allMagasin = new TreeSet<>(grundvatten.stream().map(o -> o.getGrundvattenmagasin()).collect(Collectors.toSet()));

        mGrundvattenmagasinCheckComboBox.getItems().setAll(allMagasin);
        checkedMagasin.stream().forEach(d -> magasinCheckModel.check(d));

        mGrundvattenmagasinCheckModelSession.load();
//
        var statusCheckModel = mStatusCheckComboBox.getCheckModel();
        var checkedStatus = statusCheckModel.getCheckedItems();
        var allStatus = new TreeSet<>(grundvatten.stream().map(o -> o.getStatus()).collect(Collectors.toSet()));

        mStatusCheckComboBox.getItems().setAll(allStatus);
        checkedStatus.stream().forEach(d -> statusCheckModel.check(d));

        mStatusCheckModelSession.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        var dropDownCount = 25;
        FxHelper.getComboBox(mGrundvattenmagasinCheckComboBox).setVisibleRowCount(dropDownCount);
        FxHelper.getComboBox(mStatusCheckComboBox).setVisibleRowCount(dropDownCount);
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        mGrundvattenmagasinCheckComboBox.setShowCheckedCount(true);
        mGrundvattenmagasinCheckComboBox.setTitle("Grundvattenmagasin");

        mStatusCheckComboBox.setShowCheckedCount(true);
        mStatusCheckComboBox.setTitle(Dict.STATUS.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusCheckComboBox,
                mGrundvattenmagasinCheckComboBox
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

        mFilter.mGrundvattenmagasinCheckModel = mGrundvattenmagasinCheckComboBox.getCheckModel();
        mFilter.mStatusCheckModel = mStatusCheckComboBox.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.grundvatten.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.grundvatten.checkedStatus", mStatusCheckModelSession.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedGrundvattenmagasin", mGrundvattenmagasinCheckModelSession.checkedStringProperty());
    }

}
