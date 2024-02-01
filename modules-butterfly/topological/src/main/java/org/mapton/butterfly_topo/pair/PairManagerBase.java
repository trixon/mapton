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
package org.mapton.butterfly_topo.pair;

import javafx.collections.ListChangeListener;
import org.mapton.butterfly_core.api.BaseManager;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;
import org.mapton.butterfly_format.types.topo.BTopoPointPair;
import org.mapton.butterfly_topo.api.TopoManager;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public abstract class PairManagerBase extends BaseManager<BTopoPointPair> {

    protected final TopoManager mTopoManager = TopoManager.getInstance();

    public PairManagerBase(Class<BTopoPointPair> typeParameterClass) {
        super(typeParameterClass);
        TopoManager.getInstance().getTimeFilteredItems().addListener((ListChangeListener.Change<? extends BTopoControlPoint> c) -> {
            new Thread(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
                load();
            }).start();
        });

    }

    public abstract void load();
}
