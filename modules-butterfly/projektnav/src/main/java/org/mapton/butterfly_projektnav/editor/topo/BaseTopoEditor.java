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
package org.mapton.butterfly_projektnav.editor.topo;

import java.util.ArrayList;
import static org.mapton.butterfly_format.types.BDimension._1d;
import static org.mapton.butterfly_format.types.BDimension._2d;
import static org.mapton.butterfly_format.types.BDimension._3d;
import org.mapton.butterfly_projektnav.editor.BaseEditor;
import org.mapton.butterfly_topo.api.TopoManager;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BaseTopoEditor extends BaseEditor {

    protected final TopoManager mManager = TopoManager.getInstance();

    @Override
    public String getParent() {
        return "%s/Topo".formatted(super.getParent());
    }

    public ArrayList<String> getPointWithNavetNames(String[] points) {
        var names = new ArrayList<String>();
        for (var name : points) {
            var p = mManager.getItemForKey(name);

            if (p != null) {
                if (null == p.getDimension()) {
                    names.add(name + "_P");
                } else {
                    switch (p.getDimension()) {
                        case _1d ->
                            names.add(name);
                        case _2d ->
                            names.add(name + "_P");
                        case _3d -> {
                            names.add(name + "_P");
                            names.add(name + "_H");
                        }
                    }
                }
            } else {
                System.err.println("Point not found: " + name);
            }
            mManager.getAllItemsSet();
        }

        return names;
    }

}
