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

import java.util.Comparator;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public class BInfiltration extends BBasVatten {

    private String infiltrationstyp;
    private Double kapacitet;
    private Double styrnivå_nedre;
    private Double styrnivå_övre;
    private Double tryckgivarnivå;
    private transient Ext mExt;

    public BInfiltration() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getInfiltrationstyp() {
        return infiltrationstyp;
    }

    public Double getKapacitet() {
        return kapacitet;
    }

    public Double getStyrnivå_nedre() {
        return styrnivå_nedre;
    }

    public Double getStyrnivå_övre() {
        return styrnivå_övre;
    }

    public Double getTryckgivarnivå() {
        return tryckgivarnivå;
    }

    public void setInfiltrationstyp(String infiltrationstyp) {
        this.infiltrationstyp = infiltrationstyp;
    }

    public void setKapacitet(Double kapacitet) {
        this.kapacitet = kapacitet;
    }

    public void setStyrnivå_nedre(Double styrnivå_nedre) {
        this.styrnivå_nedre = styrnivå_nedre;
    }

    public void setStyrnivå_övre(Double styrnivå_övre) {
        this.styrnivå_övre = styrnivå_övre;
    }

    public void setTryckgivarnivå(Double tryckgivarnivå) {
        this.tryckgivarnivå = tryckgivarnivå;
    }

    public class Ext extends BBasePoint.Ext<BInfiltrationObservation> {

        public BInfiltrationObservation getMaxObservation() {
            return getObservationsAllRaw().stream().max(Comparator.comparing(BInfiltrationObservation::getValue)).orElse(null);
        }

        public BInfiltrationObservation getMinObservation() {
            return getObservationsAllRaw().stream().min(Comparator.comparing(BInfiltrationObservation::getValue)).orElse(null);
        }
    }
}
