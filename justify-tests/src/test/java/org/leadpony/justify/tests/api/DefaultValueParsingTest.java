/*
 * Copyright 2018-2019 the Justify authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.leadpony.justify.tests.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

import java.io.StringReader;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.json.Json;
import jakarta.json.JsonNumber;
import jakarta.json.JsonReader;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser;

import org.junit.jupiter.api.Test;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.api.ValidationConfig;
import org.leadpony.justify.tests.helper.ApiTest;

/**
 * A test type for testing of parsing default values.
 *
 * @author leadpony
 */
@ApiTest
public class DefaultValueParsingTest {

    private static Logger log;
    private static JsonValidationService service;

    @Test
    public void getStringShouldReturnString() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getString()).isEqualTo("hello");
        }
    }

    @Test
    public void getStringShouldReturnKeyName() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();

            assertThat(parser.getString()).isEqualTo("a");
        }
    }

    @Test
    public void getStringShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": {} } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getString();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void isIntegralNumberShouldReturnTrue() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 365 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.isIntegralNumber()).isTrue();
        }
    }

    @Test
    public void isIntegralNumberShouldReturnFalse() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 3.14 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.isIntegralNumber()).isFalse();
        }
    }

    @Test
    public void isIntegralNumberShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.isIntegralNumber();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getIntShouldReturnInteger() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 365 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getInt()).isEqualTo(365);
        }
    }

    @Test
    public void getIntShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getInt();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getLongShouldReturnLongInteger() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 9223372036854775807 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getLong()).isEqualTo(9223372036854775807L);
        }
    }

    @Test
    public void getLongShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getLong();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getBigDecimalShouldReturnBigDecimal() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 3.14 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            BigDecimal expected = new BigDecimal("3.14");
            assertThat(parser.getBigDecimal()).isEqualTo(expected);
        }
    }

    @Test
    public void getBigDecimalShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getBigDecimal();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getArrayShouldReturnJsonArray() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": [1,2,3] } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("[1,2,3]");
            assertThat(parser.getArray()).isEqualTo(expected);
        }
    }

    @Test
    public void getArrayShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": [1,2,3] } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getArray();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getObjectShouldReturnJsonObject() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": { \"greeting\" : \"hello\" } } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("{ \"greeting\" : \"hello\" }");
            assertThat(parser.getObject()).isEqualTo(expected);
        }
    }

    @Test
    public void getObjectShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": { \"greeting\" : \"hello\" } } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getObject();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getValueShouldReturnJsonObject() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": { \"greeting\" : \"hello\" } } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("{ \"greeting\" : \"hello\" }");
            assertThat(parser.getValue()).isEqualTo(expected);
        }
    }

    @Test
    public void getValueShouldReturnJsonArray() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": [1,2,3] } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("[1,2,3]");
            assertThat(parser.getValue()).isEqualTo(expected);
        }
    }

    @Test
    public void getValueShouldReturnJsonString() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("\"hello\"");
            JsonValue actual = parser.getValue();
            assertThat(actual).isEqualTo(expected);
            assertThat(((JsonString) actual).getString()).isEqualTo("hello");
        }
    }

    @Test
    public void getValueShouldReturnBigDecimalJsonNumber() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 3.14 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("3.14");
            JsonValue actual = parser.getValue();
            assertThat(actual).isEqualTo(expected);
            assertThat(((JsonNumber) actual).bigDecimalValue()).isEqualTo(new BigDecimal("3.14"));
        }
    }

    @Test
    public void getValueShouldReturnIntegerJsonNumber() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": 365 } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            JsonValue expected = readValue("365");
            JsonValue actual = parser.getValue();
            assertThat(actual).isEqualTo(expected);
            assertThat(((JsonNumber) actual).intValue()).isEqualTo(365);
        }
    }

    @Test
    public void getValueShouldReturnJsonTrue() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": true } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getValue()).isEqualTo(JsonValue.TRUE);
        }
    }

    @Test
    public void getValueShouldReturnJsonFalse() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": false } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getValue()).isEqualTo(JsonValue.FALSE);
        }
    }

    @Test
    public void getValueShouldReturnJsonNull() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": null } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            assertThat(parser.getValue()).isEqualTo(JsonValue.NULL);
        }
    }

    @Test
    public void getValueShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": {} } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getValue();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getArrayStreamShouldReturnFilledValues() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": [1,2,3] } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next();
            parser.next();
            parser.next();

            Stream<JsonValue> stream = parser.getArrayStream();
            assertThat(stream).containsExactlyInAnyOrder(
                    createValue(1), createValue(2), createValue(3));
        }
    }

    @Test
    public void getArrayStreamShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": [1,2,3] } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next(); // {
            parser.next(); // "a"

            Throwable thrown = catchThrowable(() -> {
                parser.getArrayStream();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getObjectStreamShouldReturnAllWithDefauleValue() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"first\": { \"default\": 1 } }"
                + "}");

        String json = "{ \"second\": 2, \"third\" : 3 }";
        try (JsonParser parser = createParser(json, schema)) {
            parser.next();

            Stream<Map.Entry<String, JsonValue>> stream = parser.getObjectStream();
            List<Map.Entry<String, JsonValue>> entries = stream.collect(Collectors.toList());
            assertThat(entries.get(0).getKey()).isEqualTo("second");
            assertThat(entries.get(0).getValue()).isEqualTo(createValue(2));
            assertThat(entries.get(1).getKey()).isEqualTo("third");
            assertThat(entries.get(1).getValue()).isEqualTo(createValue(3));
            assertThat(entries.get(2).getKey()).isEqualTo("first");
            assertThat(entries.get(2).getValue()).isEqualTo(createValue(1));
        }
    }

    @Test
    public void getObjectStreamShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"first\": { \"default\": 1 } }"
                + "}");

        String json = "{ \"second\": 2, \"third\" : 3 }";
        try (JsonParser parser = createParser(json, schema)) {
            parser.next();
            parser.next();

            Throwable thrown = catchThrowable(() -> {
                parser.getObjectStream();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void getValueStreamShouldThrowIllegalStateException() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": \"hello\" } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next(); // {

            Throwable thrown = catchThrowable(() -> {
                parser.getValueStream();
            });

            assertThat(thrown).isInstanceOf(IllegalStateException.class);
            log.info(thrown.getMessage());
        }
    }

    @Test
    public void skipAraryShouldSkipArrayEnclosingDefaultValues() {
        JsonSchema schema = readSchema("{"
                + "\"items\": {"
                + "\"properties\": { \"a\": { \"default\": [] } }"
                + "}"
                + "}");

        String json = "[{}, {}, {}]";

        try (JsonParser parser = createParser(json, schema)) {
            parser.next(); // [

            parser.skipArray();

            assertThat(parser.hasNext()).isFalse();
        }
    }

    @Test
    public void skipObjectShouldSkipObjectEnclosingDefaultValues() {
        JsonSchema schema = readSchema("{"
                + "\"properties\": { \"a\": { \"default\": {} } }"
                + "}");

        try (JsonParser parser = createParser("{}", schema)) {
            parser.next(); // {

            parser.skipObject();

            assertThat(parser.hasNext()).isFalse();
        }
    }

    private static JsonSchema readSchema(String json) {
        return service.readSchema(new StringReader(json));
    }

    private static JsonParser createParser(String json, JsonSchema schema) {
        ValidationConfig config = service.createValidationConfig()
                .withSchema(schema)
                .withDefaultValues(true);
        return service.createParserFactory(config.getAsMap())
                .createParser(new StringReader(json));
    }

    private static JsonValue readValue(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            return reader.readValue();
        }
    }

    private static JsonValue createValue(int value) {
        return Json.createValue(value);
    }
}
