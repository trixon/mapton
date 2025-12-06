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
package org.mapton.butterfly_format;

import java.time.LocalDate;
import javafx.collections.ObservableList;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.DateHelper;

/**
 *
 * @author Patrik Karlström
 */
public class ButterflyManipulator {

    public void updateMultipleObservationsPerDay(ObservableList<? extends BXyzPoint> points) {
        var now = LocalDate.now();
        points.forEach(p -> {
            var observations = p.extOrNull().getObservationsAllRaw();
            var numOfRecentDays = 28;
            var numOfRecentObservations = observations.stream()
                    .filter(o -> DateHelper.isAfterOrEqual(o.getDate().toLocalDate(), now.minusDays(numOfRecentDays)))
                    .count();
            var multipleObservationsPerDay = numOfRecentObservations > numOfRecentDays;
            p.extOrNull().setMultipleObservationsPerDay(multipleObservationsPerDay);
        });
    }

}
