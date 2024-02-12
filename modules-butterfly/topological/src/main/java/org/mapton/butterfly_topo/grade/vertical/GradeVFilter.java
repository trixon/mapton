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
package org.mapton.butterfly_topo.grade.vertical;

import org.mapton.butterfly_topo.grade.GradeFilterBase;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVFilter extends GradeFilterBase {

    private final GradeVManager mManager = GradeVManager.getInstance();

    public GradeVFilter() {
        super(GradeVManager.getInstance());

        initListeners();
    }

    @Override
    public void update() {
        var filteredItems = mManager.getAllItems().stream()
                .filter(p -> validateFreeText(p.getName()))
                .filter(p -> validateDeltaH(p))
                .filter(p -> validateDeltaR(p))
                .filter(p -> validateDabbaH(p))
                .filter(p -> validateDabbaR(p))
                .filter(p -> validateGradeHorizontal(p))
                .filter(p -> validateGradeVertical(p))
                .toList();

        mManager.getFilteredItems().setAll(filteredItems);
    }

    private void initListeners() {
    }
}
