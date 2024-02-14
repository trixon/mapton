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

import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_topo.grade.GradeFilterBase;
import org.mapton.butterfly_topo.grade.GradeFilterConfig;
import org.mapton.butterfly_topo.grade.GradeFilterPopOverBase;

/**
 *
 * @author Patrik Karlström
 */
public class GradeVFilterPopOver extends GradeFilterPopOverBase {

    public GradeVFilterPopOver(GradeFilterBase filter, GradeFilterConfig config) {
        super(filter, config, BDimension._3d);
    }

}