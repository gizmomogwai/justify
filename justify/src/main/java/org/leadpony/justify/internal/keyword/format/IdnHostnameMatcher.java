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

/**
 * Matcher for internationalized hostnames.
 *
 * @author leadpony
 */
class IdnHostnameMatcher extends HostnameMatcher {

    IdnHostnameMatcher(CharSequence input) {
        super(input);
    }

    IdnHostnameMatcher(CharSequence input, int start, int end) {
        super(input, start, end);
    }

    @Override
    protected boolean checkFirstLabelLetter(int c) {
        if (c < 128) {
            return super.checkFirstLabelLetter(c);
        } else {
            return checkCodePointAllowed(c);
        }
    }

    @Override
    protected boolean checkLabelLetter(int c) {
        if (c < 128) {
            return super.checkLabelLetter(c);
        } else {
            return checkCodePointAllowed(c);
        }
    }

    private static boolean checkCodePointAllowed(int c) {
        IdnProperty property = IdnProperty.of(c);
        return property != IdnProperty.DISALLOWED && property != IdnProperty.UNASSIGNED;
    }
}
