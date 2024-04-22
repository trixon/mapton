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
package org.mapton.butterfly_format.types.topo;

import java.time.LocalDateTime;
import javafx.geometry.Point3D;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergencePairObservation {

    private Point3D mDelta;
    private Point3D mO1;
    private Point3D mO2;
    private LocalDateTime mDate;

    public BTopoConvergencePairObservation() {
    }

    public BTopoConvergencePairObservation(LocalDateTime date, Point3D o1, Point3D o2) {
        this.mDate = date;
        this.mO1 = o1;
        this.mO2 = o2;
        mDelta = o2.subtract(o1);
    }

}
