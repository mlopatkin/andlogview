/*
 * Copyright 2025 the Andlogview authors
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

package name.mlopatkin.andlogview.sdkrepo;

import joptsimple.internal.Strings;

import org.assertj.core.api.AbstractAssert;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

import javax.xml.stream.events.StartElement;

/**
 * Assertions for XML processing routines.
 */
public class XmlAsserts {
    public static class StartElementAssert extends AbstractAssert<StartElementAssert, StartElement> {
        protected StartElementAssert(@Nullable StartElement startElement) {
            super(startElement, StartElementAssert.class);
        }

        public StartElementAssert hasDefaultNamespace() {
            var namespaceURI = actual().getName().getNamespaceURI();
            if (!Strings.isNullOrEmpty(namespaceURI)) {
                throw failureWithActualExpected(namespaceURI, "", "Expected namespace to be absent but was <%s>",
                        namespaceURI);
            }
            return this;
        }

        public StartElementAssert hasName(String name) {
            hasDefaultNamespace();

            var localPart = actual.getName().getLocalPart();
            if (!Objects.equals(name, localPart)) {
                throw failureWithActualExpected(localPart, name, "Expected name <%s> but was <%s>",
                        name, localPart);
            }
            return this;
        }
    }

    public static StartElementAssert assertThat(@Nullable StartElement element) {
        return new StartElementAssert(element);
    }
}
