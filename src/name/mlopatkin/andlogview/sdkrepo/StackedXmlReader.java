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

import name.mlopatkin.andlogview.base.MyThrowables;

import com.google.common.base.Preconditions;
import com.google.common.collect.AbstractIterator;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * A helper class to read XML data. Provides a nicer API to read inner tags.
 */
class StackedXmlReader {
    private final XMLEventReader reader;
    private final Deque<StartElement> elementPath = new ArrayDeque<>(10);

    public StackedXmlReader(XMLEventReader reader) {
        this.reader = reader;
    }

    /**
     * Returns the next XML event.
     *
     * @return the next XML event
     * @throws XMLStreamException if the document cannot be read
     * @throws NoSuchElementException if there is no more events
     * @see XMLEventReader#nextEvent()
     */
    public XMLEvent nextEvent() throws XMLStreamException {
        var event = reader.nextEvent();
        if (event.isStartElement()) {
            elementPath.push(event.asStartElement());
        } else if (event.isEndElement()) {
            var startEvent = elementPath.pop();
            Preconditions.checkState(Objects.equals(startEvent.getName(), event.asEndElement().getName()),
                    "Unbalanced start/end events");
        }
        return event;
    }

    /**
     * Returns the text of the current XML tag if it doesn't have inner tags.
     *
     * @return the text
     * @throws XMLStreamException if the stream is not at the start element or the element has some other
     *         content
     * @see XMLEventReader#getElementText()
     */
    public String getCurrentElementText() throws XMLStreamException {
        var result = reader.getElementText();
        elementPath.pop();
        return result;
    }

    /**
     * Returns the next opening tag in the stream. If the document ends, returns null. Unlike
     * {@link XMLEventReader#nextTag()}, this does not return end events.
     *
     * @return the next tag or null
     * @throws XMLStreamException if the document is malformed
     */
    public @Nullable StartElement nextTag() throws XMLStreamException {
        while (reader.hasNext()) {
            var event = nextEvent();
            if (event.isStartElement()) {
                return event.asStartElement();
            }
        }
        return null;
    }

    /**
     * Returns the next tag enclosed in the {@code enclosing} tag. It may not be a direct child.
     *
     * @param enclosing the enclosing tag
     * @return the next tag or null if the enclosing tag ended
     * @throws XMLStreamException if the document is malformed
     */
    public @Nullable StartElement nextTagIn(StartElement enclosing) throws XMLStreamException {
        if (!elementPath.contains(enclosing)) {
            // It can be that we have already finished processing this element.
            return null;
        }
        while (reader.hasNext()) {
            var prevTop = elementPath.peekFirst();
            var event = nextEvent();
            if (event.isStartElement()) {
                return event.asStartElement();
            } else if (event.isEndElement() && event.asEndElement().getName().equals(enclosing.getName())
                    && prevTop == enclosing) {
                return null;
            }
        }
        return null;
    }

    /**
     * Skips everything until the given tag closes.
     *
     * @param tag the tag
     * @throws XMLStreamException if the document is malformed
     */
    public void skipTag(StartElement tag) throws XMLStreamException {
        while (nextTagIn(tag) != null) {
            // intentionally empty
        }
    }

    /**
     * A helper to be used in the foreach loop instead of {@link #nextTag()}.
     *
     * @return an iterable over {@link #nextTag()}
     * @throws XMLStreamException during iteration if the xml document cannot be parsed
     */
    @SuppressWarnings("RedundantThrows")
    public Iterable<StartElement> tags() throws XMLStreamException {
        return this::tagsIterator;
    }

    private Iterator<StartElement> tagsIterator() {
        return new AbstractIterator<>() {
            @Override
            protected @Nullable StartElement computeNext() {
                try {
                    var nextTag = nextTag();
                    return nextTag != null ? nextTag : endOfData();
                } catch (XMLStreamException e) {
                    throw MyThrowables.sneakyRethrow(e);
                }
            }
        };
    }

    /**
     * A helper to be used in the foreach loop instead of {@link #nextTagIn(StartElement)}.
     *
     * @return an iterable over {@link #nextTag()}
     * @throws XMLStreamException during iteration if the xml document cannot be parsed
     */
    @SuppressWarnings("RedundantThrows")
    public Iterable<StartElement> childrenOf(StartElement tag) throws XMLStreamException {
        return () -> childrenIterator(tag);
    }

    private Iterator<StartElement> childrenIterator(StartElement tag) {
        return new AbstractIterator<>() {
            @Override
            protected @Nullable StartElement computeNext() {
                try {
                    var nextTag = nextTagIn(tag);
                    return nextTag != null ? nextTag : endOfData();
                } catch (XMLStreamException e) {
                    throw MyThrowables.sneakyRethrow(e);
                }
            }
        };
    }
}
