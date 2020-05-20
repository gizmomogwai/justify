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

import java.io.InputStream;

import org.leadpony.justify.api.SpecVersion;
import org.leadpony.justify.internal.keyword.KeywordFactory;
import org.leadpony.justify.spi.ContentEncodingScheme;
import org.leadpony.justify.spi.ContentMimeType;
import org.leadpony.justify.spi.FormatAttribute;

/**
 * A schema specification.
 *
 * @author leadpony
 */
public interface SchemaSpec {

    /**
     * Returns the version of this specification.
     *
     * @return the version of this specification, never be {@code null}.
     */
    SpecVersion getVersion();

    /**
     * Returns the metaschema of this specification as a stream.
     *
     * @return the stream of the metaschema, never be {@code null}.
     */
    InputStream getMetaschemaAsStream();

    /**
     * Returns the factory of keywords available for this specification.
     *
     * @return the factory of keywords.
     */
    KeywordFactory getKeywordFactory();

    /**
     * Returns the format attribute of the specified name.
     *
     * @param name the name of the format attribute.
     * @return found format attribute, or {@code null}.
     */
    FormatAttribute getFormatAttribute(String name);

    ContentEncodingScheme getEncodingScheme(String name);

    ContentMimeType getMimeType(String value);
}
