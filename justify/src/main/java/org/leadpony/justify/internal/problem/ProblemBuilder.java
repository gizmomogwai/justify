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

package org.leadpony.justify.internal.problem;

import static org.leadpony.justify.internal.base.Arguments.requireNonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

import jakarta.json.stream.JsonLocation;

import org.leadpony.justify.api.EvaluatorContext;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.internal.base.Message;

/**
 * This class builds problems detected in validation process.
 *
 * @author leadpony
 */
public class ProblemBuilder {

    private final JsonLocation location;
    private final String pointer;
    private JsonSchema schema;
    private String keyword;
    private boolean resolvable = true;
    private Message message;
    private final Map<String, Object> parameters = new HashMap<>();
    private List<List<Problem>> branches;

    /**
     * Constructs this builder.
     *
     * @param location the source location where problem occurred in the instance,
     *                 may be {@code null}.
     * @param pointer  the JSON pointer to the location where problem occurred in
     *                 the instance, may be {@code null}.
     */
    public ProblemBuilder(JsonLocation location, String pointer) {
        this.location = location;
        this.pointer = pointer;
    }

    /**
     * Constructs this builder.
     *
     * @param context the context of the evaluators.
     */
    public ProblemBuilder(EvaluatorContext context) {
        this(context.getParser().getLocation(), context.getPointer());
    }

    /**
     * Specifies the keyword which supplies the constraint the problem violated.
     *
     * @param keyword the keyword supplying the constraint.
     * @return this builder.
     */
    public ProblemBuilder withKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    /**
     * Specifies the schema whose evaluation caused this problem.
     *
     * @param schema the schema whose evaluation caused this problem.
     * @return this builder.
     */
    public ProblemBuilder withSchema(JsonSchema schema) {
        this.schema = schema;
        return this;
    }

    /**
     * Specifies the resolvability of the problem.
     *
     * @param resolvable the resolvability of the problem.
     * @return this builder.
     */
    public ProblemBuilder withResolvability(boolean resolvable) {
        this.resolvable = resolvable;
        return this;
    }

    /**
     * Specifies the message used for the problem.
     *
     * @param message the message to be presented, cannot be {@code null}.
     * @return this builder.
     */
    public ProblemBuilder withMessage(Message message) {
        this.message = message;
        return this;
    }

    /**
     * Specifies the parameter which will be added to the problem.
     *
     * @param name  the name of the parameter.
     * @param value the value of the parameter.
     * @return this builder.
     */
    public ProblemBuilder withParameter(String name, Object value) {
        this.parameters.put(name, value);
        return this;
    }

    /**
     * Specifies the child problems of the problem to be built.
     *
     * @param branch the list of problems which are children of the problem to be
     *               built.
     * @return this builder.
     */
    public ProblemBuilder withBranch(ProblemList branch) {
        if (this.branches == null) {
            this.branches = new ArrayList<>();
        }
        this.branches.add(Collections.unmodifiableList(branch));
        return this;
    }

    /**
     * Specifies branches of problems.
     *
     * @param branches the branches of problems.
     * @return this builder.
     */
    public ProblemBuilder withBranches(List<ProblemList> branches) {
        for (ProblemList branch : branches) {
            withBranch(branch);
        }
        return this;
    }

    /**
     * Builds a problem.
     *
     * @return built problem.
     */
    public Problem build() {
        if (this.branches == null || this.branches.isEmpty()) {
            return new SimpleProblem(this);
        } else {
            return new CompositeProblem(this);
        }
    }

    /**
     * A problem without any child problems.
     *
     * @author leadpony
     */
    private static class SimpleProblem implements Problem {

        private final JsonSchema schema;
        private final String keyword;
        private final boolean resolvable;
        private final Message message;
        private final Map<String, Object> parameters;
        private final JsonLocation location;
        private final String pointer;

        protected SimpleProblem(ProblemBuilder builder) {
            this.schema = builder.schema;
            this.keyword = builder.keyword;
            this.resolvable = builder.resolvable;
            this.message = builder.message;
            this.parameters = Collections.unmodifiableMap(builder.parameters);
            this.location = builder.location;
            this.pointer = builder.pointer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getMessage(Locale locale) {
            requireNonNull(locale, "locale");
            return buildMessage(locale);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getContextualMessage(Locale locale) {
            requireNonNull(locale, "locale");
            return ProblemRenderer.DEFAULT_RENDERER.render(this, locale);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void print(Consumer<String> lineConsumer, Locale locale) {
            requireNonNull(lineConsumer, "lineConsumer");
            requireNonNull(locale, "locale");
            ProblemRenderer.DEFAULT_RENDERER.render(this, locale, lineConsumer);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonLocation getLocation() {
            return location;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getPointer() {
            return pointer;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public JsonSchema getSchema() {
            return schema;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String getKeyword() {
            return keyword;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Map<String, ?> parametersAsMap() {
            return parameters;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean isResolvable() {
            return resolvable;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            return getContextualMessage();
        }

        /**
         * Builds a message for the specified locale.
         *
         * @param locale the locale for which the message will be localized.
         * @return the built message.
         */
        private String buildMessage(Locale locale) {
            return message.format(parameters, locale);
        }
    }

    /**
     * A problem with branch problems.
     *
     * @author leadpony
     */
    private static class CompositeProblem extends SimpleProblem {

        /**
         * The lists of branches.
         */
        private final List<List<Problem>> branches;

        /**
         * Constructs this problem.
         *
         * @param builder the builder of the problem.
         */
        CompositeProblem(ProblemBuilder builder) {
            super(builder);
            this.branches = Collections.unmodifiableList(builder.branches);
        }

        @Override
        public boolean hasBranches() {
            return true;
        }

        @Override
        public int countBranches() {
            return branches.size();
        }

        @Override
        public List<Problem> getBranch(int index) {
            return branches.get(index);
        }
    }
}
