/*
 * Copyright 2020 the Justify authors.
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
package org.leadpony.justify.tests.api;

import org.junit.jupiter.api.BeforeEach;
import org.leadpony.justify.api.JsonSchemaReaderFactory;
import org.leadpony.justify.api.JsonValidationService;
import org.leadpony.justify.tests.helper.ApiTest;

/**
 * Test cases for {@link JsonSchemaReaderFactory}.
 *
 * @author leadpony
 */
@ApiTest
public class JsonSchemaReaderFactoryTest implements BaseJsonSchemaReaderFactoryTest {

    private static JsonValidationService service;

    private JsonSchemaReaderFactory sut;

    @BeforeEach
    public void setUp() {
        sut = service.createSchemaReaderFactory();
    }

    @Override
    public JsonSchemaReaderFactory sut() {
        return sut;
    }
}
