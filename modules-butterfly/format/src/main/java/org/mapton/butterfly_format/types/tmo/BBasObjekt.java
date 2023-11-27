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

import java.time.LocalDate;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasObjekt extends BBasePoint {

    private String mAnmärkning;
    private String mBenämning;
    private String mDubbnamn;
    private String mGammalt_id;
    private String mHöjd;
    private String mInformationskällor;
    private LocalDate mInstallationsdatum;
    private LocalDate mInventeringsdatum;
    private String mKontrollprogram;
    private String mKoordinatkvalitet;
    private String mLägesbeskrivning;
    private String mMätningstyper;
    private String mMätpunktstyp;
    private String mPlan;
    private String mStatus;
    private LocalDate mVersionsdatum;
    private Double mX;
    private Double mY;

    public BBasObjekt() {
    }

    public String getAnmärkning() {
        return mAnmärkning;
    }

    public String getBenämning() {
        return mBenämning;
    }

    @Override
    public String getComment() {
        return getAnmärkning();
    }

    public String getDubbnamn() {
        return mDubbnamn;
    }

    public String getGammalt_id() {
        return mGammalt_id;
    }

    public String getHöjd() {
        return mHöjd;
    }

    public String getInformationskällor() {
        return mInformationskällor;
    }

    public LocalDate getInstallationsdatum() {
        return mInstallationsdatum;
    }

    public LocalDate getInventeringsdatum() {
        return mInventeringsdatum;
    }

    public String getKontrollprogram() {
        return mKontrollprogram;
    }

    public String getKoordinatkvalitet() {
        return mKoordinatkvalitet;
    }

    public String getLägesbeskrivning() {
        return mLägesbeskrivning;
    }

    public String getMätningstyper() {
        return mMätningstyper;
    }

    public String getMätpunktstyp() {
        return mMätpunktstyp;
    }

    @Override
    public String getName() {
        return getBenämning();
    }

    public String getPlan() {
        return mPlan;
    }

    public String getStatus() {
        return mStatus;
    }

    public LocalDate getVersionsdatum() {
        return mVersionsdatum;
    }

    public Double getX() {
        return mX;
    }

    public Double getY() {
        return mY;
    }

    public void setAnmärkning(String anmärkning) {
        this.mAnmärkning = anmärkning;
    }

    public void setBenämning(String benämning) {
        this.mBenämning = benämning;
    }

    public void setDubbnamn(String dubbnamn) {
        this.mDubbnamn = dubbnamn;
    }

    public void setGammalt_id(String gammalt_id) {
        this.mGammalt_id = gammalt_id;
    }

    public void setHöjd(String höjd) {
        this.mHöjd = höjd;
    }

    public void setInformationskällor(String informationskällor) {
        this.mInformationskällor = informationskällor;
    }

    public void setInstallationsdatum(LocalDate installationsdatum) {
        this.mInstallationsdatum = installationsdatum;
    }

    public void setInventeringsdatum(LocalDate inventeringsdatum) {
        this.mInventeringsdatum = inventeringsdatum;
    }

    public void setKontrollprogram(String kontrollprogram) {
        this.mKontrollprogram = kontrollprogram;
    }

    public void setKoordinatkvalitet(String koordinatkvalitet) {
        this.mKoordinatkvalitet = koordinatkvalitet;
    }

    public void setLägesbeskrivning(String lägesbeskrivning) {
        this.mLägesbeskrivning = lägesbeskrivning;
    }

    public void setMätningstyper(String mätningstyper) {
        this.mMätningstyper = mätningstyper;
    }

    public void setMätpunktstyp(String mätpunktstyp) {
        this.mMätpunktstyp = mätpunktstyp;
    }

    public void setPlan(String plan) {
        this.mPlan = plan;
    }

    public void setStatus(String status) {
        this.mStatus = status;
    }

    public void setVersionsdatum(LocalDate versionsdatum) {
        this.mVersionsdatum = versionsdatum;
    }

    public void setX(Double x) {
        this.mX = x;
    }

    public void setY(Double y) {
        this.mY = y;
    }
}
