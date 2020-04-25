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

package org.leadpony.justify.api;

import jakarta.json.stream.JsonParser;

/**
 * A factory interface for creating {@link ProblemHandler} instances.
 *
 * @author leadpony
 */
@FunctionalInterface
public interface ProblemHandlerFactory {

    /**
     * Returns a problem handler for a JSON parser.
     *
     * @param parser the JSON parser for which problem handler will be returned.
     *               This cannot be {@code null}.
     * @return the problem handler for the specified JSON parser, cannot be {@code null}.
     */
    ProblemHandler createProblemHandler(JsonParser parser);
}
