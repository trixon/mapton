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
package org.mapton.butterfly_format.types.geo;

import java.util.TreeSet;
import org.mapton.butterfly_format.types.BXyzPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BGeoInclinometerPoint extends BXyzPoint {

    private transient Ext mExt;

    public BGeoInclinometerPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public class Ext extends BXyzPoint.Ext<BGeoInclinometerPointObservation> {

        public TreeSet<Double> getDepths() {
            var set = new TreeSet<Double>();
            for (var o : getObservationsTimeFiltered()) {
                for (var oi : o.getObservationItems()) {
                    set.add(oi.getDown());
                }
            }

            return set;
        }
    }

}
