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

package org.leadpony.justify.internal.base.json;

import java.util.HashMap;
import java.util.Map;

import jakarta.json.JsonArray;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonStructure;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser;
import jakarta.json.stream.JsonParser.Event;
import jakarta.json.stream.JsonParsingException;

import org.leadpony.justify.internal.base.Message;

/**
 * The default implementation of {@link JsonReader}.
 *
 * @author leadpony
 */
public class DefaultJsonReader implements JsonReader {

    private final JsonParser parser;
    private boolean alreadyRead;
    private boolean alreadyClosed;

    public DefaultJsonReader(JsonParser parser) {
        this.parser = parser;
    }

    @Override
    public JsonStructure read() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read or closed");
        }
        alreadyRead = true;
        if (parser.hasNext()) {
            JsonParser.Event event = parser.next();
            try {
                if (event == Event.START_ARRAY) {
                    return parser.getArray();
                } else if (event == Event.START_OBJECT) {
                    return parser.getObject();
                } else {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("event", event);
                    String message = Message.READER_READ_ERROR.format(parameters);
                    throw newParsingException(message);
                }
            } catch (IllegalStateException e) {
                throw newParsingException(e);
            }

        }
        throw newUnexpectedEndOfInputException();
    }

    @Override
    public JsonObject readObject() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read or closed");
        }
        alreadyRead = true;
        if (parser.hasNext()) {
            parser.next();
            try {
                return parser.getObject();
            } catch (IllegalStateException e) {
                throw newParsingException(e);
            }
        }
        throw newUnexpectedEndOfInputException();
    }

    @Override
    public JsonArray readArray() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read or closed");
        }
        alreadyRead = true;
        if (parser.hasNext()) {
            parser.next();
            try {
                return parser.getArray();
            } catch (IllegalStateException e) {
                throw newParsingException(e);
            }
        }
        throw newUnexpectedEndOfInputException();
    }

    @Override
    public JsonValue readValue() {
        if (alreadyRead || alreadyClosed) {
            throw new IllegalStateException("already read or closed");
        }
        alreadyRead = true;
        if (parser.hasNext()) {
            parser.next();
            try {
                return parser.getValue();
            } catch (IllegalStateException e) {
                throw newParsingException(e);
            }
        }
        throw newUnexpectedEndOfInputException();
    }

    @Override
    public void close() {
        if (!alreadyClosed) {
            parser.close();
            alreadyClosed = true;
        }
    }

    private static JsonException newUnexpectedEndOfInputException() {
        String message = Message.READER_UNEXPECTED_EOI.getLocalized();
        return new JsonException(message);
    }

    private JsonParsingException newParsingException(String message) {
        JsonLocation location = SimpleJsonLocation.before(parser.getLocation());
        return new JsonParsingException(message, location);
    }

    private JsonParsingException newParsingException(Exception cause) {
        JsonLocation location = SimpleJsonLocation.before(parser.getLocation());
        return new JsonParsingException(cause.getMessage(), cause, location);
    }
}
