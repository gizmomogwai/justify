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

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;

/**
 * {@link RegExpMatcher} for non unicode mode.
 * <p>
 * All characters in the input will be interpreted as BMP code points.
 * </p>
 *
 * @author leadpony
 */
class NonUnicodeRegExpMatcher extends RegExpMatcher {

    /**
     * Constructs this matcher.
     *
     * @param input the input string.
     */
    NonUnicodeRegExpMatcher(CharSequence input) {
        super(input);
    }

    @Override
    protected boolean identityEscape() {
        if (hasNext() && !isUnicodeIDContinue(peek())) {
            // SourceCharacter but not UnicodeIDContinue
            return withClassAtomOf(next());
        }
        return false;
    }

    @Override
    protected int codePointAt(CharSequence input, int index) {
        return input.charAt(index);
    }

    @Override
    protected int offsetByCodePoint(CharSequence input, int index) {
        return index + 1;
    }

    private static boolean isUnicodeIDContinue(int ch) {
        return UCharacter.hasBinaryProperty(ch, UProperty.ID_CONTINUE);
    }
}
