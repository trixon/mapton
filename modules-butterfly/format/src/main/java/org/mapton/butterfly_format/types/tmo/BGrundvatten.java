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

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.Comparator;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public class BGrundvatten extends BBasVatten {

    @JsonIgnore
    private Ext mExt;
    private String mFiltertyp;
    private Integer mGradning;
    private Double mReferensnivå;
    private String mSpetstyp;

    public BGrundvatten() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getFiltertyp() {
        return mFiltertyp;
    }

    public Integer getGradning() {
        return mGradning;
    }

    public Double getReferensnivå() {
        return mReferensnivå;
    }

    public String getSpetstyp() {
        return mSpetstyp;
    }

    public void setFiltertyp(String filtertyp) {
        this.mFiltertyp = filtertyp;
    }

    public void setGradning(Integer gradning) {
        this.mGradning = gradning;
    }

    public void setReferensnivå(Double referensnivå) {
        this.mReferensnivå = referensnivå;
    }

    public void setSpetstyp(String spetstyp) {
        this.mSpetstyp = spetstyp;
    }

    public class Ext extends BBasePoint.Ext<BGrundvattenObservation> {

        public BGrundvattenObservation getMaxObservation() {
            return getObservationsAllRaw().stream().max(Comparator.comparing(BGrundvattenObservation::getNivå)).orElse(null);
        }

        public BGrundvattenObservation getMinObservation() {
            return getObservationsAllRaw().stream().min(Comparator.comparing(BGrundvattenObservation::getNivå)).orElse(null);
        }
    }
}
