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
package org.mapton.butterfly_acoustic.measuring_point;

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
public class MeasPointFilterPopOver extends BaseFilterPopOver {

    private final ResourceBundle mBundle = NbBundle.getBundle(MeasPointFilterPopOver.class);
    private final SessionCheckComboBox<String> mCategorySccb = new SessionCheckComboBox<>();
    private final MeasPointFilter mFilter;
    private final SessionCheckComboBox<String> mGroupSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mSoilSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mStatusSccb = new SessionCheckComboBox<>();

    public MeasPointFilterPopOver(MeasPointFilter filter) {
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
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mSoilSccb
        );
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.noise().getVibrationPoints();
        mStatusSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getStatus()));
        mGroupSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getGroup()));
        mCategorySccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getCategory()));
        mSoilSccb.loadAndRestoreCheckItems(items.stream().map(b -> b.getSoilMaterial()));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        FxHelper.setVisibleRowCount(25,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mSoilSccb
        );
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        SessionCheckComboBox.clearChecks(
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mSoilSccb
        );
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mSoilSccb
        );

        mStatusSccb.setTitle(Dict.STATUS.toString());
        mGroupSccb.setTitle(Dict.GROUP.toString());
        mCategorySccb.setTitle(Dict.CATEGORY.toString());
        mSoilSccb.setTitle(mBundle.getString("soilMaterial"));

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusSccb,
                mGroupSccb,
                mCategorySccb,
                mSoilSccb
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mStatusCheckModel = mStatusSccb.getCheckModel();
        mFilter.mGroupCheckModel = mGroupSccb.getCheckModel();
        mFilter.mCategoryCheckModel = mCategorySccb.getCheckModel();
        mFilter.mSoilCheckModel = mSoilSccb.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.measPoint.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.measPoint.checkedGroup", mGroupSccb.checkedStringProperty());
        sessionManager.register("filter.measPoint.checkedStatus", mStatusSccb.checkedStringProperty());
        sessionManager.register("filter.measPoint.checkedCategory", mCategorySccb.checkedStringProperty());
        sessionManager.register("filter.measPoint.checkedSoil", mSoilSccb.checkedStringProperty());
    }

}
