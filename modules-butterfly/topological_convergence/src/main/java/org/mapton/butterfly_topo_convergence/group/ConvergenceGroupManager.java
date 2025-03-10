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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.Butterfly;
import org.mapton.butterfly_format.types.topo.BTopoConvergenceGroup;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class ConvergenceGroupManager extends BaseManager<BTopoConvergenceGroup> {

    private final ArrayList<BTopoConvergenceGroup> mDynamicItems = new ArrayList<>();
    private final ConvergenceGroupPropertiesBuilder mPropertiesBuilder = new ConvergenceGroupPropertiesBuilder();

    public static ConvergenceGroupManager getInstance() {
        return Holder.INSTANCE;
    }

    private ConvergenceGroupManager() {
        super(BTopoConvergenceGroup.class);
    }

    public void add(BTopoConvergenceGroup group) {
        var butterfly = group.getButterfly();
        butterfly.topo().getConvergenceGroups().add(group);
        load(butterfly);
    }

    @Override
    public Object getObjectProperties(BTopoConvergenceGroup selectedObject) {
        return mPropertiesBuilder.build(selectedObject);
    }

    @Override
    public void initObjectToItemMap() {
    }

    @Override
    public void load(Butterfly butterfly) {
        try {
            initAllItems(butterfly.topo().getConvergenceGroups());
            for (var convergenceGroup : butterfly.topo().getConvergenceGroups()) {
                var controlPoints = Arrays.stream(StringUtils.split(convergenceGroup.getRef(), ","))
                        .map(s -> butterfly.topo().getControlPointByName(s))
                        .filter(p -> p != null)
                        .collect(Collectors.toCollection(ArrayList::new));

                convergenceGroup.ext2().setControlPoints(controlPoints);
            }
        } catch (Exception e) {
            Exceptions.printStackTrace(e);
        }
    }

    @Override
    protected void applyTemporalFilter() {
        setItemsTimeFiltered(getFilteredItems());
    }

    @Override
    protected void load(ArrayList<BTopoConvergenceGroup> items) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private static class Holder {

        private static final ConvergenceGroupManager INSTANCE = new ConvergenceGroupManager();
    }
}
