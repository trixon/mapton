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

import java.time.format.DateTimeFormatter;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.MouseButton;
import org.mapton.butterfly_format.types.BMeteoPoint;
import org.mapton.butterfly_meteo.MeteoHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlström
 */
public abstract class StandardMeasurementBasePopulator<T extends BMeteoPoint> {

    protected final TableColumn<StandardMeasurementRowXyz, Double> airPreasureColumn = new TableColumn<>("Lufttryck");
    protected final TableColumn<StandardMeasurementRowXyz, Double> airTemperatureColumn = new TableColumn<>("Temperatur");
    protected final TableColumn<StandardMeasurementRowXyz, String> dateColumn = new TableColumn<>(Dict.DATE.toString());
    protected final TableView<StandardMeasurementRowXyz> mTableView = new TableView<>();
    protected final TableColumn<StandardMeasurementRowXyz, Double> preciptationColumn = new TableColumn<>("Nederbörd");
    protected final TableColumn<StandardMeasurementRowXyz, Double> relHumidity = new TableColumn<>("Luftfukt");
    protected final TableColumn<StandardMeasurementRowXyz, Integer> visibilityColumn = new TableColumn<>("Sikt");
    protected final TableColumn<StandardMeasurementRowXyz, String> weatherCodeColumn = new TableColumn<>("Väderlek");
    protected final TableColumn<StandardMeasurementRowXyz, Integer> windDirectionColumn = new TableColumn<>("Vindriktning");
    protected final TableColumn<StandardMeasurementRowXyz, Double> windSpeedColumn = new TableColumn<>("Vindstyrka");
    protected final TableColumn<StandardMeasurementRowXyz, Double> windSpeedMaxColumn = new TableColumn<>("Vindstyrka max");

    public StandardMeasurementBasePopulator() {
        dateColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().date().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))));
        airTemperatureColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().airTemperature()));
        airPreasureColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().airPreasure()));
        relHumidity.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().relHumidity()));
        preciptationColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().preciptation()));
        windDirectionColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().windDirection()));
        windSpeedColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().windSpeed()));
        windSpeedMaxColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().windSpeedMax()));
        visibilityColumn.setCellValueFactory(r -> new SimpleObjectProperty(r.getValue().visibility()));
//        weatherCodeColumn.setCellValueFactory(r -> new SimpleStringProperty(r.getValue().weatherCode()));
        weatherCodeColumn.setCellValueFactory(r -> new SimpleStringProperty(MeteoHelper.getMeteoCode(r.getValue().weatherCode())));

        mTableView.setStyle("-fx-font-family: monospace;");
        mTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        initContextMenu();
    }

    public TableView getTableView() {
        return mTableView;
    }

    public void initDecimals(TableColumn<StandardMeasurementRowXyz, Double> column, int decimals, boolean forceSign) {
        var format = "%%%s.%df".formatted(forceSign ? "+" : "", decimals);
        column.setCellFactory(cell -> new TableCell<StandardMeasurementRowXyz, Double>() {
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

    private void copy(ObservableList<StandardMeasurementRowXyz> rows) {
        var sb = new StringBuilder();
        for (var column : mTableView.getColumns()) {
            sb.append(column.getText()).append("\t");
        }
        sb.setLength(sb.length() - 1);
        sb.append("\n");

        var clipboard = Clipboard.getSystemClipboard();
        var content = new ClipboardContent();
        content.putString(sb.toString().trim());
        clipboard.setContent(content);

    }

    private void copyAllRows() {
        copy(mTableView.getItems());
    }

    private void copySelectedRows() {
        copy(mTableView.getSelectionModel().getSelectedItems());
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
