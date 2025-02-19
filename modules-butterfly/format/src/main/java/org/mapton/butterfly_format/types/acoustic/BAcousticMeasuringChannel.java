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
package org.mapton.butterfly_format.types.acoustic;

import java.time.LocalDate;
import java.util.ArrayList;

/**
 *
 * @author Patrik Karlström
 */
public class BAcousticMeasuringChannel {

    private LocalDate from;
    private String id;
    private transient Ext mExt;
    private String pointId;
    private String type;
    private LocalDate until;

    public BAcousticMeasuringChannel() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public LocalDate getFrom() {
        return from;
    }

    public String getId() {
        return id;
    }

    public String getPointId() {
        return pointId;
    }

    public String getType() {
        return type;
    }

    public LocalDate getUntil() {
        return until;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPointId(String pointId) {
        this.pointId = pointId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setUntil(LocalDate until) {
        this.until = until;
    }

    public class Ext {

        private ArrayList<BAcousticMeasuringObservation> mObservations = new ArrayList<>();

        public ArrayList<BAcousticMeasuringObservation> getObservations() {
            return mObservations;
        }

        public void setObservations(ArrayList<BAcousticMeasuringObservation> observations) {
            this.mObservations = observations;
        }
    }

}
