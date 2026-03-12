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

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.mapton.butterfly_format.types.BBasePointObservation;

/**
 *
 * @author Patrik Karlström
 */
@JsonPropertyOrder({
    "name",
    "date",
    "nivå",
    "granskning"
})
public class BInfiltrationObservation extends BBasePointObservation {

    private String granskning;
    private transient Ext mExt;
    private Double value;

    public BInfiltrationObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public String getGranskning() {
        return granskning;
    }

    public Double getValue() {
        return value;
    }

    public void setGranskning(String granskning) {
        this.granskning = granskning;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public class Ext {

        private BInfiltration mParent;

        public BInfiltration getParent() {
            return mParent;
        }

        public void setParent(BInfiltration p) {
            mParent = p;
        }

    }
}
