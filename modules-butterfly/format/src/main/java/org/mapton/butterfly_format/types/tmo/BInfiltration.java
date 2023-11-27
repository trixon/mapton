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

/**
 *
 * @author Patrik Karlström
 */
public class BInfiltration extends BBasVatten {

    private String mInfiltrationstyp;
    private Double mKapacitet;
    private Double mStyrnivå_nedre;
    private Double mStyrnivå_övre;
    private Double mTryckgivarnivå;

    public BInfiltration() {
    }

    public String getInfiltrationstyp() {
        return mInfiltrationstyp;
    }

    public Double getKapacitet() {
        return mKapacitet;
    }

    public Double getStyrnivå_nedre() {
        return mStyrnivå_nedre;
    }

    public Double getStyrnivå_övre() {
        return mStyrnivå_övre;
    }

    public Double getTryckgivarnivå() {
        return mTryckgivarnivå;
    }

    public void setInfiltrationstyp(String infiltrationstyp) {
        this.mInfiltrationstyp = infiltrationstyp;
    }

    public void setKapacitet(Double kapacitet) {
        this.mKapacitet = kapacitet;
    }

    public void setStyrnivå_nedre(Double styrnivå_nedre) {
        this.mStyrnivå_nedre = styrnivå_nedre;
    }

    public void setStyrnivå_övre(Double styrnivå_övre) {
        this.mStyrnivå_övre = styrnivå_övre;
    }

    public void setTryckgivarnivå(Double tryckgivarnivå) {
        this.mTryckgivarnivå = tryckgivarnivå;
    }
}
