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
import java.time.LocalDate;
import org.mapton.butterfly_format.types.BBasePoint;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BBasObjekt extends BBasePoint {

    private String anmärkning;
    private String benämning;
    private String dubbnamn;
    private String gammalt_id;
    private String höjd;
    private String informationskällor;
    private LocalDate installationsdatum;
    private LocalDate inventeringsdatum;
    private String kontrollprogram;
    private String koordinatkvalitet;
    private String lägesbeskrivning;
    private String mätningstyper;
    private String mätpunktstyp;
    private String plan;
    private String status;
    private LocalDate versionsdatum;
    private Double x;
    private Double y;

    public BBasObjekt() {
    }

    public String getAnmärkning() {
        return anmärkning;
    }

    public String getBenämning() {
        return benämning;
    }

    @Override
    public String getComment() {
        return getAnmärkning();
    }

    public String getDubbnamn() {
        return dubbnamn;
    }

    public String getGammalt_id() {
        return gammalt_id;
    }

    public String getHöjd() {
        return höjd;
    }

    public String getInformationskällor() {
        return informationskällor;
    }

    public LocalDate getInstallationsdatum() {
        return installationsdatum;
    }

    public LocalDate getInventeringsdatum() {
        return inventeringsdatum;
    }

    public String getKontrollprogram() {
        return kontrollprogram;
    }

    public String getKoordinatkvalitet() {
        return koordinatkvalitet;
    }

    public String getLägesbeskrivning() {
        return lägesbeskrivning;
    }

    @JsonIgnore
    @Override
    public String getMeta() {
        return super.getMeta();
    }

    public String getMätningstyper() {
        return mätningstyper;
    }

    public String getMätpunktstyp() {
        return mätpunktstyp;
    }

    @Override
    public String getName() {
        return getBenämning();
    }

    public String getPlan() {
        return plan;
    }

    public String getStatus() {
        return status;
    }

    public LocalDate getVersionsdatum() {
        return versionsdatum;
    }

    public Double getX() {
        return x;
    }

    public Double getY() {
        return y;
    }

    public void setAnmärkning(String anmärkning) {
        this.anmärkning = anmärkning;
    }

    public void setBenämning(String benämning) {
        this.benämning = benämning;
    }

    public void setDubbnamn(String dubbnamn) {
        this.dubbnamn = dubbnamn;
    }

    public void setGammalt_id(String gammalt_id) {
        this.gammalt_id = gammalt_id;
    }

    public void setHöjd(String höjd) {
        this.höjd = höjd;
    }

    public void setInformationskällor(String informationskällor) {
        this.informationskällor = informationskällor;
    }

    public void setInstallationsdatum(LocalDate installationsdatum) {
        this.installationsdatum = installationsdatum;
    }

    public void setInventeringsdatum(LocalDate inventeringsdatum) {
        this.inventeringsdatum = inventeringsdatum;
    }

    public void setKontrollprogram(String kontrollprogram) {
        this.kontrollprogram = kontrollprogram;
    }

    public void setKoordinatkvalitet(String koordinatkvalitet) {
        this.koordinatkvalitet = koordinatkvalitet;
    }

    public void setLägesbeskrivning(String lägesbeskrivning) {
        this.lägesbeskrivning = lägesbeskrivning;
    }

    public void setMätningstyper(String mätningstyper) {
        this.mätningstyper = mätningstyper;
    }

    public void setMätpunktstyp(String mätpunktstyp) {
        this.mätpunktstyp = mätpunktstyp;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setVersionsdatum(LocalDate versionsdatum) {
        this.versionsdatum = versionsdatum;
    }

    public void setX(Double x) {
        this.x = x;
    }

    public void setY(Double y) {
        this.y = y;
    }
}
