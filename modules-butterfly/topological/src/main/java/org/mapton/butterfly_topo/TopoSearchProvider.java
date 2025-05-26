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
package org.mapton.butterfly_topo;

import org.mapton.api.Mapton;
import org.mapton.butterfly_core.api.BCoordinatrix;
import org.mapton.butterfly_topo.api.TopoManager;
import org.netbeans.spi.quicksearch.SearchProvider;
import org.netbeans.spi.quicksearch.SearchRequest;
import org.netbeans.spi.quicksearch.SearchResponse;
import se.trixon.almond.util.StringHelper;

public class TopoSearchProvider implements SearchProvider {

    @Override
    public void evaluate(SearchRequest request, SearchResponse response) {
        for (var p : TopoManager.getInstance().getAllItems()) {
            if (StringHelper.matchesSimpleGlob(p.getName(), request.getText(), true, true)) {
                if (!response.addResult(() -> {
                    Mapton.getEngine().panTo(BCoordinatrix.toLatLon(p), .95);
                }, p.getName())) {
                    break;
                }
            }
        }
    }
}
