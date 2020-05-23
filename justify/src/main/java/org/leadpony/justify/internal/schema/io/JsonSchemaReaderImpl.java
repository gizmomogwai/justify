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
package org.leadpony.justify.internal.schema.io;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.stream.JsonLocation;
import jakarta.json.stream.JsonParser.Event;
import org.leadpony.justify.api.JsonSchema;
import org.leadpony.justify.api.JsonSchemaResolver;
import org.leadpony.justify.api.Keyword;
import org.leadpony.justify.api.KeywordType;
import org.leadpony.justify.api.Problem;
import org.leadpony.justify.api.ProblemHandler;
import org.leadpony.justify.internal.base.Message;
import org.leadpony.justify.internal.base.Sets;
import org.leadpony.justify.internal.base.URIs;
import org.leadpony.justify.internal.base.json.JsonService;
import org.leadpony.justify.internal.base.json.PointerAwareJsonParser;
import org.leadpony.justify.internal.keyword.IdKeyword;
import org.leadpony.justify.internal.keyword.Referenceable;
import org.leadpony.justify.internal.keyword.UnknownKeyword;
import org.leadpony.justify.internal.keyword.core.Ref;
import org.leadpony.justify.internal.keyword.format.UnknownFormatAttributeException;
import org.leadpony.justify.internal.problem.ProblemBuilder;
import org.leadpony.justify.internal.schema.BasicJsonSchema;
import org.leadpony.justify.internal.schema.Resolvable;
import org.leadpony.justify.internal.schema.SchemaReference;
import org.leadpony.justify.internal.schema.SchemaSpec;
import org.leadpony.justify.internal.validator.JsonValidator;

/**
 * @author leadpony
 */
public class JsonSchemaReaderImpl extends AbstractJsonSchemaReader
    implements ProblemHandler, KeywordType.CreationContext {

    private final PointerAwareJsonParser parser;
    private final JsonService jsonService;
    private final Map<String, KeywordType> keywordTypeMap;

    private final Map<JsonObject, Reference> referencingObjects = new IdentityHashMap<>();
    // schemas having $id keyword.
    private final Set<JsonSchema> identifiedSchemas = Sets.newIdentitySet();
    private final List<Reference> references = new ArrayList<>();

    private URI initialBaseUri = DEFAULT_INITIAL_BASE_URI;

    public JsonSchemaReaderImpl(
            PointerAwareJsonParser parser,
            JsonService jsonService,
            SchemaSpec spec) {
        this(parser, jsonService, spec.getBareKeywordTypes(), Collections.emptyMap());
    }

    public JsonSchemaReaderImpl(
            PointerAwareJsonParser parser,
            JsonService jsonService,
            Map<String, KeywordType> keywordTypeMap,
            Map<String, Object> config) {
        super(config);

        this.parser = parser;
        this.jsonService = jsonService;
        this.keywordTypeMap = keywordTypeMap;

        if (parser instanceof JsonValidator) {
            ((JsonValidator) parser).withHandler(this);
        }
    }

    /* As a AbstractSchemaReader */

    @Override
    protected JsonSchema readSchema() {
        JsonSchema schema = readRootSchema();
        if (schema != null) {
            postprocess(schema);
        }
        dispatchProblems();
        return schema;
    }

    @Override
    protected JsonLocation getLocation() {
        return parser.getLocation();
    }

    @Override
    protected void closeParser() {
        parser.close();
    }

    /* As a ProblemHandler */

    @Override
    public void handleProblems(List<Problem> problems) {
        addProblems(problems);
    }

    /* As a CreationContext */

    @Override
    public JsonSchema asJsonSchema(JsonValue value) {
        JsonSchema schema = parseSchema(value, false);
        if (schema == null) {
            throw new IllegalArgumentException();
        }
        return schema;
    }

    /* */

    /**
     * Reads the schema at the root.
     *
     * @return the schema if it exists, {@code null} otherwise.
     */
    private JsonSchema readRootSchema() {
        if (parser.hasNext()) {
            JsonValue value = parseValue(parser.next());
            return parseSchema(value, false);
        } else {
            addProblem(Message.SCHEMA_PROBLEM_EMPTY);
        }
        return null;
    }

    private JsonValue parseValue(Event event) {
        switch (event) {
        case START_ARRAY:
            return parseArray();
        case START_OBJECT:
            return parseObject();
        case VALUE_STRING:
        case VALUE_NUMBER:
            return parser.getValue();
        case VALUE_TRUE:
            return JsonValue.TRUE;
        case VALUE_FALSE:
            return JsonValue.FALSE;
        case VALUE_NULL:
            return JsonValue.NULL;
        default:
            throw new IllegalStateException();
        }
    }

    private JsonArray parseArray() {
        JsonArrayBuilder builder = jsonService.createArrayBuilder();
        while (parser.hasNext()) {
            final Event event = parser.next();
            if (event == Event.END_ARRAY) {
                return builder.build();
            }
            builder.add(parseValue(event));
        }
        throw newUnexpectedEndException();
    }

    private JsonObject parseObject() {
        JsonObjectBuilder builder = jsonService.createObjectBuilder();
        Reference reference = null;
        while (parser.hasNext()) {
            if (parser.next() == Event.END_OBJECT) {
                JsonObject object = builder.build();
                if (reference != null) {
                    addReferencingObject(object, reference);
                }
                return object;
            }
            final String name = parser.getString();
            if (parser.hasNext()) {
                builder.add(name, parseValue(parser.next()));
                if (name.equals("$ref")) {
                    reference = createReference();
                }
            } else {
                break;
            }
        }
        throw newUnexpectedEndException();
    }

    private Reference createReference() {
        return new Reference(parser.getLocation(), parser.getPointer());
    }

    private void addReferencingObject(JsonObject object, Reference reference) {
        this.referencingObjects.put(object, reference);
    }

    private JsonSchema parseSchema(JsonValue value, boolean lax) {
        switch (value.getValueType()) {
        case TRUE:
            return JsonSchema.TRUE;
        case FALSE:
            return JsonSchema.FALSE;
        case OBJECT:
            return parseSchema(value.asJsonObject(), lax);
        default:
            return null;
        }
    }

    private JsonSchema parseSchema(JsonObject value, boolean lax) {
        SchemaBuilder builder = new SchemaBuilder();
        for (Map.Entry<String, JsonValue> entry : value.entrySet()) {
            String name = entry.getKey();
            Keyword keyword = createKeyword(name, entry.getValue(), lax);
            builder.add(name, keyword);
        }
        JsonSchema schema = builder.build(value);
        if (schema.hasId()) {
            this.identifiedSchemas.add(schema);
        }
        if (schema instanceof SchemaReference) {
            addReference(value, (SchemaReference) schema);
        }
        return schema;
    }

    private Keyword createKeyword(String name, JsonValue value, boolean lax) {
        KeywordType type = keywordTypeMap.get(name);
        if (type != null) {
            try {
                return type.newInstance(value, this);
            } catch (UnknownFormatAttributeException e) {
                addProblem(createProblemBuilder(Message.SCHEMA_PROBLEM_FORMAT_UNKNOWN)
                        .withParameter("attribute", e.getAttributeName()));
                return createUnknownKeyword(name, value, true);
            } catch (IllegalArgumentException e) {
                // Ignores the exception
            }
        }
        return createUnknownKeyword(name, value, lax);
    }

    private Keyword createUnknownKeyword(String name, JsonValue value, boolean lax) {
        if (isStrictWithKeywords() && !lax) {
            ProblemBuilder builder = createProblemBuilder(Message.SCHEMA_PROBLEM_KEYWORD_UNKNOWN)
                    .withParameter("keyword", name);
            addProblem(builder);
        }

        switch (value.getValueType()) {
        case OBJECT:
        case TRUE:
        case FALSE:
            return new Referenceable(name, parseSchema(value, true));
        default:
            return new UnknownKeyword(name, value);
        }
    }

    private void addReference(JsonObject value, SchemaReference schema) {
        Reference reference = this.referencingObjects.get(value);
        if (reference != null) {
            reference.setSchema(schema);
            this.references.add(reference);
        }
    }

    private ProblemBuilder createProblemBuilder(Message message) {
        JsonLocation location = parser.getLocation();
        String pointer = parser.getPointer();
        return createProblemBuilder(location, pointer).withMessage(message);
    }

    private void addProblem(Message message) {
        addProblem(createProblemBuilder(message));
    }

    private void postprocess(JsonSchema schema) {
        Map<URI, JsonSchema> schemaMap = generateSchemaMap(
                schema, this.initialBaseUri);
        resolveAllReferences(schemaMap);
        checkInfiniteRecursiveLoop();
    }

    private Map<URI, JsonSchema> generateSchemaMap(JsonSchema root, URI baseUri) {
        Map<URI, JsonSchema> schemaMap = new HashMap<>();

        if (root instanceof Resolvable) {
            ((Resolvable) root).resolve(baseUri);
        }

        for (JsonSchema schema : this.identifiedSchemas) {
            schemaMap.put(URIs.withFragment(schema.id()), schema);
        }

        if (!this.identifiedSchemas.contains(root)) {
            schemaMap.put(URIs.withFragment(baseUri), root);
        }

        return schemaMap;
    }

    private void resolveAllReferences(Map<URI, JsonSchema> schemaMap) {
        for (Reference context : this.references) {
            SchemaReference reference = context.reference;
            URI targetId = reference.getTargetId();
            JsonSchema schema = dereferenceSchema(targetId, schemaMap);
            if (schema != null) {
                reference.setReferencedSchema(schema);
            } else {
                addProblem(createProblemBuilder(context.location, context.pointer)
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE)
                        .withParameter("ref", reference.ref())
                        .withParameter("targetId", targetId));
            }
        }
    }

    private JsonSchema dereferenceSchema(URI ref, Map<URI, JsonSchema> schemaMap) {
        ref = URIs.withFragment(ref);
        String fragment = ref.getFragment();
        if (fragment.startsWith("/")) {
            JsonSchema schema = resolveSchema(URIs.withEmptyFragment(ref), schemaMap);
            if (schema != null) {
                return schema.getSubschemaAt(fragment);
            }
            return null;
        } else {
            return resolveSchema(ref, schemaMap);
        }
    }

    private JsonSchema resolveSchema(URI id, Map<URI, JsonSchema> schemaMap) {
        JsonSchema schema = schemaMap.get(id);
        if (schema != null) {
            return schema;
        }
        for (JsonSchemaResolver resolver : getResolvers()) {
            schema = resolver.resolveSchema(id);
            if (schema != null) {
                return schema;
            }
        }
        return null;
    }

    private void checkInfiniteRecursiveLoop() {
        InfiniteLoopDetector detector = new InfiniteLoopDetector();
        for (Reference context : this.references) {
            SchemaReference reference = context.reference;
            if (detector.detectInfiniteLoop(reference)) {
                addProblem(createProblemBuilder(context.location, context.pointer)
                        .withMessage(Message.SCHEMA_PROBLEM_REFERENCE_LOOP));
            }
        }
    }

    /**
     * A builder of JSON schema.
     *
     * @author leadpony
     */
    @SuppressWarnings("serial")
    class SchemaBuilder extends LinkedHashMap<String, Keyword> {

        private URI id;
        private boolean referencing;

        void add(String name, Keyword keyword) {
            if (keyword instanceof IdKeyword) {
                this.id = ((IdKeyword) keyword).value();
            } else if (keyword instanceof Ref) {
                referencing = true;
            }
            super.put(name, keyword);
        }

        JsonSchema build(JsonObject json) {
            if (isEmpty()) {
                return JsonSchema.EMPTY;
            }
            if (referencing) {
                return new SchemaReference(this.id, json, this);
            } else {
                return BasicJsonSchema.of(this.id, json, this);
            }
        }
    }

    /**
     * A reference to be resolved.
     *
     * @author leadpony
     */
    private static class Reference {

        final JsonLocation location;
        final String pointer;

        SchemaReference reference;

        Reference(JsonLocation location, String pointer) {
            this.location = location;
            this.pointer = pointer;
        }

        void setSchema(SchemaReference reference) {
            this.reference = reference;
        }
    }
}
