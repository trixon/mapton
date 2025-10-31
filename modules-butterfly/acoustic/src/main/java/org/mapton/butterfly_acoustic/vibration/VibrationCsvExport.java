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
package org.mapton.butterfly_acoustic.vibration;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.mapton.butterfly_format.jackson.LocalDateTimeSerializer;
import org.mapton.core.api.ui.ExportConfiguration;
import org.mapton.core.api.ui.ExportProvider;
import org.openide.util.Exceptions;
import org.openide.util.lookup.ServiceProvider;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlström
 */
@ServiceProvider(service = ExportProvider.class)
public class VibrationCsvExport extends ExportProvider {

    private final VibrationManager mManager = VibrationManager.getInstance();

    public VibrationCsvExport() {
        super("Vibrationer");
        setSupportsEncoding(true);
        setSupportsTransformation(false);
    }

    @Override
    public void export(ExportConfiguration exportConfiguration) {
        var pointsToExport = mManager.getTimeFilteredItems().stream()
                .flatMap(p -> p.ext().getObservationsTimeFiltered().stream()
                .map(o -> new Row(p.getName(), o.getDate(), o.getMeasuredZ(), o.getLimit())))
                .toList();
        try {
            var file = exportConfiguration.getFile();
            writeCsv(file, Row.class, pointsToExport);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @Override
    public FileNameExtensionFilter getExtensionFilter() {
        return SimpleDialog.getExtensionFilters().get("csv");
    }

    @Override
    public String getName() {
        return "CSV-Projektnav";
    }

    private void writeCsv(File file, Class clazz, List list) throws IOException {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        var mapper = CsvMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(simpleModule)
                .build();

        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        var schema = mapper.schemaFor(clazz).withHeader().withColumnSeparator('\t').withoutQuoteChar();
        mapper.writerFor(clazz).with(schema).writeValues(file).writeAll(list);
    }

    record Row(String point, LocalDateTime mDate, Double z, Double zLimit) {

    }

}
