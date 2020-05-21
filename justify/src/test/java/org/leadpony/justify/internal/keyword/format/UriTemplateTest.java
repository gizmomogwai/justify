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

package org.leadpony.justify.internal.keyword.format;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * A test class for {@link UriTemplate}.
 *
 * @author leadpony
 */
public class UriTemplateTest {

    // System under test
    private static UriTemplate sut;

    @BeforeAll
    public static void setUpOnce() {
        sut = new UriTemplate();
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @FormatSource("uri-template.json")
    public void testUriTemplate(String value, boolean valid) {
        boolean actual = sut.test(value);
        assertThat(actual).isEqualTo(valid);
    }
}
