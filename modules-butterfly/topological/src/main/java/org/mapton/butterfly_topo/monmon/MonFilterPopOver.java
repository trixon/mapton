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
package org.mapton.butterfly_topo.monmon;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import static org.mapton.api.ui.MPopOver.GAP;
import static org.mapton.api.ui.MPopOver.autoSize;
import org.mapton.api.ui.forms.NegPosStringConverterDouble;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionMisc;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.ButterflyFormFilter;
import org.mapton.butterfly_format.Butterfly;
import org.openide.util.NbPreferences;
import se.trixon.almond.util.fx.FxHelper;
import se.trixon.almond.util.fx.session.SessionDoubleSpinner;
import se.trixon.almond.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlström
 */
public class MonFilterPopOver extends BaseTabbedFilterPopOver {

    private final double mDefault1 = 0.0;
    private final double mDefault14 = 0.8;
    private final double mDefault7 = 0.5;
    private final MonFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final BFilterSectionMisc mFilterSectionMisc;
    private final BFilterSectionPoint mFilterSectionPoint;
    private final CheckBox mLatest14Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest14Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault14, 0.05);
    private final CheckBox mLatest1Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest1Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault1, 0.05);
    private final CheckBox mLatest7Checkbox = new CheckBox();
    private final SessionDoubleSpinner mLatest7Sds = new SessionDoubleSpinner(-1.0, 1.0, mDefault7, 0.05);
    private final MonManager mManager = MonManager.getInstance();

    public MonFilterPopOver(ButterflyFormFilter filter) {
        mFilterSectionPoint = new BFilterSectionPoint();
        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionMisc = new BFilterSectionMisc(filter);

        mFilter = (MonFilter) filter;

        mFilter.setFilterSection(mFilterSectionPoint);
        mFilter.setFilterSection(mFilterSectionDate);
        mFilter.setFilterSection(mFilterSectionMisc);

        setFilter(filter);
        createUI();
        initListeners();
        initSession(NbPreferences.forModule(getClass()).node(getClass().getSimpleName()));

        populate();
    }

    @Override
    public void clear() {
        setUsePolygonFilter(false);
        mFilter.freeTextProperty().set("");

        mFilterSectionPoint.clear();
        mFilterSectionDate.clear();
        mFilterSectionMisc.clear();

        resetTabs();
        setUsePolygonFilter(false);
//        mLatest1Checkbox.setSelected(false);
//        mLatest1Sds.getValueFactory().setValue(mDefault1);
//        mLatest7Checkbox.setSelected(false);
//        mLatest7Sds.getValueFactory().setValue(mDefault7);
//        mLatest14Checkbox.setSelected(false);
//        mLatest14Sds.getValueFactory().setValue(mDefault14);
    }

    @Override
    public void load(Butterfly butterfly) {
        var mons = mManager.getAllItems();

        mFilterSectionPoint.load(mons);
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();
//        mLatest1Sds.load();
//        mLatest7Sds.load();
//        mLatest14Sds.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void onShownFirstTime() {
        mFilterSectionPoint.onShownFirstTime();
    }

    @Override
    public void presetRestore(Preferences preferences) {
        clear();
        presetStore(preferences);
    }

    @Override
    public void presetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void reset() {
        clear();
        if (getFilterPresetPopOver().restoreDefaultIfExists()) {
            //
        } else {
            mFilterSectionPoint.reset(null);
            mFilterSectionMisc.reset(null);
        }
    }

    private void createUI() {
        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        populateToolBar(mFilterSectionMisc.getInvertCheckboxToolBarItem(), mFilterSectionMisc.getInvisibleCheckboxToolBarItem());

        getTabPane().getTabs().addAll(
                mFilterSectionPoint.getTab(),
                mFilterSectionDate.getTab()
        );

        setContentNode(root);

//        mFilterSectionPoint.disable(
//                PointElement.CATEGORY,
//                PointElement.FREQUENCY,
//                PointElement.FREQUENCY_DEFAULT,
//                PointElement.FREQUENCY_DEFAULT_STAT,
//                PointElement.MEAS_MODE,
//                PointElement.MEAS_NEXT,
//                PointElement.STATUS
//        );
//
//        mFilterSectionDate.disable(
//                DateElement.FIRST,
//                DateElement.HAS_FROM_TO
//        );
    }

    private void createUI_OLD() {
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
        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
//            mFilterSectionAlarm.getSameAlarmCheckBox().setSelected(true);
        });

//        mFilterSectionMeas.initListeners(mFilter);
        mFilterSectionMisc.initListeners(mFilter);

        mFilter.latest1Property().bind(mLatest1Checkbox.selectedProperty());
        mFilter.latest1ValueProperty().bind(mLatest1Sds.sessionValueProperty());
        mFilter.latest7Property().bind(mLatest7Checkbox.selectedProperty());
        mFilter.latest7ValueProperty().bind(mLatest7Sds.sessionValueProperty());
        mFilter.latest14Property().bind(mLatest14Checkbox.selectedProperty());
        mFilter.latest14ValueProperty().bind(mLatest14Sds.sessionValueProperty());

        mLatest1Sds.disableProperty().bind(mLatest1Checkbox.selectedProperty().not());
        mLatest7Sds.disableProperty().bind(mLatest7Checkbox.selectedProperty().not());
        mLatest14Sds.disableProperty().bind(mLatest14Checkbox.selectedProperty().not());

        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.initCheckModelListeners();
    }

//    private void initSession() {
//        var sessionManager = getSessionManager();
//        getSessionManager().register("filter.freeText", mFilter.freeTextProperty());
//        sessionManager.register("filter.latest1", mLatest1Checkbox.selectedProperty());
//        sessionManager.register("filter.latest7", mLatest7Checkbox.selectedProperty());
//        sessionManager.register("filter.latest14", mLatest14Checkbox.selectedProperty());
//    }
    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        return sessionManager;
    }

}
