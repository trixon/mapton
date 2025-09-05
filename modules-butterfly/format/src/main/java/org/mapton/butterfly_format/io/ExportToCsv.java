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
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvGenerator;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Logger;
import org.mapton.butterfly_format.jackson.DimensionSerializer;
import org.mapton.butterfly_format.jackson.LocalDateTimeSerializer;
import org.mapton.butterfly_format.types.BDimension;

/**
 *
 * @author Patrik Karlström <patrik@trixon.se>
 */
public abstract class ExportToCsv {

    public ExportToCsv() {
    }

    public Logger getLogger() {
        return Logger.getLogger(getClass().getName());
    }

    public void writeCsv(File destDir, String basename, Class clazz, List list) throws IOException {
        var simpleModule = new SimpleModule();
        simpleModule.addSerializer(BDimension.class, new DimensionSerializer());
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer());

        var mapper = CsvMapper.builder()
                .addModule(new JavaTimeModule())
                .addModule(simpleModule)
                .enable(CsvGenerator.Feature.ALWAYS_QUOTE_STRINGS)
                .build();

        mapper.setVisibility(mapper.getSerializationConfig()
                .getDefaultVisibilityChecker()
                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
        );

        mapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

        var schema = mapper.schemaFor(clazz).withHeader().withQuoteChar('"');
        var file = new File(destDir, basename + ".csv");
        mapper.writerFor(clazz).with(schema).writeValues(file).writeAll(list);
        getLogger().info("%d items exported".formatted(list.size()));
    }
}
