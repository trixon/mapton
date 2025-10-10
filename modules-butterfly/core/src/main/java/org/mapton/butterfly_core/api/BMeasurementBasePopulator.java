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

import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.paint.Color;
import org.mapton.butterfly_format.types.BXyzPoint;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class BMeasurementBasePopulator<T extends BXyzPoint> {

    protected final TableColumn<BMeasurementRowXyz, String> commentColumn = new TableColumn<>("Kommentar");
    protected final TableColumn<BMeasurementRowXyz, String> dateColumn = new TableColumn<>(Dict.DATE.toString());
    protected final TableColumn<BMeasurementRowXyz, Double> delta1dColumn = new TableColumn<>("Δ1d");
    protected final TableColumn<BMeasurementRowXyz, Double> delta2dColumn = new TableColumn<>("Δ2d");
    protected final TableColumn<BMeasurementRowXyz, String> instrumentColumn = new TableColumn<>("Instrument");
    protected final TableView<BMeasurementRowXyz> mTableView = new TableView<>();
    protected final TableColumn<BMeasurementRowXyz, String> operatorColumn = new TableColumn<>("Operatör");
    protected final TableColumn<BMeasurementRowXyz, Integer> percent1dColumn = new TableColumn<>("% 1d");
    protected final TableColumn<BMeasurementRowXyz, Integer> percent2dColumn = new TableColumn<>("% 2d");

    public BMeasurementBasePopulator() {
        dateColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        delta1dColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().delta1d()));
        delta2dColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().delta2d()));
        commentColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().comment()));
        instrumentColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().instrument()));
        operatorColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().oprerator()));
        percent1dColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().percent1d()));
        percent2dColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().percent2d()));

        mTableView.setStyle("-fx-font-family: monospace;");
        mTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initDecimals(delta1dColumn, 3, true);
        initDecimals(delta2dColumn, 3, false);
        initContextMenu();
    }

    public TableView getTableView() {
        return mTableView;
    }

    public void initColorByCode(TableColumn<BMeasurementRowXyz, String> column) {
        column.setCellFactory(cell -> new TableCell<BMeasurementRowXyz, String>() {
            @Override
            protected void updateItem(String value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                    setStyle("");
                } else {
                    var row = getTableView().getItems().get(getIndex());
                    if (row.codeReplacement()) {
                        setTextFill(Color.BLACK);
                        setStyle("-fx-background-color: magenta;");
                    } else if (row.codeZero()) {
                        setTextFill(Color.BLACK);
                        setStyle("-fx-background-color: cyan;");
                    } else {
                        setTextFill(Color.LIGHTGREY);
                        setStyle("-fx-background-color: transparent;");

                    }

                    setText(value);
                }
            }
        });
    }

    public void initDecimals(TableColumn<BMeasurementRowXyz, Double> column, int decimals, boolean forceSign) {
        var format = "%%%s.%df".formatted(forceSign ? "+" : "", decimals);
        column.setCellFactory(cell -> new TableCell<BMeasurementRowXyz, Double>() {
            @Override
            protected void updateItem(Double value, boolean empty) {
                super.updateItem(value, empty);
                if (empty || value == null) {
                    setText(null);
                } else {
                    setText(format.formatted(value));
                }
            }
        });
    }

    public abstract void populate(T p);

    private void copyAllRows() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void copySelectedRows() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    private void initContextMenu() {
        var contextMenu = new ContextMenu();

        var copySelected = new MenuItem("Kopiera urval");
        copySelected.setOnAction(e -> copySelectedRows());

        var copyAll = new MenuItem("Kopiera alla");
        copyAll.setOnAction(e -> copyAllRows());

        contextMenu.getItems().addAll(copySelected, copyAll);
        mTableView.setOnMouseClicked(event -> {
            if (event.isPopupTrigger() || (event.getButton() == MouseButton.SECONDARY)) {
                contextMenu.show(mTableView, event.getScreenX(), event.getScreenY());
            }
        });
    }
}
