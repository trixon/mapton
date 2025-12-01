/*
 * Copyright 2025 Patrik Karlström.
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
package org.mapton.butterfly_format.types.remote;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.mapton.butterfly_format.types.BXyzPointObservation;

/**
 *
 * @author Patrik Karlström
 */
@JsonIgnoreProperties(value = {"replacementMeasurement", "zeroMeasurement"})

public class BInsarPointObservation extends BXyzPointObservation {

    private transient boolean replacementMeasurement;
    private transient boolean zeroMeasurement;

}
