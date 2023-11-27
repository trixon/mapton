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

    private String mAnläggningstyp;
    private Integer mBredd;
    private String mDelsträcka;
    private Integer mLängd;
    private String mPumpgropstyp;
    private String mTätningstyp;

    public BTunnelvatten() {
    }

    public String getAnläggningstyp() {
        return mAnläggningstyp;
    }

    public Integer getBredd() {
        return mBredd;
    }

    public String getDelsträcka() {
        return mDelsträcka;
    }

    public Integer getLängd() {
        return mLängd;
    }

    public String getPumpgropstyp() {
        return mPumpgropstyp;
    }

    public String getTätningstyp() {
        return mTätningstyp;
    }

    public void setAnläggningstyp(String anläggningstyp) {
        this.mAnläggningstyp = anläggningstyp;
    }

    public void setBredd(Integer bredd) {
        this.mBredd = bredd;
    }

    public void setDelsträcka(String delsträcka) {
        this.mDelsträcka = delsträcka;
    }

    public void setLängd(Integer längd) {
        this.mLängd = längd;
    }

    public void setPumpgropstyp(String pumpgropstyp) {
        this.mPumpgropstyp = pumpgropstyp;
    }

    public void setTätningstyp(String tätningstyp) {
        this.mTätningstyp = tätningstyp;
    }
}
