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
package org.mapton.butterfly_topo.table;

import java.util.function.Function;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.paint.Color;
import org.mapton.butterfly_core.api.BMeasurementBasePopulator;
import org.mapton.butterfly_core.api.BMeasurementRowXyz;
import org.mapton.butterfly_format.types.BComponent;
import org.mapton.butterfly_format.types.BDimension;
import org.mapton.butterfly_format.types.topo.BTopoControlPoint;

/**
 *
 * @author Patrik Karlström
 */
public class StandardMeasurementPopulator extends BMeasurementBasePopulator<BTopoControlPoint> {

    private final TableColumn<BMeasurementRowXyz, Double> eColumn = new TableColumn<>("E");
    private final TableColumn<BMeasurementRowXyz, Double> hColumn = new TableColumn<>("H");
    private final TableColumn<BMeasurementRowXyz, Double> nColumn = new TableColumn<>("N");

    public StandardMeasurementPopulator() {
        eColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().e()));
        hColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().h()));
        nColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().n()));
    }

    @Override
    public void populate(BTopoControlPoint p) {
        initDecimals(nColumn, 3, false);
        initDecimals(eColumn, 3, false);
        initDecimals(hColumn, 3, false);

        initColorByAlarmLevel(percent1dColumn, r -> r.alarmLevel1d());
        initColorByAlarmLevel(percent2dColumn, r -> r.alarmLevel2d());
        initColorByCode(dateColumn);

        var columns = mTableView.getColumns();
        columns.clear();
        columns.add(dateColumn);

        var dimension = p.getDimension();
        if (dimension != BDimension._1d) {
            columns.addAll(
                    nColumn,
                    eColumn
            );
        }

        if (dimension != BDimension._2d) {
            columns.add(hColumn);
        }

        if (dimension != BDimension._2d) {
            columns.add(delta1dColumn);
        }

        if (dimension != BDimension._1d) {
            columns.add(delta2dColumn);
        }

        if (dimension != BDimension._2d) {
            columns.add(percent1dColumn);
        }

        if (dimension != BDimension._1d) {
            columns.add(percent2dColumn);
        }

        columns.addAll(
                instrumentColumn,
                operatorColumn,
                commentColumn
        );

        ObservableList<BMeasurementRowXyz> rows = FXCollections.observableArrayList();
        var ext = p.extOrNull();

        for (var o : ext.getObservationsTimeFiltered().reversed()) {
            rows.add(new BMeasurementRowXyz(
                    o.getDate(),
                    o.getMeasuredY(),
                    o.getMeasuredX(),
                    o.getMeasuredZ(),
                    o.ext().getDelta1d(),
                    o.ext().getDelta2d(),
                    ext.getAlarmPercent(BComponent.HEIGHT, o.ext().getDelta1d()),
                    ext.getAlarmPercent(BComponent.PLANE, o.ext().getDelta2d()),
                    ext.getAlarmLevel(BComponent.HEIGHT, o),
                    ext.getAlarmLevel(BComponent.PLANE, o),
                    o.getInstrument(),
                    o.getOperator(),
                    o.getComment(),
                    o.isReplacementMeasurement(),
                    o.isZeroMeasurement()
            ));
        }

        mTableView.setItems(rows);
        mTableView.scrollTo(0);
    }

    private void initColorByAlarmLevel(TableColumn<BMeasurementRowXyz, Integer> column, Function<BMeasurementRowXyz, Integer> function) {
        column.setCellFactory(cell -> new TableCell<BMeasurementRowXyz, Integer>() {
            @Override
            protected void updateItem(Integer value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    var row = getTableView().getItems().get(getIndex());
                    var alarmLevel = function.apply(row);
                    if (alarmLevel >= 2) {
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: red;");
                    } else if (alarmLevel == 1) {
                        setTextFill(Color.BLACK);
                        setStyle("-fx-background-color: yellow;");
                    } else if (alarmLevel == 0) {
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: green;");
                    } else {
                        setTextFill(Color.WHITE);
                        setStyle("-fx-background-color: blue;");
                    }
                    setText(value.toString());
                }
            }
        });
    }

}
