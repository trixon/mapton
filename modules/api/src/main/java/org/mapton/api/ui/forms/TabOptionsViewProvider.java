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
package org.mapton.api.ui.forms;

import java.util.Comparator;
import java.util.List;
import javafx.scene.Node;
import org.openide.util.Lookup;

/**
 *
 * @author Patrik Karlström
 */
public interface TabOptionsViewProvider {

    static TabOptionsViewProvider getInstance(Class<? extends TabOptionsViewProvider> cls) {
        return Lookup.getDefault().lookupAll(cls).stream().findFirst().orElse(null);
    }

    static List<? extends TabOptionsViewProvider> getProviders(String parent) {
        return Lookup.getDefault().lookupAll(TabOptionsViewProvider.class).stream()
                .sorted(Comparator.comparing(TabOptionsViewProvider::getOvPosition)).toList();
    }

    default String getOvId() {
        return getClass().getName();
    }

    Node getOvNode();

    String getOvParent();

    default int getOvPosition() {
        return Integer.MAX_VALUE;
    }

    String getOvTitle();

}
