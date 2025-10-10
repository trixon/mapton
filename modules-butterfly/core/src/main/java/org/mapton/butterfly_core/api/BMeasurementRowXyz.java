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
package org.mapton.butterfly_core.api;

import java.time.LocalDateTime;

/**
 *
 * @author Patrik Karlström
 */
public record BMeasurementRowXyz(
        LocalDateTime date,
        Double n,
        Double e,
        Double h,
        Double delta1d,
        Double delta2d,
        Integer percent1d,
        Integer percent2d,
        int alarmLevel1d,
        int alarmLevel2d,
        String instrument,
        String oprerator,
        String comment,
        boolean codeReplacement,
        boolean codeZero
        ) {

}
