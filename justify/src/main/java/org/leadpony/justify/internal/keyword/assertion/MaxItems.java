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

package org.leadpony.justify.internal.keyword.assertion;

import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.annotation.KeywordType;
import org.leadpony.justify.internal.annotation.Spec;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.json.ParserEvents;
import org.leadpony.justify.internal.evaluator.ShallowEvaluator;
import org.leadpony.justify.internal.keyword.AbstractAssertionKeyword;
import org.leadpony.justify.internal.keyword.ArrayEvaluatorSource;
import org.leadpony.justify.internal.keyword.KeywordMapper;

/**
 * Assertion specified with "maxItems" validation keyword.
 *
 * @author leadpony
 */
@KeywordType("maxItems")
@Spec(SpecVersion.DRAFT_04)
@Spec(SpecVersion.DRAFT_06)
@Spec(SpecVersion.DRAFT_07)
public class MaxItems extends AbstractAssertionKeyword implements ArrayEvaluatorSource {

    private final int limit;

    /**
     * Returns the mapper which maps a JSON value to this keyword.
     *
     * @return the mapper for this keyword.
     */
    public static KeywordMapper mapper() {
        KeywordMapper.FromNonNegativeInteger mapper = MaxItems::new;
        return mapper;
    }

    public MaxItems(JsonValue json, int limit) {
        super(json);
        this.limit = limit;
    }

    @Override
    public Evaluator doCreateEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        return new AssertionEvaluator(context, schema, this, limit);
    }

    @Override
    public Evaluator doCreateNegatedEvaluator(EvaluatorContext context, JsonSchema schema, InstanceType type) {
        return new MinItems.AssertionEvaluator(context, schema, this, limit + 1);
    }

    /**
     * An evaluator of this keyword.
     *
     * @author leadpony
     */
    static class AssertionEvaluator extends ShallowEvaluator {

        private final int maxItems;
        private int currentCount;

        AssertionEvaluator(EvaluatorContext context, JsonSchema schema, Keyword keyword, int maxItems) {
            super(context, schema, keyword);
            this.maxItems = maxItems;
        }

        @Override
        public Result evaluateShallow(Event event, int depth, ProblemDispatcher dispatcher) {
            if (depth == 1) {
                if (ParserEvents.isValue(event)) {
                    ++currentCount;
                }
            } else if (depth == 0 && event == Event.END_ARRAY) {
                if (currentCount <= maxItems) {
                    return Result.TRUE;
                } else {
                    Problem p = newProblemBuilder()
                            .withMessage(Message.INSTANCE_PROBLEM_MAXITEMS)
                            .withParameter("actual", currentCount)
                            .withParameter("limit", maxItems)
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            }
            return Result.PENDING;
        }
    }
}
