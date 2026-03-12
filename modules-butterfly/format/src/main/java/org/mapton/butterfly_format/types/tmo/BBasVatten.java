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

    private Double filterlängd;
    private String grundvattenmagasin;
    private Boolean igengjuten;
    private Double installationsdjup;
    private Double marknivå;
    private Double maxPejlbartDjup;
    private String rördimension;
    private Integer rörlutningsriktning;
    private String rörtyp;
    private Double spetsnivå;

    public BBasVatten() {
    }

    public Double getFilterlängd() {
        return filterlängd;
    }

    public String getGrundvattenmagasin() {
        return grundvattenmagasin;
    }

    public Boolean getIgengjuten() {
        return igengjuten;
    }

    public Double getInstallationsdjup() {
        return installationsdjup;
    }

    public Double getMarknivå() {
        return marknivå;
    }

    public Double getMaxPejlbartDjup() {
        return maxPejlbartDjup;
    }

    public String getRördimension() {
        return rördimension;
    }

    public Integer getRörlutningsriktning() {
        return rörlutningsriktning;
    }

    public String getRörtyp() {
        return rörtyp;
    }

    public Double getSpetsnivå() {
        return spetsnivå;
    }

    public void setFilterlängd(Double filterlängd) {
        this.filterlängd = filterlängd;
    }

    public void setGrundvattenmagasin(String grundvattenmagasin) {
        this.grundvattenmagasin = grundvattenmagasin;
    }

    public void setIgengjuten(Boolean igengjuten) {
        this.igengjuten = igengjuten;
    }

    public void setInstallationsdjup(Double installationsdjup) {
        this.installationsdjup = installationsdjup;
    }

    public void setMarknivå(Double marknivå) {
        this.marknivå = marknivå;
    }

    public void setMaxPejlbartDjup(Double maxPejlbartDjup) {
        this.maxPejlbartDjup = maxPejlbartDjup;
    }

    public void setRördimension(String rördimension) {
        this.rördimension = rördimension;
    }

    public void setRörlutningsriktning(Integer rörlutningsriktning) {
        this.rörlutningsriktning = rörlutningsriktning;
    }

    public void setRörtyp(String rörtyp) {
        this.rörtyp = rörtyp;
    }

    public void setSpetsnivå(Double spetsnivå) {
        this.spetsnivå = spetsnivå;
    }

}
