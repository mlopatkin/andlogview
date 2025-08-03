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

import static name.mlopatkin.andlogview.sdkrepo.XmlAsserts.assertThat;

import static com.google.common.collect.ImmutableList.toImmutableList;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.Objects;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

class StackedXmlReaderTest {
    private static final XMLInputFactory XML_FACTORY = newXmlFactory();

    @Test
    void canParseSimpleDocument() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag/>
                """);

        assertThat(reader.nextTag()).isNotNull().hasName("tag");
        assertThat(reader.nextTag()).isNull();
    }

    @Test
    void canReadContentsOfEmptyTag() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag/>
                """);

        reader.nextTag();
        assertThat(reader.getCurrentElementText()).isEmpty();
    }

    @Test
    void canReadContentsOfNonEmptyTag() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag>some contents</tag>
                """);

        reader.nextTag();
        assertThat(reader.getCurrentElementText()).isEqualTo("some contents");
    }

    @Test
    void canReadContentsOfNonEmptyTagWithComments() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag>some<!-- comment --> contents</tag>
                """);

        reader.nextTag();
        assertThat(reader.getCurrentElementText()).isEqualTo("some contents");
    }

    @Test
    void canHandleEscapeSequences() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag>some&amp;contents</tag>
                """);

        reader.nextTag();
        assertThat(reader.getCurrentElementText()).isEqualTo("some&contents");
    }

    @Test
    void canHandleNestedTags() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag>
                    <nested>Nested text</nested>
                </tag>
                """);

        assertThat(reader.nextTag()).hasName("tag");
        assertThat(reader.nextTag()).hasName("nested");
        assertThat(reader.getCurrentElementText()).isEqualTo("Nested text");
        assertThat(reader.nextTag()).isNull();
    }

    @Test
    void canIterateOverNestedTags() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <top-level>
                    <tag1>
                        <tag1-nested1/>
                        <tag1-nested2/>
                    </tag1>
                    <tag2>
                        <tag2-nested1/>
                        <tag2-nested2/>
                    </tag2>
                </top-level>
                """);

        reader.nextTag(); // top-level
        var tag1 = Objects.requireNonNull(reader.nextTag());
        assertThat(reader.nextTagIn(tag1)).hasName("tag1-nested1");
        assertThat(reader.nextTagIn(tag1)).hasName("tag1-nested2");
        assertThat(reader.nextTagIn(tag1)).isNull();
    }

    @Test
    void tryingToIterateWhenTagIsOverDoesNotAdvanceTheStream() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <top-level>
                    <tag1>
                        <tag1-nested1/>
                        <tag1-nested2/>
                    </tag1>
                    <tag2>
                        <tag2-nested1/>
                        <tag2-nested2/>
                    </tag2>
                </top-level>
                """);

        reader.nextTag(); // top-level
        var tag1 = Objects.requireNonNull(reader.nextTag());
        reader.nextTagIn(tag1);
        reader.nextTagIn(tag1);

        assertThat(reader.nextTagIn(tag1)).isNull();
        assertThat(reader.nextTagIn(tag1)).isNull();

        assertThat(reader.nextTag()).hasName("tag2");
    }

    @Test
    void canSkipCurrentTag() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <top-level>
                    <tag1>
                        <tag1-nested1/>
                        <tag1-nested2/>
                    </tag1>
                    <tag2>
                        <tag2-nested1/>
                        <tag2-nested2/>
                    </tag2>
                </top-level>
                """);

        reader.nextTag(); // top-level
        var tag1 = Objects.requireNonNull(reader.nextTag());
        reader.skipTag(tag1);
        assertThat(reader.nextTag()).hasName("tag2");
    }

    @Test
    void canUseForeachHelperToIterateOverTags() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <top-level>
                    <tag1>
                        <tag1-nested1/>
                        <tag1-nested2/>
                    </tag1>
                    <tag2>
                    </tag2>
                </top-level>
                """);

        var tags = ImmutableList.copyOf(reader.tags())
                .stream()
                .map(e -> e.getName().getLocalPart())
                .collect(toImmutableList());

        assertThat(tags).containsExactly(
                "top-level", "tag1", "tag1-nested1", "tag1-nested2", "tag2"
        );
    }

    @Test
    void canUseForeachHelperToIterateOverNestedTags() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <top-level>
                    <tag1>
                        <tag1-nested1/>
                        <tag1-nested2/>
                    </tag1>
                    <tag2>
                    </tag2>
                </top-level>
                """);

        reader.nextTag();
        var tag = Objects.requireNonNull(reader.nextTag());
        var tags = ImmutableList.copyOf(reader.childrenOf(tag))
                .stream()
                .map(e -> e.getName().getLocalPart())
                .collect(toImmutableList());

        assertThat(tags).containsExactly(
                "tag1-nested1", "tag1-nested2"
        );

        assertThat(reader.nextTagIn(tag)).isNull();
        assertThat(reader.childrenOf(tag)).isEmpty();

        assertThat(reader.nextTag()).hasName("tag2");
    }

    @Test
    void tagsWithTheSameNameCanBeNested() throws Exception {
        var reader = newReader("""
                <?xml version="1.0"?>
                <tag>
                    <tag>
                        <nested-1/>
                        <tag>
                            <nested-2/>
                        </tag>
                        <nested-3/>
                    </tag>
                    <other/>
                </tag>
                """);
        reader.nextTag();
        var tag = Objects.requireNonNull(reader.nextTag());
        assertThat(reader.nextTagIn(tag)).hasName("nested-1");
        assertThat(reader.nextTagIn(tag)).hasName("tag");
        assertThat(reader.nextTagIn(tag)).hasName("nested-2");
        assertThat(reader.nextTagIn(tag)).hasName("nested-3");
        assertThat(reader.nextTagIn(tag)).isNull();

        assertThat(reader.nextTag()).hasName("other");
    }

    private StackedXmlReader newReader(String xmlDocument) throws XMLStreamException {
        return new StackedXmlReader(XML_FACTORY.createXMLEventReader(new StringReader(xmlDocument)));
    }

    private static XMLInputFactory newXmlFactory() {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        factory.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, true);
        factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
        factory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
        return factory;
    }
}
