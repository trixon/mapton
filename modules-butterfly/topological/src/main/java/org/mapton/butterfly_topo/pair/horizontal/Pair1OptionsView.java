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
package org.mapton.butterfly_topo.pair.horizontal;

import javafx.scene.Node;
import org.mapton.api.ui.forms.TabOptionsViewProvider;
import org.mapton.butterfly_topo.pair.PairManagerBase;
import org.mapton.worldwind.api.MOptionsView;
import org.openide.util.NbBundle;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = TabOptionsViewProvider.class)
public class Pair1OptionsView extends MOptionsView implements TabOptionsViewProvider {

    public Pair1OptionsView() {
    }

    @Override
    public Node getOvNode() {
        return this;
    }

    @Override
    public String getOvParent() {
        return "TopoOptionsView";
    }

    @Override
    public int getOvPosition() {
        return 2;
    }

    @Override
    public String getOvTitle() {
        return NbBundle.getMessage(PairManagerBase.class, "tilt_h");
    }

}
