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
package org.mapton.butterfly_format.types.tmo;

import java.time.LocalDateTime;
import java.util.ArrayList;

/**
 *
 * @author Patrik Karlström
 */
public class BRorelse extends BBasObjekt {

    private Ext mExt;
    private String mFixpunkt;
    private String mPlacering;
    private String mPlacering_kommentar;

    public BRorelse() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getFixpunkt() {
        return mFixpunkt;
    }

    public String getPlacering() {
        return mPlacering;
    }

    public String getPlacering_kommentar() {
        return mPlacering_kommentar;
    }

    public void setFixpunkt(String fixpunkt) {
        this.mFixpunkt = fixpunkt;
    }

    public void setPlacering(String placering) {
        this.mPlacering = placering;
    }

    public void setPlacering_kommentar(String placering_kommentar) {
        this.mPlacering_kommentar = placering_kommentar;
    }

    public class Ext {

        private LocalDateTime mDateLatest;
        private transient ArrayList<BRorelseObservation> observationsAllRaw;

        public LocalDateTime getDateLatest() {
            return mDateLatest;
        }

        public ArrayList<BRorelseObservation> getObservationsAllRaw() {
            return observationsAllRaw;
        }

        public void setDateLatest(LocalDateTime dateLatest) {
            this.mDateLatest = dateLatest;
        }

        public void setObservationsAllRaw(ArrayList<BRorelseObservation> observationsAllRaw) {
            this.observationsAllRaw = observationsAllRaw;
        }

    }
}
