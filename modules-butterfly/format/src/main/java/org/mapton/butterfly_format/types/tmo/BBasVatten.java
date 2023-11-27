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
public class BBasVatten extends BBasObjekt {

    private Double mFilterlängd;
    private String mGrundvattenmagasin;
    private Boolean mIgengjuten;
    private Double mInstallationsdjup;
    private Double mMarknivå;
    private Double mMaxPejlbartDjup;
    private String mRördimension;
    private Integer mRörlutningsriktning;
    private String mRörtyp;
    private Double mSpetsnivå;

    public BBasVatten() {
    }

    public Double getFilterlängd() {
        return mFilterlängd;
    }

    public String getGrundvattenmagasin() {
        return mGrundvattenmagasin;
    }

    public Boolean getIgengjuten() {
        return mIgengjuten;
    }

    public Double getInstallationsdjup() {
        return mInstallationsdjup;
    }

    public Double getMarknivå() {
        return mMarknivå;
    }

    public Double getMaxPejlbartDjup() {
        return mMaxPejlbartDjup;
    }

    public String getRördimension() {
        return mRördimension;
    }

    public Integer getRörlutningsriktning() {
        return mRörlutningsriktning;
    }

    public String getRörtyp() {
        return mRörtyp;
    }

    public Double getSpetsnivå() {
        return mSpetsnivå;
    }

    public void setFilterlängd(Double filterlängd) {
        this.mFilterlängd = filterlängd;
    }

    public void setGrundvattenmagasin(String grundvattenmagasin) {
        this.mGrundvattenmagasin = grundvattenmagasin;
    }

    public void setIgengjuten(Boolean igengjuten) {
        this.mIgengjuten = igengjuten;
    }

    public void setInstallationsdjup(Double installationsdjup) {
        this.mInstallationsdjup = installationsdjup;
    }

    public void setMarknivå(Double marknivå) {
        this.mMarknivå = marknivå;
    }

    public void setMaxPejlbartDjup(Double maxPejlbartDjup) {
        this.mMaxPejlbartDjup = maxPejlbartDjup;
    }

    public void setRördimension(String rördimension) {
        this.mRördimension = rördimension;
    }

    public void setRörlutningsriktning(Integer rörlutningsriktning) {
        this.mRörlutningsriktning = rörlutningsriktning;
    }

    public void setRörtyp(String rörtyp) {
        this.mRörtyp = rörtyp;
    }

    public void setSpetsnivå(Double spetsnivå) {
        this.mSpetsnivå = spetsnivå;
    }

}
