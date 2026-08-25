/*
 * Copyright 2026 Patrik Karlström.
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
package org.mapton.butterfly_format.types.composite;

import java.util.ArrayList;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.structural.BStructuralLoadCellPoint;
import org.mapton.butterfly_format.types.structural.BStructuralStrainGaugePoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class BCompositePoint extends BXyzPoint {

    private transient Ext mExt;
    private String managerNames;
    private String pointNames;

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getManagerNames() {
        return managerNames;
    }

    public String getPointNames() {
        return pointNames;
    }

    public void setManagerNames(String managerNames) {
        this.managerNames = managerNames;
    }

    public void setPointNames(String pointNames) {
        this.pointNames = pointNames;
    }

    public class Ext extends BXyzPoint.Ext<BCompositePointObservation> {

        private final ArrayList<BTopoControlPoint> mTopoPoints = new ArrayList();
        private final ArrayList<BStructuralLoadCellPoint> mLoadPoints = new ArrayList();
        private final ArrayList<BStructuralStrainGaugePoint> mStrainPoints = new ArrayList();

        public ArrayList<BStructuralLoadCellPoint> getLoadPoints() {
            return mLoadPoints;
        }

        public ArrayList<BStructuralStrainGaugePoint> getStrainPoints() {
            return mStrainPoints;
        }

        public ArrayList<BTopoControlPoint> getTopoPoints() {
            return mTopoPoints;
        }
    }

}
