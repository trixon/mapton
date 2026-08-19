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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvParser;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mapton.butterfly_format.ZipHelper;
import org.mapton.butterfly_format.jackson.DimensionDeserializer;
import org.mapton.butterfly_format.jackson.LocalDateTimeDeserializer;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class ImportFromCsv<T> {

    private static final CsvMapper SHARED_MAPPER;
    private static final ZipHelper ZIP_HELPER = ZipHelper.getInstance();

    private final Class<T> mClassOfT;
    private final CsvSchema mSchema;

    static {
        var simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());
        simpleModule.addDeserializer(BDimension.class, new DimensionDeserializer());

        SHARED_MAPPER = CsvMapper.builder()
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .enable(StreamReadFeature.USE_FAST_DOUBLE_PARSER)
                .enable(CsvParser.Feature.TRIM_SPACES)
                .addModule(new JavaTimeModule())
                .addModule(simpleModule)
                .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
                .disable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
                .build();

        SHARED_MAPPER.setVisibility(SHARED_MAPPER.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );
    }

    public ImportFromCsv(Class<T> clazz) {
        mClassOfT = clazz;
        mSchema = SHARED_MAPPER.schemaFor(mClassOfT)
                .withHeader()
                .withQuoteChar('"')
                .withColumnReordering(true);
    }

    public void load(File sourceDir, String path, ArrayList<T> list) {
        list.clear();

        try {
            if (sourceDir == null) {
                try (var inputStream = ZIP_HELPER.getStream(path)) {
                    if (inputStream != null) {
                        var mappingIterator = SHARED_MAPPER.readerFor(mClassOfT).with(mSchema).readValues(inputStream);
                        list.addAll((ArrayList<T>) mappingIterator.readAll());
                    }
                }
            } else {
                var file = new File(sourceDir, path);
                if (!file.isFile() || file.length() == 0) {
                    System.out.println("Missing source file, or file is empty: " + file);
                    return;
                }
                var mappingIterator = SHARED_MAPPER
                        .readerFor(mClassOfT)
                        .with(mSchema)
                        .readValues(file);
                list.addAll((ArrayList<T>) mappingIterator.readAll());
            }
        } catch (IOException ex) {
            Logger.getLogger(ImportFromCsv.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
