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

package org.leadpony.justify.internal.keyword.applicator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

import jakarta.json.JsonValue;
import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.internal.evaluator.LogicalEvaluator;

/**
 * N-ary boolean logic. This class is the abstract base class for {@link AllOf},
 * {@link AnyOf} and {@link OneOf}.
 *
 * @author leadpony
 */
abstract class NaryBooleanLogic extends Applicator {

    private final List<JsonSchema> subschemas;

    protected NaryBooleanLogic(JsonValue json, Collection<JsonSchema> subschemas) {
        super(json);
        this.subschemas = new ArrayList<>(subschemas);
    }

    @Override
    protected Evaluator doCreateEvaluator(EvaluatorContext context, InstanceType type) {
        return createLogicalEvaluator(context, type).withProblemBuilderFactory(this);
    }

    @Override
    protected Evaluator doCreateNegatedEvaluator(EvaluatorContext context, InstanceType type) {
        return createNegatedLogicalEvaluator(context, type).withProblemBuilderFactory(this);
    }

    @Override
    public boolean isInPlace() {
        return true;
    }

    @Override
    public boolean hasSubschemas() {
        return !subschemas.isEmpty();
    }

    @Override
    public Stream<JsonSchema> getSubschemas() {
        return this.subschemas.stream();
    }

    @Override
    public JsonSchema getSubschema(Iterator<String> jsonPointer) {
        if (jsonPointer.hasNext()) {
            try {
                int index = Integer.parseInt(jsonPointer.next());
                if (index < subschemas.size()) {
                    return subschemas.get(index);
                }
            } catch (NumberFormatException e) {
            }
        }
        return null;
    }

    /**
     * Creates a new evaluator for this boolean logic.
     *
     * @param context the context of the evaluator to be created.
     * @param type    the type of the instance to validate.
     * @return newly created evaluator.
     */
    protected abstract LogicalEvaluator createLogicalEvaluator(EvaluatorContext context, InstanceType type);

    /**
     * Creates a new evaluator for the negated version of this boolean logic.
     *
     * @param context the context of the evaluator to be created.
     * @param type    the type of the instance to validate.
     * @return newly created evaluator.
     */
    protected abstract LogicalEvaluator createNegatedLogicalEvaluator(EvaluatorContext context, InstanceType type);
}
