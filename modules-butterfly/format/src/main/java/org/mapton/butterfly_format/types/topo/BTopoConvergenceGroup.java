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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;

/**
 *
 * @author Patrik Karlström
 */
public class BTopoConvergenceGroup extends BTopoControlPoint {

    @JsonIgnore
    private Ext mExt;

    private String mRef;

    public Ext ext2() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getRef() {
        return mRef;
    }

    public void setRef(String ref) {
        this.mRef = ref;
    }

    public class Ext {

        private ArrayList<BTopoControlPoint> mControlPoints = new ArrayList<>();

        public ArrayList<BTopoControlPoint> getControlPoints() {
            return mControlPoints;
        }

        public void setControlPoints(ArrayList<BTopoControlPoint> controlPoints) {
            this.mControlPoints = controlPoints;
        }

    }

}
