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
    "nivå"
})
public class BGrundvattenObservation extends BBasePointObservation {

    private Ext mExt;
    private Double nivå;

    public BGrundvattenObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public Double getNivå() {
        return nivå;
    }

    public void setNivå(Double nivå) {
        this.nivå = nivå;
    }

    public class Ext {

        private BGrundvatten mParent;

        public BGrundvatten getParent() {
            return mParent;
        }

        public void setParent(BGrundvatten p) {
            mParent = p;
        }

    }
}
