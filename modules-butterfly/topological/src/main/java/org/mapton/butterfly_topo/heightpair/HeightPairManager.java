/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_topo.heightpair;

import java.util.ArrayList;
import java.util.List;
import javafx.collections.ListChangeListener;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoHeightPair;
import org.mapton.butterfly_topo.api.TopoManager;
import org.mapton.butterfly_topo.grade.GradeChartBuilder;
import se.trixon.almond.util.fx.DelayedResetRunner;
import se.trixon.almond.util.fx.FxHelper;

/**
 *
 * @author Patrik Karlström
 */
public class HeightPairManager extends BaseManager<BTopoHeightPair> {

    protected final TopoManager mTopoManager = TopoManager.getInstance();
    private final GradeChartBuilder mChartBuilder = new GradeChartBuilder();
    private final DelayedResetRunner mDelayedResetRunner;
    private final HeightPairPropertiesBuilder mPropertiesBuilder = new HeightPairPropertiesBuilder();

    public static HeightPairManager getInstance() {
        return Holder.INSTANCE;
    }

    public HeightPairManager() {
        super(BTopoHeightPair.class);
        mDelayedResetRunner = new DelayedResetRunner(1000, () -> {
            new Thread(() -> load()).start();
        });
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            // mDelayedResetRunner.reset();
        });

    }

    @Override
    public Object getObjectChart(BTopoHeightPair selectedObject) {
        return null;
//        return mChartBuilder.build(selectedObject);
    }

    @Override
    public Object getObjectProperties(BTopoHeightPair selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    public void load() {
        var p = new BTopoHeightPair();
        p.setName("123");
        var gradesLim = List.of(p);
        FxHelper.runLater(() -> {
            setItemsAll(gradesLim);
            setItemsFiltered(gradesLim);
            setItemsTimeFiltered(gradesLim);
        });

    }

    @Override
    public void load(Butterfly butterfly) {
        //nvm - load on topo manager changes instead
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoHeightPair> items) {
    }

    private static class Holder {

        private static final HeightPairManager INSTANCE = new HeightPairManager();
    }
}
