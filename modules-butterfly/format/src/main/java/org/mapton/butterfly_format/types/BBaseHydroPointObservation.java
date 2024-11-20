/*
 * Copyright 2024 Patrik Karlström.
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
package org.mapton.butterfly_format.types;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 *
 * @author Patrik Karlström
 */
public class BBaseHydroPointObservation extends BBaseControlPointObservation {

    @JsonIgnore
    private Ext mExt;

    public BBaseHydroPointObservation() {
    }

    public Ext ext() {
        if (mExt == null) {
            mExt = new Ext();
        }

        return mExt;
    }

    public class Ext<T> {

        private T mParent;

        public Ext() {
        }

        public T getParent() {
            return mParent;
        }

        public void setParent(T parent) {
            this.mParent = parent;
        }

    }
}
