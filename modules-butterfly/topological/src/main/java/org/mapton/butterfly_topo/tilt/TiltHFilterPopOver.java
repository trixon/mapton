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
package org.mapton.butterfly_topo.tilt;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo.TopoFilterFavorite;

/**
 *
 * @author Patrik Karlström
 */
public class TiltHFilterPopOver extends BaseFilterPopOver<TopoFilterFavorite> {

    private final CheckBox mCheckbox = new CheckBox("TEST");
    private final TiltHFilter mFilter;

    public TiltHFilterPopOver(TiltHFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        getPolygonFilterCheckBox().setSelected(false);
        mFilter.freeTextProperty().set("");
        mCheckbox.setSelected(false);
    }

    @Override
    public void load(Butterfly butterfly) {
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");
    }

    private void createUI() {
        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                mCheckbox
        );

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.property().bind(mCheckbox.selectedProperty());
        mFilter.polygonFilterProperty().bind(getPolygonFilterCheckBox().selectedProperty());
    }

    private void initSession() {
        getSessionManager().register("freeText", mFilter.freeTextProperty());
    }

}
