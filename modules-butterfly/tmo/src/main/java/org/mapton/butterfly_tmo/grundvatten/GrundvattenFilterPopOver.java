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

import com.dlsc.gemsfx.util.SessionManager2;
import java.util.prefs.Preferences;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class GrundvattenFilterPopOver extends BaseFilterPopOver {

    private final GrundvattenFilter mFilter;
    private final SessionCheckComboBox<String> mFiltertypSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mGrundvattenmagasinSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mInformationskällaSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mKontrollprogramSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mRörtypSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mSpetstypSCCB = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mStatusSCCB = new SessionCheckComboBox<>();

    public GrundvattenFilterPopOver(GrundvattenFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()));
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
        //mDateRangePane.reset();
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        //clear(); //TODO To clear or not to clear
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");
        mFiltertypSCCB.clearChecks();
        mGrundvattenmagasinSCCB.clearChecks();
        mInformationskällaSCCB.clearChecks();
        mKontrollprogramSCCB.clearChecks();
        mRörtypSCCB.clearChecks();
        mSpetstypSCCB.clearChecks();
        mStatusSCCB.clearChecks();
    }

    @Override
    public void load(Butterfly butterfly) {
        var grundvatten = butterfly.tmo().getGrundvatten();

        mFiltertypSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getFiltertyp()));
        mGrundvattenmagasinSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getGrundvattenmagasin()));
        mInformationskällaSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getInformationskällor()));
        mKontrollprogramSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getKontrollprogram()));
        mRörtypSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getRörtyp()));
        mSpetstypSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getSpetstyp()));
        mStatusSCCB.loadAndRestoreCheckItems(grundvatten.stream().map(o -> o.getStatus()));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mFiltertypSCCB,
                mGrundvattenmagasinSCCB,
                mInformationskällaSCCB,
                mKontrollprogramSCCB,
                mRörtypSCCB,
                mSpetstypSCCB,
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
                mFiltertypSCCB,
                mGrundvattenmagasinSCCB,
                mInformationskällaSCCB,
                mKontrollprogramSCCB,
                mRörtypSCCB,
                mSpetstypSCCB,
                mStatusSCCB
        );

        mFiltertypSCCB.setTitle("Filtertyp");
        mGrundvattenmagasinSCCB.setTitle("Grundvattenmagasin");
        mInformationskällaSCCB.setTitle("Informationskälla");
        mKontrollprogramSCCB.setTitle("Kontrollprogram");
        mRörtypSCCB.setTitle("Rörtyp");
        mSpetstypSCCB.setTitle("Spetstyp");
        mStatusSCCB.setTitle(Dict.STATUS.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusSCCB,
                mGrundvattenmagasinSCCB,
                mRörtypSCCB,
                mFiltertypSCCB,
                mSpetstypSCCB,
                mInformationskällaSCCB,
                mKontrollprogramSCCB
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mFiltertypCheckModel = mFiltertypSCCB.getCheckModel();
        mFilter.mGrundvattenmagasinCheckModel = mGrundvattenmagasinSCCB.getCheckModel();
        mFilter.mInformationskällaCheckModel = mInformationskällaSCCB.getCheckModel();
        mFilter.mKontrollprogramCheckModel = mKontrollprogramSCCB.getCheckModel();
        mFilter.mRörtypCheckModel = mRörtypSCCB.getCheckModel();
        mFilter.mSpetstypCheckModel = mSpetstypSCCB.getCheckModel();
        mFilter.mStatusCheckModel = mStatusSCCB.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private SessionManager2 initSession(Preferences preferences) {
        var sessionManager = new SessionManager2(preferences);
        sessionManager.register("filter.grundvatten.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.grundvatten.checkedFiltertyp", mFiltertypSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedGrundvattenmagasin", mGrundvattenmagasinSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedInformationskälla", mInformationskällaSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedKontrollprogram", mKontrollprogramSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedRörrtyp", mRörtypSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedSpetstyp", mSpetstypSCCB.checkedStringProperty());
        sessionManager.register("filter.grundvatten.checkedStatus", mStatusSCCB.checkedStringProperty());

        return sessionManager;
    }

}
