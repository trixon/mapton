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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import org.mapton.butterfly_format.types.BXyzPoint;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "group",
    "category",
    "status",
    "frequency",
    "operator",
    "origin",
    "limit1",
    "limit2",
    "limit3",
    "dateZero",
    //    "sensors",
    "zeroX",
    "zeroY",
    "zeroZ",
    "dimension",
    "comment",
    "meta"
})
@JsonIgnoreProperties(value = {
    "values",
    "dateRolling",
    "tag",
    "nameOfAlarmHeight",
    "nameOfAlarmPlane",
    "dateValidFrom",
    "dateValidTo",
    "lat",
    "lon",
    "dateLatest"
})
public class BGeoExtensometer extends BXyzPoint {

    private double groundLevel;
    private transient Ext mExt;
    private transient ArrayList<BGeoExtensometerPoint> mPoints = new ArrayList<>();
    private transient Double numOfDecXY;
    private transient Double numOfDecZ;
    private transient Double offsetX;
    private transient Double offsetY;
    private transient Double offsetZ;
    private String referencePointName;
    private transient Double rollingX;
    private transient Double rollingY;
    private transient Double rollingZ;
    private String sensors;

    public BGeoExtensometer() {
    }

    @Override
    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    @Override
    public LocalDateTime getDateChanged() {
        return getPoints().stream().map(p -> p.getDateChanged()).max(LocalDateTime::compareTo).orElse(null);
    }

    @Override
    public LocalDateTime getDateCreated() {
        return getPoints().stream().map(p -> p.getDateCreated()).min(LocalDateTime::compareTo).orElse(null);
    }

    public double getGroundLevel() {
        return groundLevel;
    }

    public ArrayList<BGeoExtensometerPoint> getPoints() {
        return mPoints;
    }

    public String getReferencePointName() {
        return referencePointName;
    }

    public String getSensors() {
        return sensors;
    }

    public void setGroundLevel(double groundLevel) {
        this.groundLevel = groundLevel;
    }

    public void setPoints(ArrayList<BGeoExtensometerPoint> points) {
        this.mPoints = points;
    }

    public void setPoints(String points) {
        this.sensors = points;
    }

    public void setReferencePointName(String referencePointName) {
        this.referencePointName = referencePointName;
    }

    public class Ext extends BXyzPoint.Ext<BGeoExtensometerPointObservation> {

        private BTopoControlPoint mReferencePoint;

        public Ext() {
        }

        public int getAlarmLevel() {
            return getPoints().stream().mapToInt(p -> p.ext().getAlarmLevel()).max().orElse(-1);
        }

        public BTopoControlPoint getReferencePoint() {
            return mReferencePoint;
        }

        public boolean hasNoObservations() {
            return mPoints.stream()
                    .noneMatch(point -> !point.ext().getObservationsAllRaw().isEmpty());
        }

        public void setReferencePoint(BTopoControlPoint referencePoint) {
            this.mReferencePoint = referencePoint;
        }

    }
}
