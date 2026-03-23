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
package org.mapton.butterfly_meteo.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_format.types.BMeteoPointObservation;

/**
 *
 * @author Patrik Karlström
 */
public class StandardMeasurementPopulator extends StandardMeasurementBasePopulator<BMeteoPoint> {

    public StandardMeasurementPopulator() {
    }

    @Override
    public void populate(BMeteoPoint p) {
        initDecimals(airTemperatureColumn, 1, true);
        initDecimals(airPreasureColumn, 1, false);
        initDecimals(relHumidity, 0, false);
        initDecimals(preciptationColumn, 1, false);
        initDecimals(windSpeedColumn, 1, false);
        initDecimals(windSpeedMaxColumn, 1, false);

        var columns = mTableView.getColumns();
        columns.setAll(dateColumn,
                airTemperatureColumn,
                airPreasureColumn,
                relHumidity,
                preciptationColumn,
                windDirectionColumn,
                windSpeedColumn,
                windSpeedMaxColumn,
                visibilityColumn,
                weatherCodeColumn
        );

        ObservableList<StandardMeasurementRowXyz> rows = FXCollections.observableArrayList();
        var ext = p.extOrNull();

        for (var o : ext.getObservationsTimeFiltered().reversed()) {
            var oo = (BMeteoPointObservation) o;
            rows.add(new StandardMeasurementRowXyz(
                    oo.getDate(),
                    oo.getAirTemperature(),
                    oo.getAirPressure(),
                    oo.getHumidity(),
                    oo.getPrecipitation(),
                    oo.getWindDirection(),
                    oo.getWindSpeed(),
                    oo.getWindSpeedMax(),
                    oo.getVisibility(),
                    oo.getWeatherCode()
            ));
        }

        mTableView.setItems(rows);
        mTableView.scrollTo(0);
    }
}
