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
package org.mapton.butterfly_topo.convergence;

import java.util.ArrayList;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoConvergencePoint;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceManager extends BaseManager<BTopoConvergencePoint> {

//    private final ConvergencePropertiesBuilder mPropertiesBuilder = new ConvergencePropertiesBuilder();
    public static ConvergenceManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceManager() {
        super(BTopoConvergencePoint.class);
    }

    @Override
    public Object getObjectProperties(BTopoConvergencePoint selectedObject) {
//        return mPropertiesBuilder.build(selectedObject);
        return selectedObject;
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.topo().getConvergencePoints());
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        getTimeFilteredItems().setAll(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoConvergencePoint> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final ConvergenceManager INSTANCE = new ConvergenceManager();
    }
}
