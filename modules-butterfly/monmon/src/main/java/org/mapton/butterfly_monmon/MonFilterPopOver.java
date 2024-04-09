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
package org.mapton.butterfly_monmon;

import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_core.api.BaseFilterPopOver;
import org.mapton.butterfly_format.Butterfly;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MonFilterPopOver extends BaseFilterPopOver {

    private final double mDefault1 = 0.0;
    private final double mDefault14 = 0.8;
    private final double mDefault7 = 0.5;
    private final MonFilter mFilter;
    private final CheckBox mLatest14Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest14Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault14, 0.05);
    private final CheckBox mLatest1Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest1Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault1, 0.05);
    private final CheckBox mLatest7Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest7Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault7, 0.05);

    public MonFilterPopOver(MonFilter filter) {
        mFilter = filter;
        createUI();
        initListeners();
        initSession();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");
        mLatest1Checkbox.setSelected(false);
        mLatest1Sds.getValueFactory().setValue(mDefault1);
        mLatest7Checkbox.setSelected(false);
        mLatest7Sds.getValueFactory().setValue(mDefault7);
        mLatest14Checkbox.setSelected(false);
        mLatest14Sds.getValueFactory().setValue(mDefault14);
    }

    @Override
    public void load(Butterfly butterfly) {
        mLatest1Sds.load();
        mLatest7Sds.load();
        mLatest14Sds.load();
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
        mLatest1Checkbox.setText("Senaste dygnet");
        mLatest7Checkbox.setText("Senaste veckan");
        mLatest14Checkbox.setText("Senaste två veckorna");
        mLatest1Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mLatest7Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());
        mLatest14Sds.getValueFactory().setConverter(new NegPosStringConverterDouble());

        int titleGap = SwingHelper.getUIScaled(2);

        var vBox = new VBox(GAP,
                getButtonBox(),
                new Separator(),
                new VBox(titleGap,
                        mLatest1Checkbox,
                        mLatest1Sds
                ),
                new VBox(titleGap,
                        mLatest7Checkbox,
                        mLatest7Sds
                ),
                new VBox(titleGap,
                        mLatest14Checkbox,
                        mLatest14Sds
                )
        );

        FxHelper.setEditable(true, mLatest1Sds, mLatest7Sds, mLatest14Sds);
        FxHelper.autoCommitSpinners(mLatest1Sds, mLatest7Sds, mLatest14Sds);
        //mDiffMeasAllSds.prefWidthProperty().bind(vBox.widthProperty());

        autoSize(vBox);
        setContentNode(vBox);
    }

    private void initListeners() {
        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());

        mFilter.latest1Property().bind(mLatest1Checkbox.selectedProperty());
        mFilter.latest1ValueProperty().bind(mLatest1Sds.sessionValueProperty());
        mFilter.latest7Property().bind(mLatest7Checkbox.selectedProperty());
        mFilter.latest7ValueProperty().bind(mLatest7Sds.sessionValueProperty());
        mFilter.latest14Property().bind(mLatest14Checkbox.selectedProperty());
        mFilter.latest14ValueProperty().bind(mLatest14Sds.sessionValueProperty());

        mLatest1Sds.disableProperty().bind(mLatest1Checkbox.selectedProperty().not());
        mLatest7Sds.disableProperty().bind(mLatest7Checkbox.selectedProperty().not());
        mLatest14Sds.disableProperty().bind(mLatest14Checkbox.selectedProperty().not());

        mFilter.initCheckModelListeners();
    }

    private void initSession() {
        var sessionManager = getSessionManager();
        getSessionManager().register("filter.freeText", mFilter.freeTextProperty());
        sessionManager.register("filter.latest1", mLatest1Checkbox.selectedProperty());
        sessionManager.register("filter.latest7", mLatest7Checkbox.selectedProperty());
        sessionManager.register("filter.latest14", mLatest14Checkbox.selectedProperty());
    }

}
