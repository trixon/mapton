/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_topo.grade;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.FilterSectionMisc;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.grade.horizontal.GradeHManager;
import org.mapton.butterfly_topo.grade.vertical.GradeVManager;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class GradeFilterPopOver extends BaseTabbedFilterPopOver {

    private final GradeFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final FilterSectionMeas mFilterSectionMeas;
    private final FilterSectionMisc mFilterSectionMisc;
    private final GradeHManager mHManager = GradeHManager.getInstance();
    private final GradeManagerBase mManager;
    private final GradeVManager mVManager = GradeVManager.getInstance();

    public GradeFilterPopOver(GradeFilter filter, GradeFilterConfig config) {
        mManager = config.getDimension() == BDimension._1d ? mHManager : mVManager;

        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionMeas = new FilterSectionMeas(config);
        mFilterSectionMisc = new FilterSectionMisc();

        mFilter = filter;
        mFilter.setFilterSection(mFilterSectionDate);
        mFilter.setFilterSection(mFilterSectionMeas);

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

        mFilterSectionDate.clear();
        mFilterSectionMeas.clear();
    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();
    }

    @Override
    public void onPolygonFilterChange() {
        mFilter.update();
    }

    @Override
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");

        mFilterSectionMisc.reset(null);

    }

    private void createUI() {
        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        populateToolBar(mFilterSectionMisc.getInvertCheckboxToolBarItem());

        getTabPane().getTabs().addAll(
                mFilterSectionMeas.getTab()
        //                mFilterSectionDate.getTab()
        );

        setContentNode(root);

        mFilterSectionDate.disable(
                BFilterSectionDate.DateElement.FIRST,
                BFilterSectionDate.DateElement.HAS_FROM_TO
        );

    }

    private void initListeners() {
        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
        });

        mFilterSectionMisc.initListeners(mFilter);
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionMeas.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        sessionManager.register("freeText", mFilter.freeTextProperty());

        return sessionManager;
    }
}
