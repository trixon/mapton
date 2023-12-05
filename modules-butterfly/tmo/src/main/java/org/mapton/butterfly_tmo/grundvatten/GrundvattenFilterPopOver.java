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
    private final CheckComboBox<String> mRörtypCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mRörtypCheckModelSession = new CheckModelSession(mRörtypCheckComboBox);
    private final CheckComboBox<String> mFiltertypCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mFiltertypCheckModelSession = new CheckModelSession(mFiltertypCheckComboBox);

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
        mFiltertypCheckComboBox.getCheckModel().clearChecks();
        mRörtypCheckComboBox.getCheckModel().clearChecks();
        mStatusCheckComboBox.getCheckModel().clearChecks();
    }

    @Override
    public void load(Butterfly butterfly) {
        var grundvatten = butterfly.tmo().getGrundvatten();

        FxHelper.loadAndRestoreCheckItems(mGrundvattenmagasinCheckComboBox, mGrundvattenmagasinCheckModelSession, grundvatten.stream().map(o -> o.getGrundvattenmagasin()));
        FxHelper.loadAndRestoreCheckItems(mFiltertypCheckComboBox, mFiltertypCheckModelSession, grundvatten.stream().map(o -> o.getFiltertyp()));
        FxHelper.loadAndRestoreCheckItems(mRörtypCheckComboBox, mRörtypCheckModelSession, grundvatten.stream().map(o -> o.getRörtyp()));
        FxHelper.loadAndRestoreCheckItems(mStatusCheckComboBox, mStatusCheckModelSession, grundvatten.stream().map(o -> o.getStatus()));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mGrundvattenmagasinCheckComboBox,
                mFiltertypCheckComboBox,
                mRörtypCheckComboBox,
                mStatusCheckComboBox
        );
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mGrundvattenmagasinCheckComboBox,
                mFiltertypCheckComboBox,
                mRörtypCheckComboBox,
                mStatusCheckComboBox
        );

        mGrundvattenmagasinCheckComboBox.setTitle("Grundvattenmagasin");
        mFiltertypCheckComboBox.setTitle("Filtertyp");
        mRörtypCheckComboBox.setTitle("Rörtyp");
        mStatusCheckComboBox.setTitle(Dict.STATUS.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusCheckComboBox,
                mGrundvattenmagasinCheckComboBox,
                mRörtypCheckComboBox,
                mFiltertypCheckComboBox
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

        mFilter.mGrundvattenmagasinCheckModel = mGrundvattenmagasinCheckComboBox.getCheckModel();
        mFilter.mFiltertypCheckModel = mFiltertypCheckComboBox.getCheckModel();
        mFilter.mRörtypCheckModel = mRörtypCheckComboBox.getCheckModel();
        mFilter.mStatusCheckModel = mStatusCheckComboBox.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.grundvatten.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.grundvatten.checkedGrundvattenmagasin", mGrundvattenmagasinCheckModelSession.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedFiltertyp", mFiltertypCheckModelSession.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedRörrtyp", mRörtypCheckModelSession.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedStatus", mStatusCheckModelSession.checkedStringProperty());
    }

}
