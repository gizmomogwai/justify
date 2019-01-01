/*
 * Copyright 2018 the Justify authors.
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
package org.leadpony.justify.internal.keyword.assertion.content;

import javax.json.JsonBuilderFactory;
import javax.json.JsonObjectBuilder;

import org.leadpony.justify.internal.keyword.AbstractKeyword;

/**
 * Content encoding with an unknown scheme.
 * 
 * @author leadpony
 */
public class UnknownContentEncoding extends AbstractKeyword {
    
    private final String scheme;
    
    public UnknownContentEncoding(String scheme) {
        this.scheme = scheme;
    }

    @Override
    public String name() {
        return "contentEncoding";
    }

    @Override
    public boolean canEvaluate() {
        return false;
    }

    @Override
    public void addToJson(JsonObjectBuilder builder, JsonBuilderFactory builderFactory) {
        builder.add(name(), scheme);
    }
}