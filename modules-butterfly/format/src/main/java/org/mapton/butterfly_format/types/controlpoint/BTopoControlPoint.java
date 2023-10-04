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
package org.mapton.butterfly_format.types.controlpoint;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
@JsonPropertyOrder({
    "name",
    "group",
    "category",
    "dimension",
    "status",
    "frequency",
    "operator",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "tag",
    "numOfDecXY",
    "numOfDecZ",
    "offsetX",
    "offsetY",
    "offsetZ",
    "dateLatest",
    "dateRolling",
    "dateZero",
    "zeroX",
    "zeroY",
    "zeroZ",
    "comment",
    "meta"
})
@JsonIgnoreProperties(value = {"values"})
public class BTopoControlPoint extends BBaseControlPoint {

    private BDimension dimension;
    @JsonIgnore
    private Ext mExt;
    private String nameOfAlarmHeight;
    private String nameOfAlarmPlane;
    private Double offsetX;
    private Double offsetY;
    private Double offsetZ;

    public BTopoControlPoint() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public BDimension getDimension() {
        return dimension;
    }

    public String getNameOfAlarmHeight() {
        return nameOfAlarmHeight;
    }

    public String getNameOfAlarmPlane() {
        return nameOfAlarmPlane;
    }

    public Double getOffsetX() {
        return offsetX;
    }

    public Double getOffsetY() {
        return offsetY;
    }

    public Double getOffsetZ() {
        return offsetZ;
    }

    public void setDimension(BDimension dimension) {
        this.dimension = dimension;
    }

    public void setNameOfAlarmHeight(String nameOfAlarmHeight) {
        this.nameOfAlarmHeight = nameOfAlarmHeight;
    }

    public void setNameOfAlarmPlane(String nameOfAlarmPlane) {
        this.nameOfAlarmPlane = nameOfAlarmPlane;
    }

    public void setOffsetX(Double offsetX) {
        this.offsetX = offsetX;
    }

    public void setOffsetY(Double offsetY) {
        this.offsetY = offsetY;
    }

    public void setOffsetZ(Double offsetZ) {
        this.offsetZ = offsetZ;
    }

    public class Ext extends BBaseControlPoint.Ext {

        private transient ArrayList<BTopoControlPointObservation> observationsCalculated;
        private transient ArrayList<BTopoControlPointObservation> observationsRaw;

        public int getNumOfObservations() {
            return getObservationsRaw().size();
        }

        public int getNumOfObservationsTimeFiltered() {
            return getObservationsCalculated().size();
        }

        public ArrayList<BTopoControlPointObservation> getObservationsCalculated() {
            if (observationsCalculated == null) {
                observationsCalculated = new ArrayList<>();
            }

            return observationsCalculated;
        }

        public ArrayList<BTopoControlPointObservation> getObservationsRaw() {
            if (observationsRaw == null) {
                observationsRaw = new ArrayList<>();
            }

            return observationsRaw;
        }

        public void setObservationsCalculated(ArrayList<BTopoControlPointObservation> observationsCalculated) {
            this.observationsCalculated = observationsCalculated;
        }

        public void setObservationsRaw(ArrayList<BTopoControlPointObservation> observationsRaw) {
            this.observationsRaw = observationsRaw;
        }

    }
}
