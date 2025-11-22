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
package org.mapton.api.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public abstract class CustomSerializerDecimals extends JsonSerializer<Double> {

    public static DecimalFormatSymbols sDecimalFormatSymbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

    abstract DecimalFormat getDecimalFormat();

    @Override
    public void serialize(Double value, JsonGenerator generator, SerializerProvider serializers) throws IOException {
        generator.writeNumber(Double.parseDouble(getDecimalFormat().format(value)));
    }

    public static class CustomSerializer3Decimals extends CustomSerializerDecimals {

        private static final DecimalFormat decimalFormat = new DecimalFormat("#.###", sDecimalFormatSymbols);

        @Override
        DecimalFormat getDecimalFormat() {
            return decimalFormat;
        }

    }

    public static class CustomSerializer6Decimals extends CustomSerializerDecimals {

        private static final DecimalFormat decimalFormat = new DecimalFormat("#.######", sDecimalFormatSymbols);

        @Override
        DecimalFormat getDecimalFormat() {
            return decimalFormat;
        }

    }
}
