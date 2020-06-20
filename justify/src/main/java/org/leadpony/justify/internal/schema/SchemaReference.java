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

package org.leadpony.justify.internal.schema;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.net.URI;
import java.util.Map;

import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonParser.Event;

import org.leadpony.justify.api.Evaluator;
import org.leadpony.justify.api.InstanceType;
import org.leadpony.justify.api.ObjectJsonSchema;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemDispatcher;
import org.leadpony.justify.api.keyword.Keyword;
import org.leadpony.justify.internal.base.AbstractEmptyMap;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.keyword.core.Ref;
import org.leadpony.justify.internal.problem.ProblemBuilderFactory;

/**
 * Schema reference containing "$ref" keyword.
 *
 * @author leadpony
 */
public class SchemaReference extends AbstractJsonSchema {

    private URI targetId;
    private URI refId;
    private JsonSchema referencedSchema;

    /**
     * Constructs this schema reference.
     *
     * @param id       the identifier of this schema, may be {@code null}.
     * @param json     the JSON representation of this schema.
     * @param keywords all keywords.
     */
    public SchemaReference(URI id, JsonObject json, Map<String, Keyword> keywords) {
        this(id, json, keywords, ((Ref) keywords.get("$ref")).value());
    }

    /**
     * Constructs this schema reference.
     *
     * @param id       the identifier of this schema, may be {@code null}.
     * @param json     the JSON representation of this schema.
     * @param keywords all keywords.
     * @param refId    the identifier of the referenced schema.
     */
    public SchemaReference(URI id, JsonObject json, Map<String, Keyword> keywords, URI refId) {
        super(id, json, keywords);
        this.refId = refId;
        this.referencedSchema = new NonexistentSchema();
        if (hasAbsoluteId()) {
            this.targetId = id().resolve(refId);
        } else {
            this.targetId = refId;
        }
    }

    /**
     * Returns the original value of the keyword "$ref".
     *
     * @return the value of the keyword "$ref".
     */
    public URI ref() {
        return refId;
    }

    /**
     * Returns the URI of the referenced schema.
     *
     * @return the URI of the referenced schema.
     */
    public URI getTargetId() {
        return targetId;
    }

    /**
     * Checks if this schema reference has a referenced schema or not.
     *
     * @return {@code true} if this reference has a referenced schema, {@code false}
     *         otherwise.
     */
    public boolean hasReferencedSchema() {
        return referencedSchema != null;
    }

    /**
     * Returns the referenced schema.
     *
     * @return the referenced schema.
     */
    public JsonSchema getReferencedSchema() {
        return referencedSchema;
    }

    /**
     * Assigns the referenced schema.
     *
     * @param schema the referenced schema, cannot be {@code null}.
     */
    public void setReferencedSchema(JsonSchema schema) {
        requireNonNull(schema, "schema");
        this.referencedSchema = schema;
    }

    /* JsonSchema interface */

    @Override
    public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
        return referencedSchema.createEvaluator(parent, type);
    }

    @Override
    public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
        return referencedSchema.createNegatedEvaluator(parent, type);
    }

    /* Resolvable interface */

    @Override
    public void resolve(URI baseUri) {
        super.resolve(baseUri);
        if (!this.targetId.isAbsolute()) {
            if (hasId()) {
                baseUri = id();
            }
            this.targetId = baseUri.resolve(this.targetId);
        }
    }

    /**
     * Nonexistent JSON Schema.
     *
     * @author leadpony
     */
    private class NonexistentSchema extends AbstractEmptyMap<String, Keyword> implements ObjectJsonSchema {

        @Override
        public Evaluator createEvaluator(Evaluator parent, InstanceType type) {
            return new Evaluator() {
                @Override
                public Result evaluate(Event event, int depth, ProblemDispatcher dispatcher) {
                    Problem p = ProblemBuilderFactory.DEFAULT.createProblemBuilder(parent.getContext())
                            .withKeyword("$ref")
                            .withMessage(Message.SCHEMA_PROBLEM_REFERENCE)
                            .withParameter("ref", ref())
                            .withParameter("targetId", getTargetId())
                            .build();
                    dispatcher.dispatchProblem(p);
                    return Result.FALSE;
                }
            };
        }

        @Override
        public Evaluator createNegatedEvaluator(Evaluator parent, InstanceType type) {
            return createEvaluator(parent, type);
        }

        @Override
        public JsonValue toJson() {
            return JsonValue.EMPTY_JSON_OBJECT;
        }
    }
}
