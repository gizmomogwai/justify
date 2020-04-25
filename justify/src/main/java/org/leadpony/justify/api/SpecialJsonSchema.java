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

import jakarta.json.stream.JsonParser.Event;

/**
 * A skeletal implementation of special JSON schemas.
 *
 * @author leadpony
 */
abstract class SpecialJsonSchema implements JsonSchema {

    /* As a JsonSchema */

    protected final Evaluator alwaysFalse(EvaluatorContext context) {
        JsonSchema self = this;
        return new Evaluator() {
            @Override
            public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                dispatcher.dispatchInevitableProblem(context, self);
                return Result.FALSE;
            }
        };
    }
}
