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
public class BlastFilterPopOver extends BaseFilterPopOver {

    private final BlastFilter mFilter;
    private final CheckComboBox<String> mGroupCheckComboBox = new CheckComboBox<>();
    private final CheckModelSession mGroupCheckModelSession = new CheckModelSession(mGroupCheckComboBox);

    public BlastFilterPopOver(BlastFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mGroupCheckComboBox.getCheckModel().clearChecks();

    }

    @Override
    public void load(Butterfly butterfly) {
        var blasts = butterfly.acoustic().getBlasts();

        var groupCheckModel = mGroupCheckComboBox.getCheckModel();
        var checkedGroup = groupCheckModel.getCheckedItems();
        var allGroupss = new TreeSet<>(blasts.stream().map(o -> o.getGroup()).collect(Collectors.toSet()));

        mGroupCheckComboBox.getItems().setAll(allGroupss);
        checkedGroup.stream().forEach(d -> groupCheckModel.check(d));

        mGroupCheckModelSession.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        var dropDownCount = 25;
        FxHelper.getComboBox(mGroupCheckComboBox).setVisibleRowCount(dropDownCount);
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        mGroupCheckComboBox.setShowCheckedCount(true);
        mGroupCheckComboBox.setTitle(Dict.GROUP.toString());

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mGroupCheckComboBox
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());

        mFilter.setCheckModelGroup(mGroupCheckComboBox.getCheckModel());

    }

    private void initSession() {
        var sessionManager = getSessionManager();
        sessionManager.register("filter.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.checkedGroup", mGroupCheckModelSession.checkedStringProperty());

    }

}
