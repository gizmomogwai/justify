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

package org.leadpony.justify.internal.keyword.format;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.condition.EnabledOnJre;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * A test class for {@link IdnProperty}.
 *
 * @author leadpony
 */
public class IdnPropertyTest {

    private static final String TABLE_6_2_0 = "/org/iana/idna-tables-properties-" + getUnicodeVersion() + ".csv";

    @ParameterizedTest(name = "{0}")
    @MethodSource("testCases")
    @EnabledOnJre(JRE.JAVA_8)
    public void ofShouldReturnIdnPropertyAsExpected(TestCase test) {
        for (int codePoint = test.startCodePoint; codePoint <= test.endCodePoint; codePoint++) {
            IdnProperty actual = IdnProperty.of(codePoint);
            assertThat(actual).isEqualTo(test.expected);
        }
    }

    public static Stream<TestCase> testCases() throws IOException {
        InputStream in = IdnPropertyTest.class.getResourceAsStream(TABLE_6_2_0);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            return reader.lines()
                    .skip(1) // Skips header line.
                    .map(IdnPropertyTest::mapToTestCase)
                    .collect(Collectors.toList())
                    .stream();
        }
    }

    private static TestCase mapToTestCase(String line) {
        String[] tokens = line.split(",");
        IdnProperty property = IdnProperty.valueOf(tokens[1]);
        String[] range = tokens[0].trim().split("\\-");
        int start = Integer.parseInt(range[0], 16);
        int end = start;
        if (range.length > 1) {
            end = Integer.parseInt(range[1], 16);
        }
        return new TestCase(start, end, property);
    }

    private static String getUnicodeVersion() {
        String specVersion = System.getProperty("java.specification.version");
        switch (specVersion) {
        case "1.8":
        default:
            return "6.2";
        case "10":
            return "8.0";
        }
    }

    /**
     * A test case for {@link IdnProperty}.
     *
     * @author leadpony
     */
    private static class TestCase {

        final int startCodePoint;
        final int endCodePoint;
        final IdnProperty expected;

        TestCase(int start, int end, IdnProperty expected) {
            this.startCodePoint = start;
            this.endCodePoint = end;
            this.expected = expected;
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder();
            b.append(toHexString(startCodePoint));
            if (endCodePoint > startCodePoint) {
                b.append("-").append(toHexString(endCodePoint));
            }
            b.append(" ; ").append(expected);
            return b.toString();
        }

        private String toHexString(int codePoint) {
            return String.format("%04X", codePoint);
        }
    }
}
