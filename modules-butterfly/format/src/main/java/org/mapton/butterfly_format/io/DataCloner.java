/*
 * Copyright 2026 Patrik Karlström.
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
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDateTime;
import org.mapton.butterfly_format.jackson.LocalDateTimeDeserializer;
import org.openide.util.Exceptions;

/**
 *
 * @author Patrik Karlström
 */
public class DataCloner {

    private static final ObjectMapper MAPPER;

    static {
        var simpleModule = new SimpleModule();
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer());

        MAPPER = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .registerModule(simpleModule)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
                .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    }

    public <Target extends Source, Source> Target convert(Target target, Source source) {
        if (source == null) {
            return null;
        }

        try {
            return MAPPER.updateValue(target, source);
        } catch (JsonMappingException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }
}
