/*
 * Copyright 2018-2020 the Justify authors.
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

package org.leadpony.justify.internal.evaluator;

import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.ProblemDispatcher;

/**
 * A skeletal implementation of {@link ChildrenEvaluator}
 * specifialized for JSON arrays.
 *
 * @author leadpony
 */
public abstract class AbstractConjunctiveItemsEvaluator extends AbstractLogicalEvaluator implements ChildrenEvaluator {

    private Result finalResult = Result.TRUE;
    private Evaluator childEvaluator;

    protected AbstractConjunctiveItemsEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword) {
        super(context, schema, keyword);
    }

    @Override
    public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
        if (depth == 0 && event == Event.END_ARRAY) {
            return finalResult;
        }

        if (depth == 1) {
            updateChildren(event, getParser());
        }

        if (childEvaluator != null) {
            Result result = childEvaluator.evaluate(event, depth - 1, dispatcher);
            if (result != Result.PENDING) {
                if (result == Result.FALSE) {
                    finalResult = Result.FALSE;
                }
                childEvaluator = null;
            }
        }

        return Result.PENDING;
    }

    @Override
    public void append(Evaluator evaluator) {
        if (evaluator == Evaluator.ALWAYS_TRUE) {
            return;
        }
        assert childEvaluator == null;
        childEvaluator = evaluator;
    }
}
