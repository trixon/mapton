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
package org.mapton.butterfly_topo_convergence.group;

import com.dlsc.gemsfx.util.SessionManager;
import java.util.prefs.Preferences;
import javafx.scene.layout.BorderPane;
import org.mapton.butterfly_core.api.BFilterSectionDate;
import org.mapton.butterfly_core.api.BFilterSectionDisruptor;
import org.mapton.butterfly_core.api.BFilterSectionPoint;
import org.mapton.butterfly_core.api.BaseTabbedFilterPopOver;
import org.mapton.butterfly_core.api.FilterSectionMisc;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_topo_convergence.api.ConvergenceGroupManager;
import org.openide.util.NbPreferences;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupFilterPopOver extends BaseTabbedFilterPopOver {

    private final ConvergenceGroupFilter mFilter;
    private final BFilterSectionDate mFilterSectionDate;
    private final BFilterSectionDisruptor mFilterSectionDisruptor;
    private final FilterSectionMisc mFilterSectionMisc;
    private final BFilterSectionPoint mFilterSectionPoint;
    private final ConvergenceGroupManager mManager = ConvergenceGroupManager.getInstance();

    public ConvergenceGroupFilterPopOver(ConvergenceGroupFilter filter) {
        mFilterSectionPoint = new BFilterSectionPoint();
        mFilterSectionDate = new BFilterSectionDate();
        mFilterSectionDisruptor = new BFilterSectionDisruptor();
        mFilterSectionMisc = new FilterSectionMisc();

        mFilter = filter;
        mFilter.setFilterSection(mFilterSectionPoint);
        mFilter.setFilterSection(mFilterSectionDate);
        mFilter.setFilterSection(mFilterSectionDisruptor);

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
        mFilterSectionDisruptor.clear();
        mFilterSectionMisc.clear();

    }

    @Override
    public void filterPresetRestore(Preferences preferences) {
        clear();
        filterPresetStore(preferences);
        //mDateRangePane.reset();
    }

    @Override
    public void filterPresetStore(Preferences preferences) {
        var sessionManager = initSession(preferences);
        sessionManager.unregisterAll();
    }

    @Override
    public void load(Butterfly butterfly) {
        var items = butterfly.topo().getConvergenceGroups();

        mFilterSectionPoint.load(items);
        mFilterSectionDisruptor.load();
        mFilterSectionDate.load(mManager.getTemporalRange());
        mFilterSectionMisc.load();
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
    public void reset() {
        clear();
        mFilter.freeTextProperty().set("*");

        mFilterSectionPoint.reset(null);
        mFilterSectionMisc.reset(null);
    }

    private void createUI() {
        var root = new BorderPane(getTabPane());
        root.setTop(getToolBar());
        populateToolBar(mFilterSectionMisc.getInvertCheckboxToolBarItem());

        getTabPane().getTabs().addAll(
                mFilterSectionPoint.getTab(),
                mFilterSectionDate.getTab(),
                mFilterSectionDisruptor.getTab()
        );

        setContentNode(root);

        mFilterSectionPoint.disable(
                BFilterSectionPoint.PointElement.ALARM
        );
    }

    private void initListeners() {
        activatePasteName(actionEvent -> {
            mFilter.freeTextProperty().set(mManager.getSelectedItem().getName());
        });

        mFilterSectionMisc.initListeners(mFilter);

        mFilter.polygonFilterProperty().bind(usePolygonFilterProperty());
        mFilter.initCheckModelListeners();
    }

    private SessionManager initSession(Preferences preferences) {
        var sessionManager = new SessionManager(preferences);
        mFilterSectionPoint.initSession(sessionManager);
        mFilterSectionDate.initSession(sessionManager);
        mFilterSectionDisruptor.initSession(sessionManager);
        mFilterSectionMisc.initSession(sessionManager);

        sessionManager.register("filter.convergence.group.freeText", mFilter.freeTextProperty());

        return sessionManager;
    }

}
