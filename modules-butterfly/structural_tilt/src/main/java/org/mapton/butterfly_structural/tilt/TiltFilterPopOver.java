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
package org.mapton.butterfly_structural.tilt;

import java.util.ResourceBundle;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbBundle;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class TiltFilterPopOver extends BaseFilterPopOver {

    private final TiltFilter mFilter;
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mTypeSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mSoilSccb = new SessionCheckComboBox<>();
    private final ResourceBundle mBundle = NbBundle.getBundle(TiltFilterPopOver.class);

    public TiltFilterPopOver(TiltFilter filter) {
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
                mGroupSccb,
                mTypeSccb,
                mSoilSccb
        );
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.structural().getTiltPoints();
        mGroupSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getGroup()));
//        mTypeSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getTypeOfWork()));
//        mSoilSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getSoilMaterial()));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mGroupSccb,
                mTypeSccb,
                mSoilSccb
        );
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        SessionCheckComboBox.clearChecks(
                mGroupSccb,
                mTypeSccb,
                mSoilSccb
        );
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mGroupSccb,
                mTypeSccb,
                mSoilSccb
        );

        mGroupSccb.setTitle(Dict.GROUP.toString());
        mTypeSccb.setTitle(Dict.TYPE.toString());
//        mSoilSccb.setTitle(mBundle.getString("soilMaterial"));

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mGroupSccb,
                mTypeSccb,
                mSoilSccb
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mGroupCheckModel = mGroupSccb.getCheckModel();
        mFilter.mTypeCheckModel = mTypeSccb.getCheckModel();
        mFilter.mSoilCheckModel = mSoilSccb.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.measPoint.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.measPoint.checkedGroup", mGroupSccb.checkedStringProperty());
        sessionManager.register("filter.measPoint.checkedType", mTypeSccb.checkedStringProperty());
        sessionManager.register("filter.measPoint.checkedSoil", mSoilSccb.checkedStringProperty());
    }

}
