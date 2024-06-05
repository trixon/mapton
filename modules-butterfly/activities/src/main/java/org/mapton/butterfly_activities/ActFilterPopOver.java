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
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionCheckComboBox;

/**
 *
 * @author Patrik Karlström
 */
public class ActFilterPopOver extends BaseFilterPopOver {

    private final ActFilter mFilter;
    private final SessionCheckComboBox<String> mStatusSccb = new SessionCheckComboBox<>();
    private final SessionCheckComboBox<String> mOriginSccb = new SessionCheckComboBox<>();

    public ActFilterPopOver(ActFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");
        mStatusSccb.getCheckModel().clearChecks();
        SessionCheckComboBox.clearChecks(
                mOriginSccb,
                mStatusSccb
        );
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.getAreaActivities();
        mOriginSccb.loadAndRestoreCheckItems(items.stream().map(o -> o.getOrigin()));
        mStatusSccb.loadAndRestoreCheckItems(items.stream().map(a -> ActHelper.getStatusAsString(a.getStatus())));
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
        mStatusSccb.getCheckModel().clearChecks();
    }

    private void createUI() {
        FxHelper.setShowCheckedCount(true,
                mStatusSccb,
                mOriginSccb
        );

        mStatusSccb.setTitle(Dict.STATUS.toString());
        mOriginSccb.setTitle(Dict.ORIGIN.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mStatusSccb,
                mOriginSccb
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.mStatusCheckModel = mStatusSccb.getCheckModel();
        mFilter.mOriginCheckModel = mOriginSccb.getCheckModel();

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        getSessionManager().register("freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.checkedStatus", mStatusSccb.checkedStringProperty());
        sessionManager.register("filter.checkedOrigin", mOriginSccb.checkedStringProperty());

    }

}
