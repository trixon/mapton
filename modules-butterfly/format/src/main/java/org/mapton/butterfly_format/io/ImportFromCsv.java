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
package org.mapton.butterfly_format.io;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mapton.butterfly_format.jackson.DimensionDeserializer;
import org.mapton.butterfly_format.jackson.LocalDateTimeDeserializer;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class ImportFromCsv<T> {

    private final Class<T> classOfT;
    private final ArrayList<T> items = new ArrayList<>();

    public ImportFromCsv(Class<T> clazz) {
        classOfT = clazz;
    }

    public void load(File file, ArrayList<T> list) {
        if (!file.isFile()) {
            System.out.println("Missing source file: " + file);
            return;
        }
        try {
            list.clear();

            var simpleModule = new SimpleModule();
            simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
            simpleModule.addDeserializer(BDimension.class, new DimensionDeserializer());

            var mapper = CsvMapper.builder()
                    .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                    .addModule(new JavaTimeModule())
                    .addModule(simpleModule)
                    .build();

            var schema = mapper.schemaFor(classOfT).withHeader().withQuoteChar('"');
            var mappingIterator = mapper.readerFor(classOfT)
                    .with(schema)
                    .readValues(file);

            @SuppressWarnings("unchecked")
            var listFromJson = (ArrayList<T>) mappingIterator.readAll();
            list.addAll(listFromJson);
        } catch (IOException ex) {
            Logger.getLogger(ImportFromCsv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
