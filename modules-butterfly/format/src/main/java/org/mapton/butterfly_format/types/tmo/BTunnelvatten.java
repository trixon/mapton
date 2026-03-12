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
public class BTunnelvatten extends BBasObjekt {

    private String anläggningstyp;
    private Integer bredd;
    private String delsträcka;
    private Integer längd;
    private String pumpgropstyp;
    private String tätningstyp;

    public BTunnelvatten() {
    }

    public String getAnläggningstyp() {
        return anläggningstyp;
    }

    public Integer getBredd() {
        return bredd;
    }

    public String getDelsträcka() {
        return delsträcka;
    }

    public Integer getLängd() {
        return längd;
    }

    public String getPumpgropstyp() {
        return pumpgropstyp;
    }

    public String getTätningstyp() {
        return tätningstyp;
    }

    public void setAnläggningstyp(String anläggningstyp) {
        this.anläggningstyp = anläggningstyp;
    }

    public void setBredd(Integer bredd) {
        this.bredd = bredd;
    }

    public void setDelsträcka(String delsträcka) {
        this.delsträcka = delsträcka;
    }

    public void setLängd(Integer längd) {
        this.längd = längd;
    }

    public void setPumpgropstyp(String pumpgropstyp) {
        this.pumpgropstyp = pumpgropstyp;
    }

    public void setTätningstyp(String tätningstyp) {
        this.tätningstyp = tätningstyp;
    }
}
